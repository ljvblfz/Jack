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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergingOverflowException;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.vfs.InputVFile;

import javax.annotation.Nonnull;

/**
 * A {@link DexWritingTool} that merges dex files, each one corresponding to a type, in several dex
 * files, keeping the main dex as small as possible.
 */
@ImplementationName(iface = DexWritingTool.class, name = "minimal-multidex", description =
    "allow emitting several dex files, keeping the first dex (main dex) as small as possible")
public class MinimalMultiDexWritingTool extends DexWritingTool {

  @Nonnull
  private final JackMerger mainMerger;

  public MinimalMultiDexWritingTool() {
    AvailableMergerIterator iter = new AvailableMergerIterator();
    mainMerger = iter.next();
  }

  @Override
  public void merge(@Nonnull JDefinedClassOrInterface type) throws DexWritingException {
    InputVFile vFile = getDexInputVFileOfType(jackOutputLibrary, type);
    if (type.getMarker(MainDexMarker.class) != null) {
      try {
        mergeDex(mainMerger, vFile);
      } catch (MergingOverflowException e) {
        throw new DexWritingException(new MainDexOverflowException(e));
      }
    } else {
      AvailableMergerIterator iter = new AvailableMergerIterator();
      JackMerger merger = iter.next();
      if (merger == mainMerger) {
        merger = iter.next();
      }
      do {
        try {
          mergeDex(merger, vFile);
          break;
        } catch (MergingOverflowException e) {
          merger = iter.next();
        }
      } while (iter.hasNext());
      assert iter.hasNext();
    }
  }

}
