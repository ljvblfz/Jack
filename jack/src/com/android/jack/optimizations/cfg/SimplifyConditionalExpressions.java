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

import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JIntegralConstant32;
import com.android.jack.ir.ast.JRelationalOperation;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.UnsupportedOperatorException;
import com.android.jack.ir.ast.cfg.BasicBlockLiveProcessor;
import com.android.jack.ir.ast.cfg.JConditionalBasicBlock;
import com.android.jack.ir.ast.cfg.JConditionalBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Replace;
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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Simplifies conditional expressions */
@Description("Simplifies conditional expressions")
@Transform(modify = JControlFlowGraph.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class SimplifyConditionalExpressions
    implements RunnableSchedulable<JControlFlowGraph> {

  @Nonnull
  public static final StatisticId<Counter> CONDITIONS_SIMPLIFIED = new StatisticId<>(
      "jack.cfg.conditions-simplified", "Simplified condition expressions",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private JExpression createZeroLiteral(@Nonnull JValueLiteral original) {
    return original.getType().createDefaultValue(original.getSourceInfo());
  }

  @CheckForNull
  private JBinaryOperation optimizeOperation(
      @Nonnull SourceInfo si, @Nonnull JBinaryOperator op,
      @Nonnull JExpression lhs, @Nonnull JIntegralConstant32 rhs) {

    int value = rhs.getIntValue();
    if (value == 1) {
      if (op == JBinaryOperator.GTE) {
        // expr >= 1 ---> expr > 0
        return JBinaryOperation.create(si,
            JBinaryOperator.GT, lhs, createZeroLiteral((JValueLiteral) rhs));
      }
      if (op == JBinaryOperator.LT) {
        // expr < 1 ---> expr <= 0
        return JBinaryOperation.create(si,
            JBinaryOperator.LTE, lhs, createZeroLiteral((JValueLiteral) rhs));
      }

    } else if (value == -1) {
      if (op == JBinaryOperator.LTE) {
        // expr <= -1 ---> expr <  0
        return JBinaryOperation.create(si,
            JBinaryOperator.LT, lhs, createZeroLiteral((JValueLiteral) rhs));
      }
      if (op == JBinaryOperator.GT) {
        // expr > -1 ---> expr >= 0
        return JBinaryOperation.create(si,
            JBinaryOperator.GTE, lhs, createZeroLiteral((JValueLiteral) rhs));
      }
    }
    return null;
  }

  @Override
  public void run(@Nonnull final JControlFlowGraph cfg) {
    final TransformationRequest request = new TransformationRequest(cfg);

    new BasicBlockLiveProcessor(cfg, /* stepIntoElements = */ false) {
      @Override
      public boolean visit(@Nonnull JConditionalBasicBlock block) {
        JConditionalBlockElement element =
            (JConditionalBlockElement) block.getLastElement();
        JExpression condition = element.getCondition();

        if (condition instanceof JRelationalOperation) {
          JBinaryOperation relation = (JRelationalOperation) condition;
          JBinaryOperator op = relation.getOp();
          assert op == JBinaryOperator.LT || op == JBinaryOperator.LTE ||
              op == JBinaryOperator.GT || op == JBinaryOperator.GTE;

          JExpression lhs = relation.getLhs();
          JExpression rhs = relation.getRhs();

          JBinaryOperation newRelation = null;

          if (rhs instanceof JIntegralConstant32) {
            newRelation = optimizeOperation(
                relation.getSourceInfo(), op, lhs, (JIntegralConstant32) rhs);
          } else if (lhs instanceof JIntegralConstant32) {
            try {
              newRelation = optimizeOperation(relation.getSourceInfo(),
                  op.getReverseOperator(), rhs, (JIntegralConstant32) lhs);
            } catch (UnsupportedOperatorException e) {
              throw new AssertionError();
            }
          }

          if (newRelation != null) {
            tracer.getStatistic(CONDITIONS_SIMPLIFIED).incValue();
            request.append(new Replace(relation, newRelation));
          }
        }
        return false;
      }
    }.process();
    request.commit();
  }

}
