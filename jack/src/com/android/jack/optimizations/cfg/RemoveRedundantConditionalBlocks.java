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

package com.android.jack.optimizations.cfg;

import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.cfg.BasicBlockLiveProcessor;
import com.android.jack.ir.ast.cfg.JConditionalBasicBlock;
import com.android.jack.ir.ast.cfg.JConditionalBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JGotoBlockElement;
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
import com.android.jack.ir.ast.cfg.mutations.BasicBlockBuilder;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import javax.annotation.Nonnull;

/** Remove all conditional basic with constant condition */
@Description("Remove all conditional basic with constant condition")
@Transform(modify = JControlFlowGraph.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class RemoveRedundantConditionalBlocks
    implements RunnableSchedulable<JControlFlowGraph> {

  @Nonnull
  public static final StatisticId<Counter> REMOVED_CONST_BRANCHES = new StatisticId<>(
      "jack.cfg.const-branches-removed", "Removed branches with constant condition",
      CounterImpl.class, Counter.class);

  @Nonnull
  public static final StatisticId<Counter> REMOVED_REDUNDANT_CONDITIONS = new StatisticId<>(
      "jack.cfg.redundant-conditions-removed", "Removed redundant conditional blocks",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public void run(@Nonnull final JControlFlowGraph cfg) {
    new BasicBlockLiveProcessor(cfg, /* stepIntoElements = */ false) {
      @Override
      public boolean visit(@Nonnull JConditionalBasicBlock block) {
        JConditionalBlockElement element =
            (JConditionalBlockElement) block.getLastElement();
        JExpression condition = element.getCondition();

        if (block.getIfFalse() == block.getIfTrue()) {
          // Conditional block is redundant, replace it with a simple basic block

          // Note that goto block element created reuses source
          // info of the original conditional block element
          JSimpleBasicBlock simple = new BasicBlockBuilder(cfg)
              .append(block).removeLast()
              .append(new JGotoBlockElement(
                  element.getSourceInfo(), element.getEHContext()))
              .createSimpleBlock(block.getIfTrue());

          // Replace conditional block with newly created simple block
          block.detach(simple);

          tracer.getStatistic(REMOVED_REDUNDANT_CONDITIONS).incValue();

        } else if (condition instanceof JBooleanLiteral) {
          // Split conditional block: pre-block --> cond-block
          // with pre-block containing all the elements except for
          // the last (conditional) one
          block.split(-1);

          // Detach the conditional block and replace it with either
          // if-true or if-false successor depending on the constant value
          block.detach(((JBooleanLiteral) condition).getValue()
              ? block.getIfTrue() : block.getIfFalse());

          tracer.getStatistic(REMOVED_CONST_BRANCHES).incValue();
        }
        return false;
      }
    }.process();
  }
}
