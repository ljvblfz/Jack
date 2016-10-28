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

import com.android.jack.ir.ast.JVisitor;
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

/** Counts and reports the statistics of different basic block kinds. */
@Description("Counts and reports the statistics of different basic block kinds.")
@Filter(TypeWithoutPrebuiltFilter.class)
public class CfgBasicBlockTracker implements RunnableSchedulable<JBasicBlock> {
  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private static final StatisticId<ExtendedSample> TOTAL_COUNT = new StatisticId<>(
      "jack.cfg.bb.total", "Total basic block statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);
  @Nonnull
  private static final StatisticId<ExtendedSample> RETURN_COUNT = new StatisticId<>(
      "jack.cfg.bb.return", "Return basic block statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);
  @Nonnull
  private static final StatisticId<ExtendedSample> ENTRY_COUNT = new StatisticId<>(
      "jack.cfg.bb.entry", "Entry basic block statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);
  @Nonnull
  private static final StatisticId<ExtendedSample> EXIT_COUNT = new StatisticId<>(
      "jack.cfg.bb.exit", "Exit basic block statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);
  @Nonnull
  private static final StatisticId<ExtendedSample> THROWING_EXPR_COUNT = new StatisticId<>(
      "jack.cfg.bb.throwing-expr", "Throwing expression basic block statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);
  @Nonnull
  private static final StatisticId<ExtendedSample> THROW_COUNT = new StatisticId<>(
      "jack.cfg.bb.throw", "Throw basic block statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);
  @Nonnull
  private static final StatisticId<ExtendedSample> SWITCH_COUNT = new StatisticId<>(
      "jack.cfg.bb.switch", "Switch basic block statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);
  @Nonnull
  private static final StatisticId<ExtendedSample> CASE_COUNT = new StatisticId<>(
      "jack.cfg.bb.catch", "Case basic block statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);
  @Nonnull
  private static final StatisticId<ExtendedSample> CATCH_COUNT = new StatisticId<>(
      "jack.cfg.bb.catch", "Catch basic block statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);
  @Nonnull
  private static final StatisticId<ExtendedSample> SIMPLE_COUNT = new StatisticId<>(
      "jack.cfg.bb.simple", "Simple basic block statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);
  @Nonnull
  private static final StatisticId<ExtendedSample> CONDITIONAL_COUNT = new StatisticId<>(
      "jack.cfg.bb.conditional", "Conditional basic block statistics",
      ExtendedSampleImpl.class, ExtendedSample.class);

  private final JVisitor processor = new JVisitor() {
    @Override public boolean visit(@Nonnull JBasicBlock block) {
      throw new AssertionError(block.getClass().getSimpleName());
    }

    @Override public boolean visit(@Nonnull JPlaceholderBasicBlock block) {
      throw new AssertionError();
    }

    @Override public boolean visit(@Nonnull JEntryBasicBlock block) {
      // Don't count in total
      tracer.getStatistic(ENTRY_COUNT).add(block.getElementCount());
      return false;
    }

    @Override public boolean visit(@Nonnull JExitBasicBlock block) {
      // Don't count in total
      tracer.getStatistic(EXIT_COUNT).add(block.getElementCount());
      return false;
    }

    @Override public boolean visit(@Nonnull JCaseBasicBlock block) {
      tracer.getStatistic(TOTAL_COUNT).add(block.getElementCount());
      tracer.getStatistic(CASE_COUNT).add(block.getElementCount());
      return false;
    }

    @Override public boolean visit(@Nonnull JCatchBasicBlock block) {
      tracer.getStatistic(TOTAL_COUNT).add(block.getElementCount());
      tracer.getStatistic(CATCH_COUNT).add(block.getElementCount());
      return false;
    }

    @Override public boolean visit(@Nonnull JSimpleBasicBlock block) {
      tracer.getStatistic(TOTAL_COUNT).add(block.getElementCount());
      tracer.getStatistic(SIMPLE_COUNT).add(block.getElementCount());
      return false;
    }

    @Override public boolean visit(@Nonnull JConditionalBasicBlock block) {
      tracer.getStatistic(TOTAL_COUNT).add(block.getElementCount());
      tracer.getStatistic(CONDITIONAL_COUNT).add(block.getElementCount());
      return false;
    }

    @Override public boolean visit(@Nonnull JReturnBasicBlock block) {
      tracer.getStatistic(TOTAL_COUNT).add(block.getElementCount());
      tracer.getStatistic(RETURN_COUNT).add(block.getElementCount());
      return false;
    }

    @Override public boolean visit(@Nonnull JSwitchBasicBlock block) {
      tracer.getStatistic(TOTAL_COUNT).add(block.getElementCount());
      tracer.getStatistic(SWITCH_COUNT).add(block.getElementCount());
      return false;
    }

    @Override public boolean visit(@Nonnull JThrowingExpressionBasicBlock block) {
      tracer.getStatistic(TOTAL_COUNT).add(block.getElementCount());
      tracer.getStatistic(THROWING_EXPR_COUNT).add(block.getElementCount());
      return false;
    }

    @Override public boolean visit(@Nonnull JThrowBasicBlock block) {
      tracer.getStatistic(TOTAL_COUNT).add(block.getElementCount());
      tracer.getStatistic(THROW_COUNT).add(block.getElementCount());
      return false;
    }
  };

  @Override
  public void run(@Nonnull JBasicBlock block) {
    processor.accept(block);
  }
}
