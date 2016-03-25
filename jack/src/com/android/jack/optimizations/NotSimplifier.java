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

package com.android.jack.optimizations;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.JAndOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JBitAndOperation;
import com.android.jack.ir.ast.JBitOrOperation;
import com.android.jack.ir.ast.JEqOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JGtOperation;
import com.android.jack.ir.ast.JGteOperation;
import com.android.jack.ir.ast.JLtOperation;
import com.android.jack.ir.ast.JLteOperation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNeqOperation;
import com.android.jack.ir.ast.JOrOperation;
import com.android.jack.ir.ast.JPrefixNotOperation;
import com.android.jack.ir.ast.JPrimitiveType.JBooleanType;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JUnaryOperator;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.UnsupportedOperatorException;
import com.android.jack.ir.types.JFloatingPointType;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Simplify '!' operator when it is valuable.
 */
@Description("Simplify '!' operator when it is valuable")
@Constraint(need = {JPrefixNotOperation.class}, no = {ThreeAddressCodeForm.class})
@Transform(add = {JPrefixNotOperation.class, JGteOperation.class, JGtOperation.class,
    JLteOperation.class, JLtOperation.class, JEqOperation.class, JNeqOperation.class,
    JAndOperation.class, JOrOperation.class, JBitAndOperation.class, JBitOrOperation.class})
@Filter(SourceTypeFilter.class)
public class NotSimplifier implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  /**
   * Count number of operators before and after that the transformation will be apply to check if it
   * is valuable or not.
   */
  private static class CountOperatorAfterRemoval extends JVisitor {

    // Start to 1 since the outer ! should be count before the transformation and not after since
    // it will be removed, except in one case if binary operator can not be reverse.
    @Nonnegative
    private int opBeforeTransformation = 1;
    @Nonnegative
    private int opAfterTransformation = 0;

    @Override
    public boolean visit(@Nonnull JExpression expr) {
      assert expr.getType() instanceof JBooleanType || expr.getType().isSameType(
          Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN));
      opAfterTransformation++;
      return false;
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binaryOp) {
      assert binaryOp.getType() instanceof JBooleanType || binaryOp.getType().isSameType(
          Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN));
      opBeforeTransformation++;
      JBinaryOperator op = binaryOp.getOp();

      if ((op.isComparison() && !useFloatingTypes(binaryOp))
          || op.isConditionalOperation()
          || op == JBinaryOperator.BIT_AND
          || op == JBinaryOperator.BIT_OR) {
        // Operator will be inverse, thus it exists before and after the transformation.
        opAfterTransformation++;
      } else {
        assert op == JBinaryOperator.ASG
            || op == JBinaryOperator.BIT_XOR
            || op.isComparison() && useFloatingTypes(binaryOp);
        // Operator could not be inverse, thus it always exists after transformation and first
        // enclosing '!' should also be added.
        opAfterTransformation += 2;
      }

      return binaryOp.getOp().isConditionalOperation() || op == JBinaryOperator.BIT_AND
          || op == JBinaryOperator.BIT_OR;
    }

    @Override
    public boolean visit(@Nonnull JUnaryOperation unaryOp) {
      assert unaryOp.getOp() == JUnaryOperator.NOT;
      opBeforeTransformation++;
      // This '!' operator will disappear, do not add it in countOpAfter.
      return false;
    }

    private boolean useFloatingTypes(@Nonnull JBinaryOperation binaryOp) {
      return binaryOp.getLhs().getType() instanceof JFloatingPointType ||
          binaryOp.getRhs().getType() instanceof JFloatingPointType;
    }
  }

  /**
   * Reverse expression to remove '!' operator and decrease total number of operators.
   */
  private static class ReverseNotExpression extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    public ReverseNotExpression(@Nonnull TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public boolean visit(@Nonnull JExpression expr) {
      assert expr.getType() instanceof JBooleanType || expr.getType().isSameType(
          Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN));
      tr.append(new Replace(expr, new JPrefixNotOperation(expr.getSourceInfo(), expr)));
      return false;
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binaryOp) {
      assert binaryOp.getType() instanceof JBooleanType || binaryOp.getType().isSameType(
          Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_BOOLEAN));
      JBinaryOperator op = binaryOp.getOp();

      if (op.isComparison() || op.isConditionalOperation()
          || op == JBinaryOperator.BIT_AND
          || op == JBinaryOperator.BIT_OR) {
        try {
          tr.append(new Replace(binaryOp, JBinaryOperation.create(binaryOp.getSourceInfo(),
              binaryOp.getOp().getReverseOperator(), binaryOp.getLhs(),
              binaryOp.getRhs())));
        } catch (UnsupportedOperatorException e) {
          throw new JNodeInternalError("Failures into not simplifier", e);
        }
      }

      // Continue to inverse operator only if it is '&&', '||', '|', '&' otherwise stop inversion.
      return binaryOp.getOp().isConditionalOperation() || op == JBinaryOperator.BIT_AND
          || op == JBinaryOperator.BIT_OR;
    }

    @Override
    public boolean visit(@Nonnull JUnaryOperation unaryOp) {
      assert unaryOp.getOp() == JUnaryOperator.NOT;
      tr.append(new Replace(unaryOp, unaryOp.getArg()));
      return false;
    }
  }

  private static class NotSimplifierVisitor extends JVisitor {

    @Nonnull
    private final JMethod method;

    public NotSimplifierVisitor(@Nonnull JMethod method) {
      this.method = method;
    }

    @Override
    public boolean visit(@Nonnull JUnaryOperation unaryOp) {
      boolean deep = true;

      if (unaryOp.getOp() == JUnaryOperator.NOT) {
        CountOperatorAfterRemoval countOp = new CountOperatorAfterRemoval();
        countOp.accept(unaryOp.getArg());
        if (countOp.opAfterTransformation < countOp.opBeforeTransformation) {
          TransformationRequest tr = new TransformationRequest(method);
          tr.append(new Replace(unaryOp, unaryOp.getArg()));
          ReverseNotExpression reverse = new ReverseNotExpression(tr);
          reverse.accept(unaryOp.getArg());
          tr.commit();
          deep = false;
        }
      }

      return deep;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    NotSimplifierVisitor notRemover = new NotSimplifierVisitor(method);
    notRemover.accept(method);
  }

}
