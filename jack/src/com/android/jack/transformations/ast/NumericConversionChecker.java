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

package com.android.jack.transformations.ast;

import com.android.jack.Options;
import com.android.jack.ir.SideEffectOperation;
import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JCastOperation;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.ir.types.JNumericType;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Check that there are no more implicit casts and boxing/unboxing between numeric types.
 */
@Description("Check that there are no more implicit casts and boxing/unboxing between numeric"
    + " types.")
@Constraint(no = {SideEffectOperation.class, InitInNewArray.class,
    JCastOperation.WithIntersectionType.class})
public class NumericConversionChecker implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  static class Visitor extends JVisitor {

    @Nonnull
    private static final String MISSING_UNBOXING_ERROR = "Missing unboxing";
    @Nonnull
    private static final String MISSING_BOXING_ERROR = "Missing boxing";
    @Nonnull
    private static final String MISSING_CAST_ERROR = "Missing numeric cast";

    @Override
    public void endVisit(@Nonnull JReturnStatement returnStatement) {
      JExpression returnExpr = returnStatement.getExpr();

      if (returnExpr != null) {
        JType expectedType = returnStatement.getParent(JMethod.class).getType();
        checkBoxingOrUnboxing(returnExpr, expectedType);
        checkCast(returnExpr, expectedType);
      }

      super.endVisit(returnStatement);
    }

    @Override
    public void endVisit(@Nonnull JForStatement forStmt) {
      checkUnboxing(forStmt.getTestExpr());
      super.endVisit(forStmt);
    }

    @Override
    public void endVisit(@Nonnull JWhileStatement whileStmt) {
      checkUnboxing(whileStmt.getTestExpr());
      super.endVisit(whileStmt);
    }

    @Override
    public void endVisit(@Nonnull JDoStatement doStmt) {
      checkUnboxing(doStmt.getTestExpr());
      super.endVisit(doStmt);
    }

    @Override
    public void endVisit(@Nonnull JConditionalExpression conditional) {
      checkUnboxing(conditional.getIfTest());

      JExpression thenExpr = conditional.getThenExpr();
      JExpression elseExpr = conditional.getElseExpr();
      JType conditionalType = conditional.getType();
      checkBoxingOrUnboxing(thenExpr, conditionalType);
      checkBoxingOrUnboxing(elseExpr, conditionalType);
      checkCast(thenExpr, conditionalType);
      checkCast(elseExpr, conditionalType);

      super.endVisit(conditional);
    }

    @Override
    public void endVisit(@Nonnull JIfStatement ifStmt) {
      checkUnboxing(ifStmt.getIfExpr());
      super.endVisit(ifStmt);
    }

    @Override
    public void endVisit(@Nonnull JSwitchStatement switchStmt) {
      checkUnboxing(switchStmt.getExpr());
      super.endVisit(switchStmt);
    }

    @Override
    public void endVisit(@Nonnull JDynamicCastOperation cast) {
      checkBoxingOrUnboxing(cast.getExpr(), cast.getType());
      super.endVisit(cast);
    }

    @Override
    public void endVisit(@Nonnull JBinaryOperation binary) {
      JExpression lhs = binary.getLhs();
      JType lhsType = lhs.getType();
      JExpression rhs = binary.getRhs();
      JType rhsType = rhs.getType();

      switch (binary.getOp()) {
        case CONCAT:
        case ASG_CONCAT:
        case ASG_ADD:
        case ASG_DIV:
        case ASG_MOD:
        case ASG_MUL:
        case ASG_SUB:
        case ASG_BIT_AND:
        case ASG_BIT_OR:
        case ASG_BIT_XOR:
        case ASG_SHL:
        case ASG_SHR:
        case ASG_SHRU: {
          // not concerned
          break;
        }
        case ASG: {
          checkBoxingOrUnboxing(rhs, lhsType);

          if (lhsType instanceof JNumericType) {
            checkCast(rhs, lhsType);
          }
          break;
        }
        case SHL:
        case SHR:
        case SHRU: {
          checkUnboxing(lhs);
          checkUnboxing(rhs);
          checkCast(lhs, binary.getType());
          checkCast(rhs, JPrimitiveTypeEnum.INT.getType());
          break;
        }
        case BIT_AND:
        case BIT_OR:
        case BIT_XOR:
        case AND:
        case OR:
        case ADD:
        case DIV:
        case MOD:
        case MUL:
        case SUB: {
          JType expectedType = binary.getType();
          checkUnboxing(lhs);
          checkUnboxing(rhs);
          checkCast(lhs, expectedType);
          checkCast(rhs, expectedType);
          break;
        }
        case GT:
        case GTE:
        case LT:
        case LTE: {
           JType expectedType = JPrimitiveType.getBinaryPromotionType(lhsType, rhsType);
           checkUnboxing(lhs);
           checkUnboxing(rhs);
           checkCast(lhs, expectedType);
           checkCast(rhs, expectedType);
          break;
        }
        case EQ:
        case NEQ: {
        if (lhsType instanceof JNumericType || rhsType instanceof JNumericType) {
          JType expectedType = JPrimitiveType.getBinaryPromotionType(lhsType, rhsType);
          checkUnboxing(lhs);
          checkUnboxing(rhs);
          checkCast(lhs, expectedType);
          checkCast(rhs, expectedType);
        } else if (rhsType == JPrimitiveTypeEnum.BOOLEAN.getType()
            || lhsType == JPrimitiveTypeEnum.BOOLEAN.getType()) {
          checkUnboxing(lhs);
          checkUnboxing(rhs);
        }
          break;
        }
      }

      super.endVisit(binary);
   }

    @Override
    public void endVisit(@Nonnull JFieldInitializer init) {
      JExpression initializer = init.getInitializer();

      JType expectedType = init.getFieldRef().getType();
      checkBoxingOrUnboxing(initializer, expectedType);
      checkCast(initializer, expectedType);

      super.endVisit(init);
    }

    @Override
    public void endVisit(@Nonnull JMethodCall call) {
      List<JExpression> args = call.getArgs();
      List<JType> parameterTypes = call.getMethodId().getParamTypes();
      assert args.size() == parameterTypes.size();
      Iterator<JType> paramTypeIterator = parameterTypes.iterator();
      for (JExpression jExpression : args) {
        JType expectedType = paramTypeIterator.next();
        checkBoxingOrUnboxing(jExpression, expectedType);
        checkCast(jExpression, expectedType);
      }
      super.endVisit(call);
    }

    @Override
    public void endVisit(@Nonnull JNewArray newArray) {

      for (JExpression dimension : newArray.getDims()) {
        if (!(dimension instanceof JAbsentArrayDimension)) {
          if (dimension.getType() instanceof JPrimitiveType) {
            checkCast(dimension, JPrimitiveTypeEnum.INT.getType());
          } else {
            throw new AssertionError(MISSING_UNBOXING_ERROR);
          }
        }
      }

      assert newArray.getInitializers().isEmpty() || newArray.hasConstantInitializer();

      super.endVisit(newArray);
    }

    @Override
    public void endVisit(@Nonnull JArrayRef arrayRef) {
      JExpression indexExpr = arrayRef.getIndexExpr();

      checkUnboxing(indexExpr);

      checkCast(indexExpr, JPrimitiveTypeEnum.INT.getType());

      super.endVisit(arrayRef);
    }

    @Override
    public void endVisit(@Nonnull JUnaryOperation unary) {
      switch (unary.getOp()) {
        case DEC:
        case INC:
        case NOT:
        case BIT_NOT:
        case NEG: {
          checkUnboxing(unary.getArg());
          checkCast(unary.getArg(), unary.getType());
          break;
        }
      }

      super.endVisit(unary);
    }

    private void checkUnboxing(@Nonnull JExpression expr) {
      if (!(expr.getType() instanceof JPrimitiveType)) {
        throw new AssertionError(MISSING_UNBOXING_ERROR);
      }
    }

    private void checkBoxingOrUnboxing(
        @Nonnull JExpression expr, @Nonnull JType expectedType) {
      JType type = expr.getType();
      if (!(expectedType instanceof JPrimitiveType) && (type instanceof JPrimitiveType)) {
        throw new AssertionError(MISSING_BOXING_ERROR);
      } else if ((expectedType instanceof JPrimitiveType) && !(type instanceof JPrimitiveType)) {
        throw new AssertionError(MISSING_UNBOXING_ERROR);
      }
    }

    private void checkCast(@Nonnull JExpression exprToCast, @Nonnull JType expectedType) {
      if (expectedType instanceof JNumericType && !exprToCast.getType().isSameType(expectedType)) {
        throw new AssertionError(MISSING_CAST_ERROR);
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    Visitor visitor = new Visitor();
    visitor.accept(method);
  }

}
