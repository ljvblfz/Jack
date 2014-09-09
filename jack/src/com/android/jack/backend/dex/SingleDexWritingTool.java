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

import com.android.jack.JackIOException;
import com.android.jack.JackUserException;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergeOverflow;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.vfs.InputOutputVFile;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A {@link DexWritingTool} that merges dex files, each one corresponding to a type, to a single
 * dex.
 */
@ImplementationName(iface = DexWritingTool.class, name = "single-dex",
    description = "only emit one dex file")
public class SingleDexWritingTool extends DexWritingTool {

  @Override
  public void write(@Nonnull OutputVDir outputVDir) throws JackIOException {
    JackMerger merger = new JackMerger(createDexFile());
    OutputVFile outputDex = getOutputDex(outputVDir);
    List<InputVFile> dexList = new ArrayList<InputVFile>();
    getAllDexFilesFromDir(getTypeDexDir(), dexList);

    for (InputVFile currentDex : dexList) {
      try {
        mergeDex(merger, currentDex);
      } catch (MergeOverflow e) {
        throw new JackUserException("Index overflow while merging dex files. Try using multidex",
            e);
      }
    }
    finishMerge(merger, outputDex);
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

  private OutputVFile getOutputDex(@Nonnull OutputVDir outputVDir) {
    return getOutputDex(outputVDir, 1);
  }
}
