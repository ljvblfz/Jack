/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched;

import com.android.sched.item.onlyfor.OnlyForType;
import com.android.sched.util.codec.ClassSelector;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.PropertyId;

import javax.annotation.Nonnull;

/**
 * Properties for SchedLib
 */
@HasKeyId
public class SchedProperties {
  @Nonnull
  public static final BooleanPropertyId FAILED_STOP = BooleanPropertyId.create(
      "sched.failedstop", "Define if the SchedLib stop at the first failed")
      .addDefaultValue("true");

  @Nonnull
  public static final PropertyId<Class<? extends OnlyForType>> ONLY_FOR = PropertyId.create(
      "sched.onlyfor", "Define which items to take into account",
      new ClassSelector<OnlyForType>(OnlyForType.class)).addDefaultValue("default");
}
