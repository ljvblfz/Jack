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

import com.google.common.collect.Sets;

import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.analysis.UseDefsMarker;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.List;
import java.util.Set;
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
    if (expression instanceof JValueLiteral) {
      return (JValueLiteral) expression;
    }

    // The value may be a reference to a synthetic local created to hold
    // the actual value, we unroll such assignment chain in some simple cases.
    Set<JLocal> localsSeen = Sets.newIdentityHashSet();
    while (expression instanceof JLocalRef) {
      JLocal local = ((JLocalRef) expression).getLocal();
      if (!local.isSynthetic() || localsSeen.contains(local)) {
        break;
      }
      localsSeen.add(local);

      UseDefsMarker usedRefs = expression.getMarker(UseDefsMarker.class);
      if (usedRefs == null) {
        break;
      }
      List<DefinitionMarker> defs = usedRefs.getDefs();
      if (defs.size() != 1) {
        break;
      }
      expression = defs.get(0).getValue();
    }

    // If the expression is NOT a simple value literal, let's mark it
    return expression instanceof JValueLiteral ?
        (JValueLiteral) expression : NON_LITERAL_OR_MULTIPLE_VALUE;
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
