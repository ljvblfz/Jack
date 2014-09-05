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
import com.android.jack.JackFileException;
import com.android.jack.JackIOException;
import com.android.jack.JackUserException;
import com.android.jack.Options;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergeOverflow;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.InputOutputVFile;
import com.android.sched.vfs.InputRootVDir;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A {@link DexWritingTool} that merges dex files, each one corresponding to a type.
 */
public class MergingDexWritingTool extends DexWritingTool {

  @Nonnull
  private JackMerger merger;
  @Nonnegative
  private int dexCount = 1;
  @Nonnull
  private OutputVFile dexVFile;

  public MergingDexWritingTool(@Nonnull OutputVDir outputVDir) {
    super(outputVDir);
    merger = new JackMerger(createDexFile());
    try {
      dexVFile = getNextOutputDex();
    } catch (IOException e) {
      throw new JackIOException("Error creating output dex", e);
    }
  }

  @Override
  public void write() {

    InputRootVDir dexFileVDir = (InputRootVDir) ThreadConfig.get(Options.TYPEDEX_DIR);
    boolean isMultidex = ThreadConfig.get(DexFileWriter.MULTIDEX).booleanValue();

    try {
      if (isMultidex) {

        List<InputVFile> mainDexList = new ArrayList<InputVFile>();
        List<InputVFile> anyDexList = new ArrayList<InputVFile>();
        for (JDefinedClassOrInterface type : Jack.getSession().getTypesToEmit()) {
          try {
            InputVFile inputVFile = dexFileVDir.getInputVFile(DexWriter.getFilePath(type));
            if (type.containsMarker(MainDexMarker.class)) {
              mainDexList.add(inputVFile);
            } else {
              anyDexList.add(inputVFile);
            }
          } catch (NotFileOrDirectoryException e) {
            throw new JackFileException("Error trying to read file for type '"
                + Jack.getUserFriendlyFormatter().getName(type) + "'", e);
          }
        }

        mergeDexesFromList(mainDexList, OverflowPolicy.MULTIDEX_MAIN_DEX);

        if (ThreadConfig.get(DexFileWriter.MINIMAL_MAIN_DEX).booleanValue()) {
          finishMerge(merger, dexVFile, outputVDir.getLocation());
          dexVFile = getNextOutputDex();
          merger = new JackMerger(createDexFile());
        }

        assert anyDexList != null;
        mergeDexesFromList(anyDexList, OverflowPolicy.MULTIDEX_ANY_DEX);

      } else {

        List<InputVFile> dexList = new ArrayList<InputVFile>();
        getAllDexFilesFromDir(dexFileVDir, dexList);
        mergeDexesFromList(dexList, OverflowPolicy.SINGLE_DEX);
      }

      finishMerge(merger, dexVFile, outputVDir.getLocation());
    } catch (IOException e) {
      throw getWriteException(outputVDir.getLocation(), e);
    }
  }

  private void mergeDexesFromList(List<InputVFile> dexList, OverflowPolicy overflowPolicy)
      throws IOException {
    for (InputVFile currentDex : dexList) {
      try {
        merger.addDexFile(new DexBuffer(currentDex.openRead()));
      } catch (IOException e) {
        throw new JackIOException(
            "Could not read Dex file '" + currentDex.getLocation().getDescription() + "'", e);
      } catch (MergeOverflow e) {
        switch (overflowPolicy) {
          case SINGLE_DEX:
            throw new JackUserException(
                "Index overflow while merging dex files. Try using multidex.", e);
          case MULTIDEX_MAIN_DEX:
            throw new JackUserException(
                "Too many classes in main dex. Index overflow while merging dex files.", e);
          case MULTIDEX_ANY_DEX:
            finishMerge(merger, dexVFile, outputVDir.getLocation());
            dexVFile = getNextOutputDex();
            merger = new JackMerger(createDexFile());
            break;
          default:
            throw new AssertionError();
        }
      }
    }

  }

  private void getAllDexFilesFromDir(@Nonnull InputVDir dexFileVDir,
      @Nonnull List<InputVFile> dexFiles) {
    for (InputVElement subFile : dexFileVDir.list()) {
      if (subFile.isVDir()) {
        getAllDexFilesFromDir((InputVDir) subFile, dexFiles);
      } else if (subFile.getName().endsWith(DexFileWriter.DEX_FILE_EXTENSION)) {
        dexFiles.add((InputOutputVFile) subFile);
      }
    }
  }

  private void finishMerge(@Nonnull JackMerger merger, @Nonnull OutputVFile out,
      @Nonnull Location outputLocation) {
    OutputStream os;
    try {
      os = new BufferedOutputStream(out.openWrite());
    } catch (IOException e) {
      throw new JackIOException("Failed to read intermediate dex", e);
    }
    try {
      merger.finish(os);
      os.close();
    } catch (IOException e) {
      try {
        os.close();
      } catch (IOException close) {
        // let the Exception for the initial error be handled
      }
      throw getWriteException(outputLocation, e);
    }
  }

  private JackFileException getWriteException(@Nonnull Location outputLocation,
      @Nonnull IOException e) {
    return new JackFileException("Could not write Dex file to output '"
        + outputLocation.getDescription() + "'", e);
  }

  private OutputVFile getNextOutputDex() throws IOException {
    assert dexCount >= 1;
    if (dexCount == 1) {
      dexCount++;
      return outputVDir.createOutputVFile(new VPath(DexFileWriter.DEX_FILENAME, '/'));
    } else {
      return outputVDir.createOutputVFile(
          new VPath(DexFileWriter.DEX_PREFIX + dexCount++ + DexFileWriter.DEX_FILE_EXTENSION, '/'));
    }
  }

  private static enum OverflowPolicy {
    SINGLE_DEX,
    MULTIDEX_MAIN_DEX,
    MULTIDEX_ANY_DEX;
  }

}
