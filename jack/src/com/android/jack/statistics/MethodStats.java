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

import com.android.jack.Options;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.scheduling.feature.CompiledTypeStats;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.PercentImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;

/**
 * This {@link RunnableSchedulable} computes some statistics about compiled methods.
 */
@Description("Computes some statistics about compiled methods.")
@Support(CompiledTypeStats.class)
public class MethodStats implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  public static final StatisticId<Percent> CONCRETE_METHODS_PERCENT = new StatisticId<Percent>(
      "jack.source.method.concrete", "Concrete methods",
      PercentImpl.class, Percent.class);

  @Nonnull
  public static final StatisticId<Counter> INSTANCE_METHODS_COUNT = new StatisticId<Counter>(
      "jack.source.method.instance", "Instance methods",
      CounterImpl.class, Counter.class);

  @Nonnull
  public static final StatisticId<Counter> STATIC_METHODS_COUNT = new StatisticId<Counter>(
      "jack.source.method.static", "Static methods",
      CounterImpl.class, Counter.class);

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || !filter.accept(this.getClass(), method)) {
      return;
    }
    Tracer tracer = TracerFactory.getTracer();
    if (!method.isNative() && !method.isAbstract()) {
      tracer.getStatistic(CONCRETE_METHODS_PERCENT).addTrue();
    } else {
      tracer.getStatistic(CONCRETE_METHODS_PERCENT).addFalse();
    }
    if (Modifier.isStatic(method.getModifier())) {
      tracer.getStatistic(STATIC_METHODS_COUNT).incValue();
    } else {
      tracer.getStatistic(INSTANCE_METHODS_COUNT).incValue();
    }
  }
}
