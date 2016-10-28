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
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
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
  public static final StatisticId<Counter> REMOVED_CONST_BRANCHES = new StatisticId<Counter>(
      "jack.cfg.const-branches-removed", "Removed branches with constant condition",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public void run(@Nonnull final JControlFlowGraph cfg) {
    new BasicBlockLiveProcessor(/* stepIntoElements = */ false) {
      @Override
      public boolean visit(@Nonnull JConditionalBasicBlock block) {
        JConditionalBlockElement element =
            (JConditionalBlockElement) block.getLastElement();
        JExpression condition = element.getCondition();

        if (condition instanceof JBooleanLiteral) {
          // Split conditional block: pre-block --> cond-block
          // with pre-block containing all the elements except for
          // the last (conditional) one
          JSimpleBasicBlock preBlock = block.split(-1);

          // Depending on constant value, re-point the pre-block to either
          // if-true or if-false successor of the original conditional block
          boolean isTrueValue = ((JBooleanLiteral) condition).getValue();
          preBlock.replaceAllSuccessors(block,
              isTrueValue ? block.getIfTrue() : block.getIfFalse());

          // Remove all references from the conditional block
          block.dereferenceAllSuccessors();

          tracer.getStatistic(REMOVED_CONST_BRANCHES).incValue();
        }
        return false;
      }

      @Nonnull
      @Override
      public JControlFlowGraph getCfg() {
        return cfg;
      }
    }.process();
  }
}
