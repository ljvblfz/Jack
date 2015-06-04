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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An abstract {@link DexWritingTool} containing common logic from default
 * and deterministic StandardMultiDexWritingTools.
 */
public abstract class StandardMultiDexWritingTool extends MultiDexWritingTool {

  @Nonnull
  private boolean seenNonMarkedType = false;

  @CheckForNull
  private JackMerger mainMerger;

  @Nonnull
  protected abstract JackMerger createMainMerger(int numberOfMainTypes);

  @Override
  public void merge(@Nonnull JDefinedClassOrInterface type) throws DexWritingException {
    if (type.getMarker(MainDexMarker.class) != null) {
      assert !seenNonMarkedType;
      try {
        assert mainMerger != null;
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

  @Override
  @Nonnull
  public Iterator<JDefinedClassOrInterface> sortAndPrepare(
      @Nonnull Collection<JDefinedClassOrInterface> types) {
    int numberOfMainTypes = 0;
    ArrayList<JDefinedClassOrInterface> mainList =
        new ArrayList<JDefinedClassOrInterface>();
    ArrayList<JDefinedClassOrInterface> defaultList =
        new ArrayList<JDefinedClassOrInterface>(types.size());
    for (JDefinedClassOrInterface type : types) {
      if (type.containsMarker(MainDexMarker.class)) {
        mainList.add(type);
        numberOfMainTypes++;
      } else {
        defaultList.add(type);
      }
    }

    sortAndPrepareInternal(defaultList, mainList);

    mainMerger = createMainMerger(numberOfMainTypes);

    mainList.addAll(defaultList);
    return mainList.iterator();
  }
}
