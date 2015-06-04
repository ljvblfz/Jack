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

import com.android.jack.Options;
import com.android.jack.tools.merger.ConstantManager;
import com.android.jack.tools.merger.JackMerger;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;


/**
 * A helper to manage mergers which make sure that one group of types is merged
 * before the rest.
 */
public class MarkedFirstMergingManager extends DeterministicMergingManager {

  @Override
  @Nonnull
  protected JackMerger getNewJackMerger(int firstTypeIndex) {
    return new JackMerger(createDexFile(), ConstantManager.getDefaultInstance(
        ThreadConfig.get(Options.BEST_MERGING_ACCURACY).booleanValue()));
  }

  @Nonnull
  public synchronized JackMerger getAndCreateMarkedFirstMerger(int numberOfMarkedTypes) {
    currentMergerIdx++;
    mergers.add(new JackMerger(createDexFile(), ConstantManager.getDefaultInstance(
        ThreadConfig.get(Options.BEST_MERGING_ACCURACY).booleanValue(),
        numberOfMarkedTypes)));
    return mergers.get(currentMergerIdx);
  }

}