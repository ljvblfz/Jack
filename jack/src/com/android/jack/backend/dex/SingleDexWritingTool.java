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
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergingOverflowException;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.vfs.OutputVFS;
import com.android.sched.vfs.OutputVFile;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A {@link DexWritingTool} that merges dex files, each one corresponding to a type, to a single
 * dex.
 */
@ImplementationName(iface = DexWritingTool.class, name = "single-dex",
    description = "only emit one dex file")
public class SingleDexWritingTool extends DexWritingTool {

  @Override
  public void write(@Nonnull OutputVFS outputVDir) throws DexWritingException {

    JackMerger merger = new JackMerger(createDexFile());
    OutputVFile outputDex = getOutputDex(outputVDir);

    final OutputJackLibrary jackOutputLibrary = Jack.getSession().getJackOutputLibrary();

    Set<MatchableInputVFile> dexToMerge = new HashSet<MatchableInputVFile>();
    for (JDefinedClassOrInterface type : Jack.getSession().getTypesToEmit()) {
      dexToMerge.add(new MatchableInputVFile(getDexInputVFileOfType(jackOutputLibrary, type)));
    }
    addOrphanDexFiles(dexToMerge);

    for (MatchableInputVFile matchableVFile : dexToMerge) {
      try {
        mergeDex(merger, matchableVFile.getInputVFile());
      } catch (MergingOverflowException e) {
        throw new DexWritingException(new SingleDexOverflowException(e));
      }
    }

    finishMerge(merger, outputDex);
  }

  @Nonnull
  private OutputVFile getOutputDex(@Nonnull OutputVFS outputVDir) throws DexWritingException {
    return getOutputDex(outputVDir, 1);
  }
}
