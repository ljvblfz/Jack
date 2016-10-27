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

package com.android.jack.ir.ast.cfg;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.formatter.TypePackageAndMethodFormatter;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.ExtendedSample;
import com.android.sched.util.log.stats.ExtendedSampleImpl;
import com.android.sched.util.log.stats.StatisticId;

import javax.annotation.Nonnull;

/** Counts and reports the number of control flow graphs. */
@Description("Counts and reports the number of control flow graphs.")
@Filter(TypeWithoutPrebuiltFilter.class)
public class ControlFlowGraphSizeTracker implements RunnableSchedulable<JControlFlowGraph> {
  @Nonnull
  public static final StatisticId<ExtendedSample> STATISTICS = new StatisticId<>(
      "jack.cfg.statistics", "Control flow graphs statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();
  @Nonnull
  private final TypePackageAndMethodFormatter formatter = Jack.getUserFriendlyFormatter();

  @Override
  public void run(@Nonnull final JControlFlowGraph cfg) {
    JMethod method = cfg.getMethod();
    tracer.getStatistic(STATISTICS).add(cfg.getBlocksDepthFirst(/* forward = */ false).size(),
        formatter.getName(method) + " [" + formatter.getName(method.getEnclosingType()) + "]");
  }
}
