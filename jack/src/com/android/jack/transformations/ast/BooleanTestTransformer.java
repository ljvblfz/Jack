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

package com.android.jack.transformations.ast;

import com.android.jack.Options;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JConditionalOperation;
import com.android.jack.ir.ast.JEqOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLoop;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JUnaryOperator;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;
/**
 * This {@link RunnableSchedulable} transforms boolean tests that are not in the condition
 * expression of an "if" statement into a JConditional.
 */
@Description("transforms boolean tests that are not in the condition expression" +
             "of an \"if\" statement into a JConditional")
@Transform(add = {JConditionalExpression.class, JBooleanLiteral.class, JEqOperation.class},
    remove = {BooleanTestOutsideIf.class, ThreeAddressCodeForm.class})
@Constraint(no = {JConditionalOperation.class, JLoop.class})
@Filter(TypeWithoutPrebuiltFilter.class)
public class BooleanTestTransformer implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final JMethod method;

    public Visitor(@Nonnull JMethod method) {
      this.method = method;
    }

    @Override
    public void endVisit(@Nonnull JBinaryOperation binOp) {
      switch (binOp.getOp()) {
        case EQ:
        case NEQ:
        case GTE:
        case GT:
        case LTE:
        case LT:
          if (needReplacement(binOp)) {
            SourceInfo sourceInfo = binOp.getSourceInfo();
            JConditionalExpression replacingExpr =
                new JConditionalExpression(sourceInfo, binOp, new JBooleanLiteral(
                    sourceInfo, true), new JBooleanLiteral(sourceInfo, false));
            TransformationRequest request = new TransformationRequest(method);
            request.append(new Replace(binOp, replacingExpr));
            request.commit();
          }
          break;
        default:
          // ignore other cases
          break;
      }
      super.endVisit(binOp);
    }

    private boolean needReplacement(@Nonnull JUnaryOperation unaryOp) {
      return unaryOp.getOp() == JUnaryOperator.NOT
          && !(isIfCondition(unaryOp) || isConditionalCondition(unaryOp));
    }

    private boolean needReplacement(@Nonnull JBinaryOperation binOp) {
      // comparison are supported in JIfstatement and into jConditional.
      return !(isIfCondition(binOp) || isConditionalCondition(binOp));
    }

    private boolean isIfCondition(@Nonnull JExpression expr) {
      JNode parent = expr.getParent();
      return parent instanceof JIfStatement && ((JIfStatement) parent).getIfExpr() == expr;
    }

    private boolean isConditionalCondition(@Nonnull JExpression expr) {
      JNode parent = expr.getParent();
      return parent instanceof JConditionalExpression
          && ((JConditionalExpression) parent).getIfTest() == expr;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    Visitor visitor = new Visitor(method);
    visitor.accept(method);
  }
}
