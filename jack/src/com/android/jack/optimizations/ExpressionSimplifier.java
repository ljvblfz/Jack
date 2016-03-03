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

import com.android.jack.Options;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCastOperation;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JIntegralConstant32;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JNumberLiteral;
import com.android.jack.ir.ast.JPrefixNotOperation;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JUnaryOperator;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.Number;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.ast.ImplicitCast;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Tag;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class will replace all expressions using constants by evaluating them and replace them
 * by the result of evaluation.
 */
@Description("Expressions simplifier")
@Constraint(no = {ImplicitCast.class, JCastOperation.WithIntersectionType.class},
    need = {ThreeAddressCodeForm.class})
@Transform(add = {ExpressionSimplifier.ExpressionsSimplified.class,
    JPrefixNotOperation.class,
    JBooleanLiteral.class,
    JIntLiteral.class,
    JLongLiteral.class,
    JDoubleLiteral.class,
    JFloatLiteral.class,
    JByteLiteral.class,
    JShortLiteral.class,
    JCharLiteral.class})
@Support(Optimizations.ExpressionSimplifier.class)
public class ExpressionSimplifier implements RunnableSchedulable<JMethod> {

  /**
   * A {@link Tag} meaning that expressions were simplified.
   */
  @Description("Expressions are simplified.")
  public static final class ExpressionsSimplified implements Tag {
  }

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  static class Simplifier extends JVisitor {

    @CheckForNull
    private JMethod currentMethod;

    private final boolean enableNullInstanceOf =
        ThreadConfig.get(Optimizations.ENABLE_NULL_INSTANCEOF).booleanValue();

    @Override
    public boolean visit(@Nonnull JMethod method) {
      currentMethod = method;
      return super.visit(method);
    }

    @Override
    public void endVisit(@Nonnull JCastOperation cast) {
      JExpression castedExpr = cast.getExpr();

      if (castedExpr instanceof JNumberLiteral && cast.getType() instanceof JPrimitiveType) {
        SourceInfo si = cast.getSourceInfo();
        Number numberValue = ((JNumberLiteral) castedExpr).getNumber();
        JValueLiteral simplifiedExpr = refineCst(si, numberValue,
            ((JPrimitiveType) cast.getType()).getPrimitiveTypeEnum());
        assert currentMethod != null;
        TransformationRequest tr = new TransformationRequest(currentMethod);
        tr.append(new Replace(cast, simplifiedExpr));
        tr.commit();
      }
    }

    @Override
    public void endVisit(@Nonnull JConditionalExpression jconditional) {
      JExpression condition = jconditional.getIfTest();
      if (condition instanceof JBooleanLiteral) {
        assert currentMethod != null;
        TransformationRequest tr = new TransformationRequest(currentMethod);
        if (((JBooleanLiteral) condition).getValue()) {
          tr.append(new Replace(jconditional, jconditional.getThenExpr()));
        } else {
          tr.append(new Replace(jconditional, jconditional.getElseExpr()));
        }
        tr.commit();
      }
    }

    @Override
    public void endVisit(@Nonnull JInstanceOf instanceOf) {
      if (enableNullInstanceOf) {
        JExpression expr = instanceOf.getExpr();
        if (expr instanceof JNullLiteral || isCastOfNull(expr)) {
          assert currentMethod != null;
          TransformationRequest tr = new TransformationRequest(currentMethod);
          tr.append(
              new Replace(instanceOf, new JBooleanLiteral(instanceOf.getSourceInfo(), false)));
          tr.commit();
        }
      }
    }

    private boolean isCastOfNull(@Nonnull JExpression expr) {
      if (expr instanceof JCastOperation
          && ((JCastOperation) expr).getExpr() instanceof JNullLiteral) {
        return true;
      }
      return false;
    }

    @Override
    public void endVisit(@Nonnull JUnaryOperation unaryExpr) {
      JExpression simplifiedExpr = null;

      JExpression arg = unaryExpr.getArg();
      if (arg instanceof JValueLiteral) {
        SourceInfo si = unaryExpr.getSourceInfo();
        if (arg instanceof JIntegralConstant32) {
          int value = ((JIntegralConstant32) arg).getIntValue();
          switch (unaryExpr.getOp()) {
            case BIT_NOT: {
              simplifiedExpr = new JIntLiteral(si, ~value);
              break;
            }
            case NEG: {
              simplifiedExpr = new JIntLiteral(si, -value);
              break;
            }
            default: {
              // Nothing to do.
            }
          }
        } else if (arg instanceof JLongLiteral) {
          long value = ((JLongLiteral) arg).getValue();
          switch (unaryExpr.getOp()) {
            case BIT_NOT: {
              simplifiedExpr = new JLongLiteral(si, ~value);
              break;
            }
            case NEG: {
              simplifiedExpr = new JLongLiteral(si, -value);
              break;
            }
            default: {
              // Nothing to do.
            }
          }
        } else if (arg instanceof JDoubleLiteral && unaryExpr.getOp() == JUnaryOperator.NEG) {
          simplifiedExpr = new JDoubleLiteral(si, -((JDoubleLiteral) arg).getValue());
        } else if (arg instanceof JFloatLiteral && unaryExpr.getOp() == JUnaryOperator.NEG) {
          simplifiedExpr = new JFloatLiteral(si, -((JFloatLiteral) arg).getValue());
        } else if (arg instanceof JBooleanLiteral && unaryExpr.getOp() == JUnaryOperator.NOT) {
          simplifiedExpr = new JBooleanLiteral(si, !((JBooleanLiteral) arg).getValue());
        }
      }

      if (simplifiedExpr != null) {
        assert currentMethod != null;
        TransformationRequest tr = new TransformationRequest(currentMethod);
        tr.append(new Replace(unaryExpr, simplifiedExpr));
        tr.commit();
      }

      super.endVisit(unaryExpr);
    }

    @Override
    public void endVisit(@Nonnull JBinaryOperation binaryExpr) {
      JExpression lhs = binaryExpr.getLhs();
      JExpression rhs = binaryExpr.getRhs();
      SourceInfo si = binaryExpr.getSourceInfo();
      JBinaryOperator op = binaryExpr.getOp();
      JExpression simplifiedExpr = null;


      if (lhs instanceof JNullLiteral && rhs instanceof JNullLiteral) {
        switch (op) {
          case NEQ: {
            simplifiedExpr = new JBooleanLiteral(si, false);
            break;
          }
          case EQ: {
            simplifiedExpr = new JBooleanLiteral(si, true);
            break;
          }
          default: {
            throw new AssertionError("Unsupported operator");
          }
        }
      } else if (lhs instanceof JBooleanLiteral && rhs instanceof JBooleanLiteral) {
        boolean lhsValue = ((JBooleanLiteral) lhs).getValue();
        boolean rhsValue = ((JBooleanLiteral) rhs).getValue();
        switch (op) {
          case NEQ: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue != rhsValue);
            break;
          }
          case EQ: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue == rhsValue);
            break;
          }
          case AND: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue && rhsValue);
            break;
          }
          case OR: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue || rhsValue);
            break;
          }
          case BIT_AND: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue & rhsValue);
            break;
          }
          case BIT_OR: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue | rhsValue);
            break;
          }
          case BIT_XOR: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue ^ rhsValue);
            break;
          }
          default: {
            throw new AssertionError("Unsupported operator");
          }
        }
      } else if (lhs instanceof JBooleanLiteral) {
        switch (op) {
          case AND: {
            if (((JBooleanLiteral) lhs).getValue()) {
              simplifiedExpr = rhs;
            } else {
              simplifiedExpr = new JBooleanLiteral(si, false);
            }
            break;
          }
          case OR: {
            if (((JBooleanLiteral) lhs).getValue()) {
              simplifiedExpr = new JBooleanLiteral(si, true);
            }  else {
              simplifiedExpr = rhs;
            }
            break;
          }
          case EQ: {
            if (((JBooleanLiteral) lhs).getValue()) {
              simplifiedExpr = rhs;
            } else if (binaryExpr.getParent() instanceof JIfStatement) {
              simplifiedExpr = new JPrefixNotOperation(si, rhs);
            }
            break;
          }
          case NEQ: {
            if (((JBooleanLiteral) lhs).getValue()) {
              if (binaryExpr.getParent() instanceof JIfStatement) {
                simplifiedExpr = new JPrefixNotOperation(si, rhs);
              }
            } else {
              simplifiedExpr = rhs;
            }
            break;
          }
          case BIT_AND: {
            if (((JBooleanLiteral) lhs).getValue()) {
              simplifiedExpr = rhs;
            }
            break;
          }
          case BIT_OR: {
            if (!((JBooleanLiteral) lhs).getValue()) {
              simplifiedExpr = rhs;
            }
            break;
          }
          case BIT_XOR: {
            if (((JBooleanLiteral) lhs).getValue()) {
              if (binaryExpr.getParent() instanceof JIfStatement) {
                simplifiedExpr = new JPrefixNotOperation(si, rhs);
              }
            } else {
              simplifiedExpr = rhs;
            }
            break;
          }
          default: {
            // Nothing to do.
          }
        }
      } else if (rhs instanceof JBooleanLiteral) {
        switch (op) {
          case AND: {
            if (((JBooleanLiteral) rhs).getValue()) {
              simplifiedExpr = lhs;
            }
            break;
          }
          case OR: {
            if (!((JBooleanLiteral) rhs).getValue()) {
              simplifiedExpr = lhs;
            }
            break;
          }
          case EQ: {
            if (((JBooleanLiteral) rhs).getValue()) {
              simplifiedExpr = lhs;
            } else if (binaryExpr.getParent() instanceof JIfStatement) {
              simplifiedExpr = new JPrefixNotOperation(si, lhs);
            }
            break;
          }
          case NEQ: {
            if (((JBooleanLiteral) rhs).getValue()) {
              if (binaryExpr.getParent() instanceof JIfStatement) {
                simplifiedExpr = new JPrefixNotOperation(si, lhs);
              }
            } else {
              simplifiedExpr = lhs;
            }
            break;
          }
          case BIT_AND: {
            if (((JBooleanLiteral) rhs).getValue()) {
              simplifiedExpr = lhs;
            }
            break;
          }
          case BIT_OR: {
            if (!((JBooleanLiteral) rhs).getValue()) {
              simplifiedExpr = lhs;
            }
            break;
          }
          case BIT_XOR: {
            if (((JBooleanLiteral) rhs).getValue()) {
              if (binaryExpr.getParent() instanceof JIfStatement) {
                simplifiedExpr = new JPrefixNotOperation(si, lhs);
              }
            } else {
              simplifiedExpr = lhs;
            }
            break;
          }
          default: {
            // Nothing to do.
          }
        }
      } else if (lhs instanceof JIntegralConstant32 && rhs instanceof JIntegralConstant32) {
        int lhsValue = ((JIntegralConstant32) lhs).getIntValue();
        int rhsValue = ((JIntegralConstant32) rhs).getIntValue();
        switch (op) {
          case ADD: {
            simplifiedExpr = new JIntLiteral(si, lhsValue + rhsValue);
            break;
          }
          case SUB: {
            simplifiedExpr = new JIntLiteral(si, lhsValue - rhsValue);
            break;
          }
          case MUL: {
            simplifiedExpr = new JIntLiteral(si, lhsValue * rhsValue);
            break;
          }
          case DIV: {
            if (rhsValue != 0) {
              simplifiedExpr = new JIntLiteral(si, lhsValue / rhsValue);
            }
            break;
          }
          case MOD: {
            if (rhsValue != 0) {
              simplifiedExpr = new JIntLiteral(si, lhsValue % rhsValue);
            }
            break;
          }
          case SHL: {
            simplifiedExpr = new JIntLiteral(si, lhsValue << rhsValue);
            break;
          }
          case SHR: {
            simplifiedExpr = new JIntLiteral(si, lhsValue >> rhsValue);
            break;
          }
          case SHRU: {
            simplifiedExpr = new JIntLiteral(si, lhsValue >>> rhsValue);
            break;
          }
          case LT: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue < rhsValue);
            break;
          }
          case LTE: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue <= rhsValue);
            break;
          }
          case GT: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue > rhsValue);
            break;
          }
          case GTE: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue >= rhsValue);
            break;
          }
          case EQ: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue == rhsValue);
            break;
          }
          case NEQ: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue != rhsValue);
            break;
          }
          case BIT_AND: {
            simplifiedExpr = new JIntLiteral(si, lhsValue & rhsValue);
            break;
          }
          case BIT_OR: {
            simplifiedExpr = new JIntLiteral(si, lhsValue | rhsValue);
            break;
          }
          case BIT_XOR: {
            simplifiedExpr = new JIntLiteral(si, lhsValue ^ rhsValue);
            break;
          }
          default: {
            throw new AssertionError("Unsupported operator");
          }
        }
      } else if (lhs instanceof JFloatLiteral && rhs instanceof JFloatLiteral) {
        float lhsValue = ((JFloatLiteral) lhs).getValue();
        float rhsValue = ((JFloatLiteral) rhs).getValue();
        switch (op) {
          case ADD: {
            simplifiedExpr = new JFloatLiteral(si, lhsValue + rhsValue);
            break;
          }
          case SUB: {
            simplifiedExpr = new JFloatLiteral(si, lhsValue - rhsValue);
            break;
          }
          case MUL: {
            simplifiedExpr = new JFloatLiteral(si, lhsValue * rhsValue);
            break;
          }
          case DIV: {
            simplifiedExpr = new JFloatLiteral(si, lhsValue / rhsValue);
            break;
          }
          case MOD: {
            simplifiedExpr = new JFloatLiteral(si, lhsValue % rhsValue);
            break;
          }
          case LT: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue < rhsValue);
            break;
          }
          case LTE: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue <= rhsValue);
            break;
          }
          case GT: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue > rhsValue);
            break;
          }
          case GTE: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue >= rhsValue);
            break;
          }
          case EQ: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue == rhsValue);
            break;
          }
          case NEQ: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue != rhsValue);
            break;
          }
          default: {
            throw new AssertionError("Unsupported operator");
          }
        }
      } else if (lhs instanceof JDoubleLiteral && rhs instanceof JDoubleLiteral) {
        double lhsValue = ((JDoubleLiteral) lhs).getValue();
        double rhsValue = ((JDoubleLiteral) rhs).getValue();
        switch (op) {
          case ADD: {
            simplifiedExpr = new JDoubleLiteral(si, lhsValue + rhsValue);
            break;
          }
          case SUB: {
            simplifiedExpr = new JDoubleLiteral(si, lhsValue - rhsValue);
            break;
          }
          case MUL: {
            simplifiedExpr = new JDoubleLiteral(si, lhsValue * rhsValue);
            break;
          }
          case DIV: {
            simplifiedExpr = new JDoubleLiteral(si, lhsValue / rhsValue);
            break;
          }
          case MOD: {
            simplifiedExpr = new JDoubleLiteral(si, lhsValue % rhsValue);
            break;
          }
          case LT: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue < rhsValue);
            break;
          }
          case LTE: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue <= rhsValue);
            break;
          }
          case GT: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue > rhsValue);
            break;
          }
          case GTE: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue >= rhsValue);
            break;
          }
          case EQ: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue == rhsValue);
            break;
          }
          case NEQ: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue != rhsValue);
            break;
          }
          default: {
            throw new AssertionError("Unsupported operator");
          }
        }
      } else if (lhs instanceof JLongLiteral && rhs instanceof JLongLiteral) {
        long lhsValue = ((JLongLiteral) lhs).getValue();
        long rhsValue = ((JLongLiteral) rhs).getValue();
        switch (op) {
          case ADD: {
            simplifiedExpr = new JLongLiteral(si, lhsValue + rhsValue);
            break;
          }
          case SUB: {
            simplifiedExpr = new JLongLiteral(si, lhsValue - rhsValue);
            break;
          }
          case MUL: {
            simplifiedExpr = new JLongLiteral(si, lhsValue * rhsValue);
            break;
          }
          case DIV: {
            if (rhsValue != 0) {
              simplifiedExpr = new JLongLiteral(si, lhsValue / rhsValue);
            }
            break;
          }
          case MOD: {
            if (rhsValue != 0) {
              simplifiedExpr = new JLongLiteral(si, lhsValue % rhsValue);
            }
            break;
          }
          case SHL: {
            simplifiedExpr = new JLongLiteral(si, lhsValue << rhsValue);
            break;
          }
          case SHR: {
            simplifiedExpr = new JLongLiteral(si, lhsValue >> rhsValue);
            break;
          }
          case SHRU: {
            simplifiedExpr = new JLongLiteral(si, lhsValue >>> rhsValue);
            break;
          }
          case LT: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue < rhsValue);
            break;
          }
          case LTE: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue <= rhsValue);
            break;
          }
          case GT: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue > rhsValue);
            break;
          }
          case GTE: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue >= rhsValue);
            break;
          }
          case EQ: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue == rhsValue);
            break;
          }
          case NEQ: {
            simplifiedExpr = new JBooleanLiteral(si, lhsValue != rhsValue);
            break;
          }
          case BIT_AND: {
            simplifiedExpr = new JLongLiteral(si, lhsValue & rhsValue);
            break;
          }
          case BIT_OR: {
            simplifiedExpr = new JLongLiteral(si, lhsValue | rhsValue);
            break;
          }
          case BIT_XOR: {
            simplifiedExpr = new JLongLiteral(si, lhsValue ^ rhsValue);
            break;
          }
          default: {
            throw new AssertionError("Unsupported operator");
          }
        }
      }

      if (simplifiedExpr != null) {
        assert currentMethod != null;
        TransformationRequest tr = new TransformationRequest(currentMethod);
        tr.append(new Replace(binaryExpr, simplifiedExpr));
        tr.commit();
      }
    }

    @Nonnull
    private JValueLiteral refineCst(@Nonnull SourceInfo si, @Nonnull Number numberValue,
        @Nonnull JPrimitiveTypeEnum destType) {
      switch (destType) {
        case BOOLEAN:
          assert numberValue.byteValue() == 1 || numberValue.byteValue() == 0;
          return (new JBooleanLiteral(si, numberValue.byteValue() == 1 ? true : false));
        case BYTE:
          return (new JByteLiteral(si, numberValue.byteValue()));
        case SHORT:
          return (new JShortLiteral(si, numberValue.shortValue()));
        case CHAR:
          return (new JCharLiteral(si, numberValue.charValue()));
        case INT:
          return (new JIntLiteral(si, numberValue.intValue()));
        case FLOAT:
          return (new JFloatLiteral(si, numberValue.floatValue()));
        case DOUBLE:
          return (new JDoubleLiteral(si, numberValue.doubleValue()));
        case LONG:
          return (new JLongLiteral(si, numberValue.longValue()));
        default:
          throw new AssertionError("Type not supported to refine a constant");
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    Simplifier s = new Simplifier();
    s.accept(method);
  }
}
