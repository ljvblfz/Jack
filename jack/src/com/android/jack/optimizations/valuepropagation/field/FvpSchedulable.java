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

package com.android.jack.optimizations.valuepropagation.field;

import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import javax.annotation.Nonnull;

/** Base class for all field value propagation schedulables */
public abstract class FvpSchedulable {
  @Nonnull
  public static final StatisticId<Counter> FIELD_VALUES_PROPAGATED = new StatisticId<>(
      "jack.optimization.field-value-propagation", "Field value propagated",
      CounterImpl.class, Counter.class);

  @Nonnull
  static JValueLiteral createDefaultValue(@Nonnull JField field) {
    return (JValueLiteral) field.getType().createDefaultValue(field.getSourceInfo());
  }
}
