/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.scheduling.filter;

import com.android.jack.Options;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.schedulable.ComponentFilter;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * A filter that only accepts types without corresponding valid whole dex prebuilt. These are either
 * types from the source code, types from libraries without prebuilt for this type or types for
 * which only prebuilt code is valid.
 */
@Description("Filter accepting types without a valid type prebuilt")
public class TypeWithoutValidTypePrebuilt
  implements ComponentFilter<JDefinedClassOrInterface> {

  private final boolean isUseTypePrebuiltEnabled =
      ThreadConfig.get(Options.USE_WHOLE_DEX_PREBUILT).booleanValue();

  @Override
  public boolean accept(@Nonnull JDefinedClassOrInterface clOrI) {
    if (!isUseTypePrebuiltEnabled) {
      return true;
    }
    return !TypeWithoutPrebuiltFilter.hasPrebuilt(clOrI);
  }

}

