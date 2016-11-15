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

import com.android.jack.ir.ast.cfg.BasicBlockLiveProcessor;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JConditionalBasicBlock;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JReturnBasicBlock;
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
import com.android.jack.ir.ast.cfg.JThrowingExpressionBasicBlock;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Optimizes primary successor of the conditional block */
@Description("Optimizes primary successor of the conditional block")
@Transform(modify = JControlFlowGraph.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class OptimizeConditionalPrimarySuccessor
    implements RunnableSchedulable<JControlFlowGraph> {

  @Nonnull
  public static final StatisticId<Counter> CONDITIONAL_BLOCKS_OPTIMIZED = new StatisticId<>(
      "jack.cfg.conditional-block-optimized", "Conditional blocks optimized",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public void run(@Nonnull JControlFlowGraph cfg) {
    final TransformationRequest request = new TransformationRequest(cfg);
    new BasicBlockLiveProcessor(cfg, /* stepIntoElements = */ false) {
      @Override
      public boolean visit(@Nonnull JConditionalBasicBlock block) {
        JBasicBlock primary = block.getPrimarySuccessor();
        JBasicBlock alternative = block.getAlternativeSuccessor();

        // If primary block successor is also a primary successor of the
        // alternative successor or its own primary successor, we can
        // invert primary/secondary successors, since it usually result in
        // better code being generated.
        Set<JBasicBlock> blocksInChain = new HashSet<>();
        JBasicBlock pointer = alternative;
        while (pointer != null) {
          if (pointer == primary) {
            if (pointer != alternative) {
              tracer.getStatistic(CONDITIONAL_BLOCKS_OPTIMIZED).incValue();
              block.setInverted(!block.isInverted());
            }
            break;
          } else if (blocksInChain.contains(pointer)) {
            break;
          }

          blocksInChain.add(pointer);
          pointer = getNextPrimary(pointer);
        }

        return false;
      }
    }.process();
    request.commit();
  }

  @CheckForNull
  private JBasicBlock getNextPrimary(@Nonnull JBasicBlock block) {
    if (block instanceof JSimpleBasicBlock) {
      return ((JSimpleBasicBlock) block).getPrimarySuccessor();
    } else if (block instanceof JThrowingExpressionBasicBlock) {
      return ((JThrowingExpressionBasicBlock) block).getPrimarySuccessor();
    } else if (block instanceof JReturnBasicBlock) {
      return ((JReturnBasicBlock) block).getPrimarySuccessor();
    } else {
      return null;
    }
  }
}
