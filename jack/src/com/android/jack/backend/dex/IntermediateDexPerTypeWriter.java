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
import com.android.jack.experimental.incremental.JackIncremental;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.library.FileType;
import com.android.jack.library.OutputLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.jack.scheduling.marker.DexCodeMarker;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.vfs.InputOutputVDir;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Write intermediate dex files per type.
 */
@Description("Write intermediate dex files per type")
@Constraint(need = {DexCodeMarker.class, ClassDefItemMarker.Complete.class})
@Produce(IntermediateDexProduct.class)
public class IntermediateDexPerTypeWriter extends DexWriter implements
    RunnableSchedulable<JDefinedClassOrInterface> {

  @CheckForNull
  private final OutputLibrary outputLibrary = Jack.getSession().getJackOutputLibrary();

  @CheckForNull
  protected InputOutputVDir intermediateDexDir = ThreadConfig.get(Options.INTERMEDIATE_DEX_DIR);

  @CheckForNull
  protected boolean generateDexFile = ThreadConfig.get(Options.GENERATE_DEX_FILE).booleanValue();

  private final boolean forceJumbo = ThreadConfig.get(CodeItemBuilder.FORCE_JUMBO).booleanValue();

  private final boolean isIncrementalMode =
      ThreadConfig.get(JackIncremental.GENERATE_COMPILER_STATE).booleanValue();

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    assert !(type.getLocation() instanceof TypeInInputLibraryLocation
        && ((TypeInInputLibraryLocation) type.getLocation()).getInputLibraryLocation()
            .getInputLibrary().containsFileType(FileType.DEX));

    ClassDefItemMarker cdiMarker = type.getMarker(ClassDefItemMarker.class);
    assert cdiMarker != null;

    DexOptions options = new DexOptions();
    options.forceJumbo = forceJumbo;
    DexFile typeDex = new DexFile(options);
    typeDex.add(cdiMarker.getClassDefItem());
    OutputVFile vFile;
    OutputStream outStream = null;
    try {
      // In incremental mode, intermediate dex output is managed by incremental wrapper that used
      // INTERMEDIATE_DEX_DIR property and not by the output library, even if Jayce file output for
      // incremental mode is managed by the output library. It is the same thing if Jack generates
      // a dex file and jayce files in the same time. There are temporary checks until Jack
      // incremental support will be updated and Intermediate_dex_dir usage was cleaned.
      if (outputLibrary != null && !isIncrementalMode && intermediateDexDir == null) {
        assert generateDexFile || intermediateDexDir == null;
        vFile = outputLibrary.createFile(FileType.DEX,
            new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
      } else {
        assert intermediateDexDir != null;
        vFile = intermediateDexDir.createOutputVFile(getFilePath(type));
      }
    } catch (IOException e) {
      throw new JackIOException("Could not create Dex file in output "
          + (outputLibrary != null ? outputLibrary.getLocation().getDescription()
              : intermediateDexDir) + " for type " + Jack.getUserFriendlyFormatter().getName(type),
          e);
    }
    try {
      outStream = vFile.openWrite();
      typeDex.prepare();
      typeDex.writeTo(outStream, null, false);
    } catch (IOException e) {
      throw new JackIOException("Could not write Dex file to output " + vFile, e);
    } finally {
      if (outStream != null) {
        outStream.close();
      }
    }
  }
}
