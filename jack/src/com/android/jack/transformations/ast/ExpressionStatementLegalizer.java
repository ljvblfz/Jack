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
import com.android.jack.ir.SideEffectOperation;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Stores expressions contained in a {@code JExpressionStatement} in a local variable
 */
@Description("Stores expressions contained in a JExpressionStatement in a local variable")
@Transform(add = {JAsgOperation.NonReusedAsg.class, JLocalRef.class},
  remove = UnassignedValues.class)
@Constraint(no = {SideEffectOperation.class})
@Use(LocalVarCreator.class)
public class ExpressionStatementLegalizer implements RunnableSchedulable<JMethod> {
  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {
    @Nonnull
    private final TransformationRequest tr;

    @Nonnull
    private final LocalVarCreator lvCreator;

    public Visitor(@Nonnull TransformationRequest tr, @Nonnull JMethod method) {
      this.tr = tr;
      lvCreator = new LocalVarCreator(method, "all");
    }

    private boolean isLegal(@Nonnull JExpression expr) {
      if (expr.getType() == JPrimitiveTypeEnum.VOID.getType()) {
        return true;
      }
      return (expr instanceof JAsgOperation
          || expr instanceof JVariableRef
          || expr instanceof JMethodCall);
    }

    @Override
    public void endVisit(@Nonnull JExpressionStatement exprSt) {
      JExpression expr = exprSt.getExpr();
      JType type = expr.getType();
      if (!isLegal(expr)) {
        SourceInfo sourceInfo = exprSt.getSourceInfo();
        JLocal lv = lvCreator.createTempLocal(type, sourceInfo, tr);
        JAsgOperation asg = new JAsgOperation(sourceInfo, new JLocalRef(sourceInfo, lv), expr);
        tr.append(new Replace(expr, asg));
      }
      super.endVisit(exprSt);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(tr, method);
    visitor.accept(method);
    tr.commit();
  }
}
