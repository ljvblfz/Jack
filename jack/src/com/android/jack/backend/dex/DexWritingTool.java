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
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.OverflowException;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.vfs.InputRootVDir;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A helper to write dex files.
 */
public abstract class DexWritingTool {

  @Nonnull
  private final boolean forceJumbo = ThreadConfig.get(CodeItemBuilder.FORCE_JUMBO).booleanValue();

  @Nonnull
  protected DexFile createDexFile() {
    DexOptions options = new DexOptions();
    options.forceJumbo = forceJumbo;
    return new DexFile(options);
  }

  public abstract void write(@Nonnull OutputVDir outputVDir) throws JackIOException;

  protected InputRootVDir getTypeDexDir() {
    return (InputRootVDir) ThreadConfig.get(Options.TYPEDEX_DIR);
  }

  protected void finishMerge(@Nonnull JackMerger merger, @Nonnull OutputVFile out)
      throws JackIOException {
    OutputStream os = null;
    try {
      try {
        os = new BufferedOutputStream(out.openWrite());
        merger.finish(os);
      } finally {
        if (os != null) {
          os.close();
        }
      }
    } catch (IOException e) {
      throw new JackIOException("Cannot write to output dex " + out, e);
    }
  }

  protected void mergeDex(@Nonnull JackMerger merger, InputVFile inputDex) throws JackIOException,
      OverflowException {
    try {
      merger.addDexFile(new DexBuffer(inputDex.openRead()));
    } catch (IOException e) {
      throw new JackIOException("Could not read Dex from " + inputDex, e);
    }
  }

  protected OutputVFile getOutputDex(@Nonnull OutputVDir outputVDir, int dexCount)
      throws JackIOException {
    assert dexCount >= 1;
    String dexName;
    if (dexCount == 1) {
      dexName = DexFileWriter.DEX_FILENAME;
    } else {
      dexName = DexFileWriter.DEX_PREFIX + dexCount + DexFileWriter.DEX_FILE_EXTENSION;
    }
    try {
      return outputVDir.createOutputVFile(new VPath(dexName, '/'));
    } catch (IOException e) {
      throw new JackIOException("Cannot create dex file '" + dexName + "' in " + outputVDir, e);
    }
  }

  protected void fillDexLists(List<InputVFile> mainDexList, List<InputVFile> anyDexList)
      throws JackIOException {
    for (JDefinedClassOrInterface type : Jack.getSession().getTypesToEmit()) {
      try {
        InputVFile inputVFile = getTypeDexDir().getInputVFile(DexWriter.getFilePath(type));
        if (type.containsMarker(MainDexMarker.class)) {
          mainDexList.add(inputVFile);
        } else {
          anyDexList.add(inputVFile);
        }
      } catch (NotFileOrDirectoryException e) {
        throw new JackIOException("Error trying to read file for type '"
            + Jack.getUserFriendlyFormatter().getName(type) + "'", e);
      }
    }
  }
}
