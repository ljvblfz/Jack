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

package com.android.jack.optimizations.wofr;

import com.android.jack.optimizations.Optimizations;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import javax.annotation.Nonnull;

/** Base class for all write-only field removal schedulables */
public abstract class WofrSchedulable {
  @Nonnull
  public static final StatisticId<Counter> FIELD_WRITES_REMOVED = new StatisticId<>(
      "jack.optimization.write-only-field-removal.writes-removed", "Field writes removed",
      CounterImpl.class, Counter.class);

  @Nonnull
  public static final StatisticId<Counter> FIELDS_REMOVED = new StatisticId<>(
      "jack.optimization.write-only-field-removal.fields-removed", "Fields removed",
      CounterImpl.class, Counter.class);

  public final boolean preserveJls =
      ThreadConfig.get(Optimizations.WriteOnlyFieldRemoval.PRESERVE_JLS).booleanValue();

  public final boolean preserveReflections =
      ThreadConfig.get(Optimizations.WriteOnlyFieldRemoval.PRESERVE_REFLECTIONS).booleanValue();
}
