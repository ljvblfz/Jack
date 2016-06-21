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

package com.android.jack.optimizations.common;

import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Base class for literal value trackers */
public class LiteralValueTrackerBase {
  /** Special unique value to represent multiple values or a complex value */
  @Nonnull
  private static final JValueLiteral NON_LITERAL_OR_MULTIPLE_VALUE =
      new JValueLiteral(SourceInfo.UNKNOWN) {
        @Override public boolean isTypeValue() {
          return false;
        }

        @Override public JType getType() {
          throw new AssertionError();
        }

        @Override public void traverse(@Nonnull JVisitor visitor) {
          throw new AssertionError();
        }

        @Override public void traverse(
            @Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
          throw new AssertionError();
        }

        @Override
        public void visit(@Nonnull JVisitor visitor,
            @Nonnull TransformRequest transformRequest) throws Exception {
          throw new AssertionError();
        }
      };

  /** Represent the value of the expression as a literal */
  @Nonnull
  final JValueLiteral asLiteral(@Nonnull JExpression expression) {
    JValueLiteral result =
        OptimizerUtils.asLiteralOrDefault(expression, NON_LITERAL_OR_MULTIPLE_VALUE);
    assert result != null;
    return result;
  }

  /** Returns true if the value is not null and represents multiple or non-literal expression */
  final boolean isMultipleOrNonLiteralValue(@CheckForNull JValueLiteral literal) {
    return literal == NON_LITERAL_OR_MULTIPLE_VALUE;
  }

  /** Returns the value representing multiple or non-literal expression */
  @Nonnull
  final JValueLiteral getMultipleOrNonLiteralValue() {
    return NON_LITERAL_OR_MULTIPLE_VALUE;
  }
}
