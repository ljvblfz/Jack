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
import com.android.jack.Options;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.dx.dex.DexOptions;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergingOverflowException;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFS;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A helper to write dex files.
 */
public abstract class DexWritingTool {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final boolean forceJumbo = ThreadConfig.get(CodeItemBuilder.FORCE_JUMBO).booleanValue();

  @Nonnull
  protected DexFile createDexFile() {
    DexOptions options = new DexOptions();
    options.forceJumbo = forceJumbo;
    return new DexFile(options);
  }

  public abstract void write(@Nonnull OutputVFS outputVDir) throws DexWritingException;


  @Nonnull
  protected InputVFS getIntermediateDexDir() {
    return ThreadConfig.get(Options.INTERMEDIATE_DEX_DIR);
  }

  protected void finishMerge(@Nonnull JackMerger merger, @Nonnull OutputVFile out)
      throws DexWritingException {
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
      throw new DexWritingException(e);
    }
  }

  protected void mergeDex(@Nonnull JackMerger merger, InputVFile inputDex)
      throws MergingOverflowException, DexWritingException {
    try {
      merger.addDexFile(new DexBuffer(inputDex.openRead()));
    } catch (IOException e) {
      throw new DexWritingException(new CannotReadException(inputDex.getLocation(), e));
    }
  }

  @Nonnull
  protected OutputVFile getOutputDex(@Nonnull OutputVFS outputVfs, int dexCount)
      throws DexWritingException {
    assert dexCount >= 1;
    String dexName;
    if (dexCount == 1) {
      dexName = DexFileWriter.DEX_FILENAME;
    } else {
      dexName = DexFileWriter.DEX_PREFIX + dexCount + FileType.DEX.getFileExtension();
    }
    try {
      return outputVfs.getRootOutputVDir().createOutputVFile(new VPath(dexName, '/'));
    } catch (CannotCreateFileException e) {
      throw new DexWritingException(e);
    }
  }

  protected void fillDexLists(@Nonnull List<InputVFile> mainDexList,
      @Nonnull List<InputVFile> anyDexList) throws LibraryFormatException {
    OutputJackLibrary jackOutputLibrary = Jack.getSession().getJackOutputLibrary();
    boolean generateLibWithDex =
        jackOutputLibrary != null && jackOutputLibrary.containsFileType(FileType.DEX);

    for (JDefinedClassOrInterface type : Jack.getSession().getTypesToEmit()) {
      try {
        InputVFile inputVFile;
        if (generateLibWithDex) {
          assert jackOutputLibrary != null;
          inputVFile = jackOutputLibrary.getFile(FileType.DEX,
              new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
        } else {
          inputVFile = getIntermediateDexFile(type);
        }

        if (type.containsMarker(MainDexMarker.class)) {
          mainDexList.add(inputVFile);
        } else {
          anyDexList.add(inputVFile);
        }
      } catch (FileTypeDoesNotExistException e) {
        // this was created by Jack, so this should not happen
        throw new AssertionError(e);
      } catch (NotFileOrDirectoryException e) {
        // this was created by Jack, so this should not happen
        throw new AssertionError(e);
      } catch (NoSuchFileException e) {
        // this was created by Jack, so this should not happen
        throw new AssertionError(e);
      }
    }
  }

  @Nonnull
  private InputVFile getIntermediateDexFile(@Nonnull JDefinedClassOrInterface type)
      throws NotFileOrDirectoryException, NoSuchFileException, FileTypeDoesNotExistException {
    InputVFile inputVFile = null;

    // Intermediate dex files can be located into the intermediate dex dir or into a library
    OutputJackLibrary jackOutputLibrary = Jack.getSession().getJackOutputLibrary();
    if (jackOutputLibrary != null && jackOutputLibrary.containsFileType(FileType.DEX)) {
      inputVFile = jackOutputLibrary.getFile(FileType.DEX,
          new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
    } else {
      inputVFile =
          getIntermediateDexDir().getRootInputVDir().getInputVFile(DexWriter.getFilePath(type));
    }

    return inputVFile;
  }
}
