/*
 * Copyright (C) 2013 The Android Open Source Project
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
import com.android.jack.ir.CompoundAssignment;
import com.android.jack.ir.ast.JArithmeticBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JConcatOperation;
import com.android.jack.ir.ast.JIntegralConstant32;
import com.android.jack.ir.ast.JLogicalAndBitwiseOperation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JShiftOperation;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.types.JIntegralType32;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.ast.ImplicitBoxingAndUnboxing;
import com.android.jack.transformations.ast.ImplicitCast;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.PercentImpl;
import com.android.sched.util.log.stats.StatisticId;

import javax.annotation.Nonnull;

/**
 * Compute number of binary operations using constant value.
 */
@Description("Compute number of binary operations using constant value.")
@Constraint(no = {JConcatOperation.class, ImplicitCast.class, ImplicitBoxingAndUnboxing.class,
    ThreeAddressCodeForm.class, CompoundAssignment.class})
@Support(CodeStats.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class BinaryOperationWithCst implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);
  @Nonnull
  private final CounterVisitor visitor = new CounterVisitor(TracerFactory.getTracer());

  private static class StatBinOp {
    public static final StatisticId<Percent> SHIFT_WITH_LIT8 = new StatisticId<Percent>(
        "jack.operator.shift.lit8", "Shift operation using a lit8",
        PercentImpl.class, Percent.class);

    public static final StatisticId<Percent> LOGICAL_WITH_LIT16 = new StatisticId<Percent>(
        "jack.operator.logical.lit16", "Logical operation using a lit16",
        PercentImpl.class, Percent.class);

    public static final StatisticId<Percent> ARITHMETIC_WITH_LIT16 = new StatisticId<Percent>(
        "jack.operator.arithmetic.1lit16", "Aritmetic operation using a lit16",
        PercentImpl.class, Percent.class);

    public static final StatisticId<Percent> BINARY_WITH_TWO_LITERALS = new StatisticId<Percent>(
        "jack.operator.binary.2lit", "Binary operation using two literals",
        PercentImpl.class, Percent.class);
  }

  private static class CounterVisitor extends JVisitor {
    @Nonnull
    private final Tracer tracer;

    public CounterVisitor(@Nonnull Tracer tracer) {
      this.tracer = tracer;
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binOp) {
      if (binOp instanceof JShiftOperation) {
        computeStat(
            binOp, tracer.getStatistic(StatBinOp.SHIFT_WITH_LIT8), Byte.MIN_VALUE, Byte.MAX_VALUE);
      }

      if (binOp instanceof JLogicalAndBitwiseOperation) {
        computeStat(binOp, tracer.getStatistic(StatBinOp.LOGICAL_WITH_LIT16), Short.MIN_VALUE,
            Short.MAX_VALUE);
      }

      if (binOp instanceof JArithmeticBinaryOperation) {
        computeStat(binOp, tracer.getStatistic(StatBinOp.ARITHMETIC_WITH_LIT16), Short.MIN_VALUE,
            Short.MAX_VALUE);
      }

      return super.visit(binOp);
    }

    private void computeStat(JBinaryOperation binOp, Percent p, int minValue, int maxValue) {
      boolean couldBeOptimize = false;

      if (binOp.getType() instanceof JIntegralType32) {
        if (binOp.getRhs() instanceof JIntegralConstant32) {
          int value = ((JIntegralConstant32) binOp.getRhs()).getIntValue();
          if (value > minValue && value < maxValue) {
            couldBeOptimize = true;
          }
        }
        if (binOp.getLhs() instanceof JIntegralConstant32) {
          int value = ((JIntegralConstant32) binOp.getLhs()).getIntValue();
          if (value > minValue && value < maxValue) {
            couldBeOptimize = true;
          }
        }
      }

      Percent twoCstPercent = tracer.getStatistic(StatBinOp.BINARY_WITH_TWO_LITERALS);
      twoCstPercent.add(
          binOp.getRhs() instanceof JValueLiteral && binOp.getLhs() instanceof JValueLiteral);

      p.add(couldBeOptimize);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    visitor.accept(method);
  }
}
