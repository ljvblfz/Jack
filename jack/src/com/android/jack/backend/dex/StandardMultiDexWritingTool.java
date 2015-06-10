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

import com.android.jack.backend.dex.MergingManager.AvailableMergerIterator;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergingOverflowException;
import com.android.sched.util.codec.ImplementationName;

import javax.annotation.Nonnull;

/**
 * A {@link DexWritingTool} that merges dex files, each one corresponding to a type, in several dex
 * files. It is assumed that all types marked for MainDex will be submitted before any not marked
 * type.
 */
@ImplementationName(iface = DexWritingTool.class, name = "multidex",
    description = "allow emitting several dex files")
public class StandardMultiDexWritingTool extends MultiDexWritingTool {
  @Nonnull
  private boolean seenNonMarkedType = false;

  @Nonnull
  private final JackMerger mainMerger = manager.getIterator().next(0);

  @Override
  public void merge(@Nonnull JDefinedClassOrInterface type) throws DexWritingException {
    if (type.getMarker(MainDexMarker.class) != null) {
      assert !seenNonMarkedType;
      try {
        manager.mergeDex(mainMerger, type);
      } catch (MergingOverflowException e) {
        throw new DexWritingException(new MainDexOverflowException(e));
      }
    } else {
      seenNonMarkedType = true;
      AvailableMergerIterator iter = manager.getIterator();
      JackMerger merger = iter.current();
      while (iter.hasNext()) {
        try {
          manager.mergeDex(merger, type);
          break;
        } catch (MergingOverflowException e) {
          merger = iter.next(e.getTypeIndex());
        }
      }
      assert iter.hasNext();
    }
  }
}
