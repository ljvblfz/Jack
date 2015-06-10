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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.util.codec.VariableName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * A helper to write dex files in multi dex mode.
 */
@VariableName("writer")
public abstract class MultiDexWritingTool extends DexWritingTool {

  protected void sortAndNumberInternal(@Nonnull ArrayList<JDefinedClassOrInterface> defaultList,
      @Nonnull ArrayList<JDefinedClassOrInterface> mainList) {

  }

  @Override
  @Nonnull
  public Iterator<JDefinedClassOrInterface> sortAndNumber(
      Collection<JDefinedClassOrInterface> types) {
    ArrayList<JDefinedClassOrInterface> mainList =
        new ArrayList<JDefinedClassOrInterface>();
    ArrayList<JDefinedClassOrInterface> defaultList =
        new ArrayList<JDefinedClassOrInterface>(types.size());
    for (JDefinedClassOrInterface type : types) {
      if (type.containsMarker(MainDexMarker.class)) {
        mainList.add(type);
      } else {
        defaultList.add(type);
      }
    }

    sortAndNumberInternal(defaultList, mainList);

    mainList.addAll(defaultList);
    return mainList.iterator();
  }

}
