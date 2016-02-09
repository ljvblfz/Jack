/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.transformations.booleanoperators;

import com.android.jack.Options;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JConditionalOperation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.SanityChecks;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * {@code ConditionalAndOrRemoverChecker} is the visitor that checks that conditional
 * boolean operators && and || have been removed.
 */
@Description("Checks that conditional boolean operators && and || have been removed")
@Constraint(no = {JConditionalOperation.class})
@Support(SanityChecks.class)
public class ConditionalAndOrRemoverChecker implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class ConditionalAndOrRemoverCheckerVisitor extends JVisitor{

    @Override
    public boolean visit(@Nonnull JBinaryOperation binOp) {
      super.visit(binOp);
      if (binOp.getOp() == JBinaryOperator.AND || binOp.getOp() == JBinaryOperator.OR) {
        throw new AssertionError("Conditional operator found : " + binOp.toSource());
      }
      return true;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    ConditionalAndOrRemoverCheckerVisitor bescv = new ConditionalAndOrRemoverCheckerVisitor();
    bescv.accept(method);
  }
}
