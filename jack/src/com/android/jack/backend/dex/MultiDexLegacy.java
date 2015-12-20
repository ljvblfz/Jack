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

import com.android.jack.library.DumpInLibrary;
import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;

import javax.annotation.Nonnull;

/**
 * A {@link Feature} that represents multidex support for legacy device.
 */
@HasKeyId
@Description("Multidex support for legacy device")
public class MultiDexLegacy implements Feature {

  @Nonnull
  public static final BooleanPropertyId MULTIDEX_LEGACY = BooleanPropertyId.create(
      "jack.dex.output.multidex.legacy",
      "Enable multidex compatibility support for devices that do not have native runtime support")
      .addDefaultValue(false).addCategory(DumpInLibrary.class);
}
