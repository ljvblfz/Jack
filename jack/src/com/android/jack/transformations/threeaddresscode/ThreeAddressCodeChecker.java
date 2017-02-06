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

package com.android.jack.transformations.threeaddresscode;

import com.android.jack.Options;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.SanityChecks;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * {@code ThreeAddressCodeChecker} is the visitor checking the three address form.
 */
@Description("Check that tyhe code is in three address form.")
@Name("ThreeAddressCodeChecker")
@Constraint(need = {ThreeAddressCodeForm.class})
@Transform(add = ThreeAddressCodeForm.Checked.class)
@Support(SanityChecks.class)
public class ThreeAddressCodeChecker implements RunnableSchedulable<JMethod>{

  private static class InternalThreeAddressCodeChecker extends JVisitor {

    @Override
    public boolean visit(@Nonnull JExpression expr) {
      if (expr.getParent() instanceof JExpression) {
        throwError(expr);
      }

      if (expr.getParent() instanceof JIfStatement && !isValidExpressionForTac(expr)) {
        throwError(expr);
      }

      if (expr instanceof JBinaryOperation) {
        JBinaryOperation binary = (JBinaryOperation) expr;
        if (!isVariableRef(binary.getLhs()) || !isValidExpressionForTac(binary.getRhs())) {
          throwError(expr);
        }
        return false;
      }

      return super.visit(expr);
    }

    private static void throwError(@Nonnull JExpression expr) {
      final JNode parent = expr.getParent();
      assert parent != null;
      throw new AssertionError(parent.toSource() + " is not three address code.");
    }

    private boolean isValidExpressionForTac(@Nonnull JExpression expr) {
      if (isVariableRef(expr) || expr instanceof JLiteral) {
        return true;
      }

      if (expr instanceof JBinaryOperation) {
        JBinaryOperation binary = (JBinaryOperation) expr;
        if (binary.getOp() == JBinaryOperator.ASG) {
          return false;
        }
        return (isVariableRef(binary.getLhs()) && isVariableRef(binary.getRhs()));
      }

      if (expr instanceof JArrayRef) {
        JArrayRef arrayRef = (JArrayRef) expr;
        return (isVariableRef(arrayRef.getInstance()) && isVariableRef(arrayRef.getIndexExpr()));
      }

      if (expr instanceof JMethodCall) {
        JMethodCall methodCall = (JMethodCall) expr;
        JExpression instance = methodCall.getInstance();
        if (instance != null && !isVariableRef(instance)) {
          return false;
        }
        for (JExpression param :  methodCall.getArgs()) {
          if (!isVariableRef(param)) {
            return false;
          }
        }
        return true;
      }

      return false;
    }

    private boolean isVariableRef(@Nonnull JExpression expr) {
      return (expr instanceof JLocalRef || expr instanceof JParameterRef);
    }
  }

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    InternalThreeAddressCodeChecker tcaBuilder = new InternalThreeAddressCodeChecker();
    tcaBuilder.accept(method);
  }
}
