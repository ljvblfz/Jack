/*
 * Copyright (C) 2015 The Android Open Source Project
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
import com.android.jack.ir.formatter.UserFriendlyFormatter;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergingOverflowException;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFS;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A helper to manage mergers.
 */
public class MergingManager {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final boolean forceJumbo = ThreadConfig.get(CodeItemBuilder.FORCE_JUMBO).booleanValue();

  private int currentMergerIdx = -1;

  private static final int mergersLimit = 100;

  @Nonnull
  private final List<JackMerger> mergers = new ArrayList<JackMerger>();

  @Nonnull
  protected final OutputJackLibrary jackOutputLibrary = Jack.getSession().getJackOutputLibrary();

  @Nonnull
  public JackMerger getNewJackMerger(int firstTypeIndex) {
    return new JackMerger(createDexFile(),
        ThreadConfig.get(Options.BEST_MERGING_ACCURACY).booleanValue());
  }

  public void mergeDex(@Nonnull JackMerger merger, @Nonnull JDefinedClassOrInterface type)
      throws MergingOverflowException, DexWritingException {
    InputVFile inputDex = getDexInputVFileOfType(jackOutputLibrary, type);
    try {
      merger.addDexFile(new DexBuffer(inputDex.getInputStream()), -1);
    } catch (IOException e) {
      throw new DexWritingException(new CannotReadException(inputDex, e));
    }
  }

  public AvailableMergerIterator getIterator() {
    return new AvailableMergerIterator();
  }

  @Nonnull
  protected DexFile createDexFile() {
    DexOptions options = new DexOptions();
    options.forceJumbo = forceJumbo;
    return new DexFile(options);
  }

  public void finishMerge(@Nonnull OutputVFS outputVDir) throws DexWritingException {
    for (int i = 0; i < mergers.size(); i++) {
      finishMerge(mergers.get(i), getOutputDex(outputVDir, i + 1));
    }
  }

  private void finishMerge(@Nonnull JackMerger merger, @Nonnull OutputVFile out)
      throws DexWritingException {
    OutputStream os = null;
    try {
      try {
        os = new BufferedOutputStream(out.getOutputStream());
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

  @Nonnull
  private synchronized int getCurrentMergerIdx() {
    return currentMergerIdx;
  }

  @Nonnull
  private synchronized int getNextMergerIdx(int oldMergerIdx, int typeIdx) {
    if (currentMergerIdx > oldMergerIdx) {
      return getCurrentMergerIdx();
    }
    currentMergerIdx++;
    mergers.add(getNewJackMerger(typeIdx));
    return getCurrentMergerIdx();
  }

  /**
   * An iterator on mergers. Takes care of creating new mergers when they are required.
   */
  class AvailableMergerIterator {

    private int currentIdx;

    public AvailableMergerIterator() {
      currentIdx = getCurrentMergerIdx();
    }

    public boolean hasNext() {
      return currentIdx < mergersLimit;
    }

    public JackMerger current() {
      return mergers.get(getCurrentMergerIdx());
    }

    public JackMerger next(int firstTypeIndex) {
      currentIdx = getNextMergerIdx(currentIdx, firstTypeIndex);
      return mergers.get(currentIdx);
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

  @Nonnull
  protected InputVFile getDexInputVFileOfType(@Nonnull OutputJackLibrary jackOutputLibrary,
      @Nonnull JDefinedClassOrInterface type) {
    InputVFile inputVFile = null;
    Location location = type.getLocation();
    try {
      if (location instanceof TypeInInputLibraryLocation) {
        InputLibrary inputLibrary =
            ((TypeInInputLibraryLocation) location).getInputLibraryLocation().getInputLibrary();
        if (inputLibrary.containsFileType(FileType.DEX)) {
          inputVFile = inputLibrary.getFile(FileType.DEX,
              new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
        }
      }

      if (inputVFile == null) {
        inputVFile = jackOutputLibrary.getFile(FileType.DEX,
            new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
      }
    } catch (FileTypeDoesNotExistException e) {
      // this was created by Jack, so this should not happen
      throw new AssertionError(
          UserFriendlyFormatter.getFormatter().getName(type) + " does not exist");
    }

    return inputVFile;
  }

}