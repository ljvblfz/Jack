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
import com.android.jack.library.OutputLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.jack.scheduling.marker.DexCodeMarker;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.location.Location;
import com.android.sched.util.stream.ByteStreamSucker;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Write dex files in library.
 */
@Description("Write dex files in library")
@Constraint(need = {DexCodeMarker.class, ClassDefItemMarker.Complete.class})
@Produce(DexInLibraryProduct.class)
public class DexInLibraryWriter extends DexWriter implements
    RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private final OutputLibrary outputLibrary = Jack.getSession().getJackOutputLibrary();

  private final boolean forceJumbo = ThreadConfig.get(CodeItemBuilder.FORCE_JUMBO).booleanValue();

  private final boolean usePrebuilts =
      ThreadConfig.get(Options.USE_PREBUILT_FROM_LIBRARY).booleanValue();

  private final boolean generateLibFromIncrementalDir =
      ThreadConfig.get(Options.GENERATE_LIBRARY_FROM_INCREMENTAL_FOLDER).booleanValue();

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    OutputVFile vFile;

    if (usePrebuilts) {
      Location loc = type.getLocation();
      if (loc instanceof TypeInInputLibraryLocation) {
        InputVFile in;
        InputLibrary inputLibrary =
            ((TypeInInputLibraryLocation) loc).getInputLibraryLocation().getInputLibrary();
        if (!generateLibFromIncrementalDir) {
          try {
            in = inputLibrary.getFile(FileType.PREBUILT,
                new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
            vFile = outputLibrary.createFile(FileType.PREBUILT,
                new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));

            InputStream is = in.getInputStream();
            OutputStream os = vFile.getOutputStream();
            try {
              new ByteStreamSucker(is, os, true).suck();
            } finally {
              is.close(); // is != null or check before
            }
            return;
          } catch (FileTypeDoesNotExistException e) {
            // Pre-dex is not accessible, thus write dex file from type
          }
        } else {
          return;
        }
      }
    }

    ClassDefItemMarker cdiMarker = type.getMarker(ClassDefItemMarker.class);
    assert cdiMarker != null;

    DexOptions options = new DexOptions();
    options.forceJumbo = forceJumbo;
    DexFile typeDex = new DexFile(options);
    typeDex.add(cdiMarker.getClassDefItem());
    OutputStream outStream = null;
    try {
      vFile = outputLibrary.createFile(FileType.PREBUILT,
          new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
    } catch (IOException e) {
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
  }
}
