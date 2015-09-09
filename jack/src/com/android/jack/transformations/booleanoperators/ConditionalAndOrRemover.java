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
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JConditionalOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.optimizations.NotSimplifier.NotExpressionsSimplified;
import com.android.jack.transformations.booleanoperators.FallThroughMarker.FallThroughEnum;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;


/**
 * This {@link RunnableSchedulable} replaces complex boolean expression with {@code ||} and
 * {@code &&} by an equivalent {@link JConditionalExpression} which is to be removed afterwards by
 * another {@link RunnableSchedulable}.
 */
@Description("Removes conditional operator && and ||.")
@Name("ConditionalAndOrRemover")
@Constraint(need = {JConditionalOperation.class, NotExpressionsSimplified.class})
@Transform(add = {JConditionalExpression.class, JBooleanLiteral.class, FallThroughMarker.class},
    remove = {JConditionalOperation.class, ThreeAddressCodeForm.class})
public class ConditionalAndOrRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class BooleanExpressionSimplifierVisitor extends JVisitor {

    @Nonnull
    private final TransformationRequest transformationRequest;

    public BooleanExpressionSimplifierVisitor(
        @Nonnull TransformationRequest transformationRequest) {
      this.transformationRequest = transformationRequest;
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binOp) {
      JConditionalExpression replacingExpr;
      SourceInfo sourceInfo = binOp.getSourceInfo();
      JExpression lhs = binOp.getLhs();
      switch (binOp.getOp()) {
        case AND:
          replacingExpr =
              new JConditionalExpression(sourceInfo, lhs, binOp.getRhs(), new JBooleanLiteral(
                  sourceInfo, false));
          transformationRequest.append(new Replace(binOp, replacingExpr));
          break;
        case OR:
          replacingExpr =
              new JConditionalExpression(sourceInfo, lhs, new JBooleanLiteral(sourceInfo, true),
                  binOp.getRhs());
          replacingExpr.addMarker(new FallThroughMarker(FallThroughEnum.ELSE));
          transformationRequest.append(new Replace(binOp, replacingExpr));
          break;
        default:
          // ignore other cases
          break;
      }
      return super.visit(binOp);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest request = new TransformationRequest(method);
    BooleanExpressionSimplifierVisitor besv =
        new BooleanExpressionSimplifierVisitor(request);
    besv.accept(method);
    request.commit();
  }
}