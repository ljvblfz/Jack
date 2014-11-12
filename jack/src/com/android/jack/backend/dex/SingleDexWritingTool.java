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

import com.google.common.collect.Iterators;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.library.FileType;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergingOverflowException;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.InputOutputVFile;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;

import java.util.ArrayList;
import java.util.Iterator;
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
  public void write(@Nonnull OutputVDir outputVDir) throws DexWritingException {
    JackMerger merger = new JackMerger(createDexFile());
    OutputVFile outputDex = getOutputDex(outputVDir);
    Iterator<InputVFile> inputVFileIt;

    // Intermediate dex files can be located into the intermediate dex dir or into a library
    OutputJackLibrary jackOutputLibrary = Jack.getSession().getJackOutputLibrary();
    if (jackOutputLibrary != null && jackOutputLibrary.containsFileType(FileType.DEX)) {
      inputVFileIt = jackOutputLibrary.iterator(FileType.DEX);
    } else {
      List<InputVFile> dexList = new ArrayList<InputVFile>();
      getAllDexFilesFromDir(getIntermediateDexDir(), dexList);
      inputVFileIt = dexList.iterator();
    }

    inputVFileIt = getAllDexFilesFromLib(inputVFileIt);
    while (inputVFileIt.hasNext()) {
      try {
        mergeDex(merger, inputVFileIt.next());
      } catch (MergingOverflowException e) {
        throw new DexWritingException(new SingleDexOverflowException(e));
      }
    }
    finishMerge(merger, outputDex);
  }

  private Iterator<InputVFile> getAllDexFilesFromLib(@Nonnull Iterator<InputVFile> inputVFileIt) {
    Iterator<InputVFile> newInputVFileIt = inputVFileIt;
    List<InputLibrary> librariesDone = new ArrayList<InputLibrary>();
    for (JDefinedClassOrInterface jdcoi : Jack.getSession().getTypesToEmit()) {
      Location loc = jdcoi.getLocation();
      if (loc instanceof TypeInInputLibraryLocation) {
        InputLibrary inputLibrary =
            ((TypeInInputLibraryLocation) loc).getInputLibraryLocation().getInputLibrary();
        if (!librariesDone.contains(inputLibrary)) {
          if (inputLibrary.containsFileType(FileType.DEX)) {
            newInputVFileIt =
                Iterators.concat(newInputVFileIt, inputLibrary.iterator(FileType.DEX));
          }
          librariesDone.add(inputLibrary);
        }
      }
    }
    return (newInputVFileIt);
  }

  private void getAllDexFilesFromDir(@Nonnull InputVDir dexFileVDir,
      @Nonnull List<InputVFile> dexFiles) {
    for (InputVElement subFile : dexFileVDir.list()) {
      if (subFile.isVDir()) {
        getAllDexFilesFromDir((InputVDir) subFile, dexFiles);
      } else if (FileType.DEX.isOfType((InputVFile) subFile)) {
        dexFiles.add((InputOutputVFile) subFile);
      }
    }
  }

  @Nonnull
  private OutputVFile getOutputDex(@Nonnull OutputVDir outputVDir) throws DexWritingException {
    return getOutputDex(outputVDir, 1);
  }
}
