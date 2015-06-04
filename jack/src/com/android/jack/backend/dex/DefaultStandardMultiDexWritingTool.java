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
import com.android.jack.tools.merger.ConstantManager;
import com.android.jack.tools.merger.JackMerger;
import com.android.sched.util.codec.ImplementationName;

import java.util.ArrayList;

import javax.annotation.Nonnull;

/**
 * A {@link DexWritingTool} that merges dex files, each one corresponding to a type, in several dex
 * files. It is assumed that all types marked for MainDex will be submitted before any not marked
 * type.
 */
@ImplementationName(iface = DexWritingTool.class, name = "multidex",
    description = "allow emitting several dex files")
public class DefaultStandardMultiDexWritingTool extends StandardMultiDexWritingTool {

  @Override
  @Nonnull
  protected MergingManager getManager() {
    return new MarkedFirstMergingManager();
  }

  @Override
  @Nonnull
  protected JackMerger createMainMerger(int numberOfMainTypes) {
    MarkedFirstMergingManager markedFirstManager = (MarkedFirstMergingManager) manager;
    return markedFirstManager.getAndCreateMarkedFirstMerger(numberOfMainTypes);
  }

  @Override
  protected void sortAndPrepareInternal(@Nonnull ArrayList<JDefinedClassOrInterface> defaultList,
      @Nonnull ArrayList<JDefinedClassOrInterface> mainList) {
    for (JDefinedClassOrInterface type : mainList) {
      type.addMarker(new NumberMarker(ConstantManager.DEFAULT_MULTIDEX_MAIN_DEX_INDEX));
    }
    for (JDefinedClassOrInterface type : defaultList) {
      type.addMarker(new NumberMarker(ConstantManager.DEFAULT_MULTIDEX_NON_MAIN_DEX_INDEX));
    }
  }

}
