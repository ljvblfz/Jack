/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.backend.dex;

import com.android.jack.Jack;
import com.android.jack.JackEventType;
import com.android.jack.JackIOException;
import com.android.jack.Options;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.dx.dex.DexOptions;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.LibraryLocation;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Abstract class to write dex files into library.
 */
public abstract class DexInLibraryWriter extends DexWriter implements
    RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private final OutputJackLibrary outputLibrary = Jack.getSession().getJackOutputLibrary();

  private final boolean forceJumbo = ThreadConfig.get(CodeItemBuilder.FORCE_JUMBO).booleanValue();

  private final int apiLevel = ThreadConfig.get(Options.ANDROID_MIN_API_LEVEL).intValue();

  private final boolean usePrebuilts =
      ThreadConfig.get(Options.USE_PREBUILT_FROM_LIBRARY).booleanValue();

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    OutputVFile vFile;

    Tracer tracer = TracerFactory.getTracer();
    Event event = tracer.start(JackEventType.DX_BACKEND);

    try {

      if (usePrebuilts) {
        Location loc = type.getLocation();
        if (loc instanceof TypeInInputLibraryLocation) {
          InputVFile in;
          InputLibrary inputLibrary = ((TypeInInputLibraryLocation) loc).getInputLibrary();
          LibraryLocation inputLibraryLocation = inputLibrary.getLocation();
          if (outputLibrary.containsLibraryLocation(inputLibraryLocation)) {
            return;
          }
          try {
            in = inputLibrary.getFile(FileType.PREBUILT,
                new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
            vFile = outputLibrary.createFile(FileType.PREBUILT,
                new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));

            vFile.copy(in);
            return;
          } catch (FileTypeDoesNotExistException e) {
            // Pre-dex is not accessible, thus write dex file from type
          }
        }
      }

      ClassDefItemMarker cdiMarker = type.getMarker(ClassDefItemMarker.class);
      assert cdiMarker != null;

      DexOptions options = new DexOptions();
      options.forceJumbo = forceJumbo;
      options.targetApiLevel = apiLevel;
      DexFile typeDex = new DexFile(options);
      typeDex.add(cdiMarker.getClassDefItem());
      OutputStream outStream = null;
      try {
        vFile = outputLibrary.createFile(FileType.PREBUILT,
            new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
      } catch (CannotCreateFileException e) {
        throw new JackIOException("Could not create Dex file in output "
            + outputLibrary.getLocation().getDescription() + " for type "
            + Jack.getUserFriendlyFormatter().getName(type), e);
      }
      try {
        outStream = vFile.getOutputStream();
        typeDex.getStringIds().intern(DexWriter.getJackDexTag());
        typeDex.prepare();
        typeDex.writeTo(outStream, null, false);
      } catch (IOException e) {
        throw new JackIOException(
            "Could not write Dex file to output " + vFile.getLocation().getDescription(), e);
      } finally {
        if (outStream != null) {
          outStream.close();
        }
      }
    } finally {
      event.end();
    }
  }
}
