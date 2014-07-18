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

import com.android.jack.JackFileException;
import com.android.jack.JackIOException;
import com.android.jack.JackUserException;
import com.android.jack.Options;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.naming.CompositeName;
import com.android.jack.ir.naming.TypeName;
import com.android.jack.ir.naming.TypeName.Kind;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergeOverflow;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.vfs.InputOutputVDir;
import com.android.sched.vfs.InputOutputVFile;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Common code used to write dex into a file or a zip file.
 */
public abstract class DexWriter {

  @Nonnull
  public static final String DEX_FILE_EXTENSION = ".dex";

  protected final boolean emitOneDexPerType = ThreadConfig.get(Options.GENERATE_ONE_DEX_PER_TYPE)
        .booleanValue();

  protected void mergeDexPerType(@Nonnull DexFile dexFile, @Nonnull OutputStream os)
      throws JackUserException {
    JackMerger merger = new JackMerger(dexFile);

    List<InputVFile> dexFiles = new ArrayList<InputVFile>();
    InputOutputVDir dexFileVDir = ThreadConfig.get(Options.DEX_FILE_FOLDER);
    fillDexFiles(dexFileVDir, dexFiles);

    for (InputVFile dexFileToMerge : dexFiles) {
      try {
        merger.addDexFile(new DexBuffer(dexFileToMerge.openRead()));
      } catch (IOException e) {
        throw new JackIOException(
            "Could not read Dex file '" + dexFileToMerge.getLocation().getDescription() + "'", e);
      } catch (MergeOverflow e) {
        throw new JackUserException("Index overflow during merge of dex files", e);
      }
    }

    try {
      merger.finish(os);
    } catch (IOException e) {
      throw new JackFileException("Could not write Dex file to output", e);
    }
  }

  @Nonnull
  protected DexFile getDexFile(@Nonnull JSession session) {
    DexFileMarker dexFileMarker = session.getMarker(DexFileMarker.class);
    assert dexFileMarker != null;
    DexFile dexFile = dexFileMarker.getFinalDexFile();
    assert dexFile != null;
    return dexFile;
  }

  @Nonnull
  protected VPath getFilePath(@Nonnull JDefinedClassOrInterface type) {
    return new VPath(new CompositeName(new TypeName(Kind.BINARY_QN, type),
        DEX_FILE_EXTENSION), '/');
  }

  private void fillDexFiles(@Nonnull InputOutputVDir dexFileVDir,
      @Nonnull List<InputVFile> dexFiles) {
    for (InputVElement subFile : dexFileVDir.list()) {
      if (subFile.isVDir()) {
        fillDexFiles((InputOutputVDir) subFile, dexFiles);
      } else if (subFile.getName().endsWith(DEX_FILE_EXTENSION)) {
        dexFiles.add((InputOutputVFile) subFile);
      }
    }
  }
}
