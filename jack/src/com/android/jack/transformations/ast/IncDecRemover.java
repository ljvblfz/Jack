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
import com.android.jack.ir.ast.JAbstractMethodBody;
import com.android.jack.ir.ast.JAddOperation;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMultiExpression;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPostfixDecOperation;
import com.android.jack.ir.ast.JPostfixIncOperation;
import com.android.jack.ir.ast.JPostfixOperation;
import com.android.jack.ir.ast.JPrefixDecOperation;
import com.android.jack.ir.ast.JPrefixIncOperation;
import com.android.jack.ir.ast.JPrefixOperation;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JSubOperation;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Remove increment and decrement
 */
@Description("Remove increment and decrement.")
@Name("IncDecRemover")
@Transform(add = {JAsgOperation.class, JAddOperation.class, JSubOperation.class,
    JMultiExpression.class, JArrayRef.class, JLocalRef.class, JIntLiteral.class,
    JFieldRef.class, JParameterRef.class, JDynamicCastOperation.class},
    remove = {JPrefixDecOperation.class, JPrefixIncOperation.class,
    JPostfixDecOperation.class, JPostfixIncOperation.class, ThreeAddressCodeForm.class})
@Use({LocalVarCreator.class, SideEffectExtractor.class})
public class IncDecRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class IncDecRemoverVisitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    @CheckForNull
    private SideEffectExtractor extractor;

    @CheckForNull
    private LocalVarCreator lvCreator;

    private IncDecRemoverVisitor(@Nonnull TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public boolean visit(@Nonnull JMethod method) {
      JAbstractMethodBody body = method.getBody();
      if (body != null && body instanceof JMethodBody) {
        lvCreator = new LocalVarCreator(method, "idr");
        extractor = new SideEffectExtractor(lvCreator);
      }
      return super.visit(method);
    }

    @Override
    public void endVisit(@Nonnull JMethod x) {
      lvCreator = null;
      extractor = null;
      super.endVisit(x);
    }

    @Override
    public boolean visit(@Nonnull JUnaryOperation unary) {
      JBinaryOperator newOperator = null;

      switch (unary.getOp()) {
        case DEC:
          newOperator = JBinaryOperator.SUB;
          break;
        case INC:
          newOperator = JBinaryOperator.ADD;
          break;
        case BIT_NOT:
        case NEG:
        case NOT:
          break;
      }

      // ++a => a = a + 1
      // --a => a = a - 1
      // a++ => ($idr0 = a, a = $idr0 + 1, $idr0)
      // a-- => ($idr0 = a, a = $idr0 - 1, $idr0)
      if (newOperator != null) {
        SourceInfo sourceInfo = unary.getSourceInfo();
        JType binaryType = unary.getType();
        ArrayList<JExpression> exprs = new ArrayList<JExpression>();

        assert extractor != null;
        JExpression argCopy = extractor.copyWithoutSideEffects(unary.getArg(), tr);

        if (unary instanceof JPostfixOperation) {
          // $idr0 = a
          assert lvCreator != null;
          JLocal idr0 = lvCreator.createTempLocal(binaryType, sourceInfo, tr);
          JLocalRef part1Lhs = new JLocalRef(sourceInfo, idr0);
          JAsgOperation part1 = new JAsgOperation(sourceInfo, part1Lhs, unary.getArg());
          exprs.add(part1);

          // a = $idr0 newOperator 1
          JBinaryOperation part2Rhs =
              JBinaryOperation.create(sourceInfo, newOperator, new JLocalRef(
                  sourceInfo, idr0), new JIntLiteral(sourceInfo, 1));
          JAsgOperation part2 = new JAsgOperation(sourceInfo, argCopy, part2Rhs);
          exprs.add(part2);

          // $idr0
          JLocalRef part3 = new JLocalRef(sourceInfo, idr0);
          exprs.add(part3);
          JMultiExpression me = new JMultiExpression(sourceInfo, exprs);
          tr.append(new Replace(unary, me));
        } else {
          assert unary instanceof JPrefixOperation : "Not yet supported";

          // a = a newOperator 1 or a = (type of a) (a newOperator 1)
          JExpression rhs = JBinaryOperation.create(
              sourceInfo, newOperator, argCopy, new JIntLiteral(sourceInfo, 1));
          JType unaryArgType = unary.getArg().getType();
          if (unaryArgType == JPrimitiveTypeEnum.BYTE.getType() ||
              unaryArgType == JPrimitiveTypeEnum.SHORT.getType() ||
              unaryArgType == JPrimitiveTypeEnum.CHAR.getType()) {
            rhs = new JDynamicCastOperation(sourceInfo, rhs, unaryArgType);
          }
          JAsgOperation newExpr = new JAsgOperation(sourceInfo, unary.getArg(), rhs);
          tr.append(new Replace(unary, newExpr));
        }
      }

      return super.visit(unary);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    IncDecRemoverVisitor rca = new IncDecRemoverVisitor(tr);
    rca.accept(method);
    tr.commit();
  }
}
