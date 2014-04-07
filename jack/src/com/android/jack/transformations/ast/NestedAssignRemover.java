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
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Replaces {@link JAsgOperation} into {@link JExpression} or
 * {@link com.android.jack.ir.ast.JStatement JStatement} by an
 * {@link JMultiExpression} to remove read/write of the same expression.
 */
@Description("Replaces assign into expression or statement by multi expression to remove read/write"
    + " of the same expression.")
@Name("NestedAssignRemover")
@Constraint(need = JAsgOperation.class)
@Transform(add = {JMultiExpression.class, JLocalRef.class, JAsgOperation.NonReusedAsg.class},
    remove = {JAsgOperation.class, ThreeAddressCodeForm.class})
@Use(LocalVarCreator.class)
public class NestedAssignRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final JMethod method;

    @Nonnull
    private final LocalVarCreator localVarCreator;

    public Visitor(JMethod method) {
      this.method = method;
      localVarCreator = new LocalVarCreator(method, "nestedAsg");
    }

    @Override
    public void endVisit(@Nonnull JBinaryOperation binOp) {
      if (binOp.getOp() == JBinaryOperator.ASG
          && binOp.isResultUsed()) {
          // a = b => (t = b, a = t, t)
          SourceInfo sourceInfo = binOp.getSourceInfo();

          TransformationRequest tr = new TransformationRequest(method);
          JExpression rhs = binOp.getRhs();
          JType rhsType = rhs.getType();

          JLocal tmp = localVarCreator.createTempLocal(rhsType, sourceInfo, tr);

          List<JExpression> exprs = new LinkedList<JExpression>();

          // t = b
          JAsgOperation asg1 = new JAsgOperation(sourceInfo,
              new JLocalRef(sourceInfo, tmp), rhs);
          exprs.add(asg1);
          // a = t
          JAsgOperation asg2 = new JAsgOperation(sourceInfo,
              binOp.getLhs(), new JLocalRef(sourceInfo, tmp));
          exprs.add(asg2);
          // t
          exprs.add(new JLocalRef(sourceInfo, tmp));

          tr.append(new Replace(binOp, new JMultiExpression(sourceInfo, exprs)));
          tr.commit();
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    JDefinedClassOrInterface enclosingType = method.getEnclosingType();
    if (enclosingType.isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    Visitor visitor = new Visitor(method);
    visitor.accept(method);
  }
}
