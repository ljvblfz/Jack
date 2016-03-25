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

package com.android.jack.statistics;

import com.android.jack.ir.ast.JField;
import com.android.jack.scheduling.feature.CompiledTypeStats;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;

/**
 * This {@link RunnableSchedulable} computes some statistics about compiled fields.
 */
@Description("Computes some statistics about compiled fields.")
@Support(CompiledTypeStats.class)
@Filter(SourceTypeFilter.class)
public class FieldStats implements RunnableSchedulable<JField> {

  @Nonnull
  public static final StatisticId<Counter> INSTANCE_FIELDS_COUNT = new StatisticId<Counter>(
      "jack.source.field.instance", "Instance fields",
      CounterImpl.class, Counter.class);

  @Nonnull
  public static final StatisticId<Counter> STATIC_FIELDS_COUNT = new StatisticId<Counter>(
      "jack.source.field.static", "Static fields",
      CounterImpl.class, Counter.class);

  @Override
  public void run(@Nonnull JField field) throws Exception {
    if (field.isExternal()) {
      return;
    }
    Tracer tracer = TracerFactory.getTracer();
    if (Modifier.isStatic(field.getModifier())) {
      tracer.getStatistic(STATIC_FIELDS_COUNT).incValue();
    } else {
      tracer.getStatistic(INSTANCE_FIELDS_COUNT).incValue();
    }
  }
}
