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

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ir.SideEffectOperation;
import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JCastOperation;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JUnaryOperation;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.types.JIntegralType32;
import com.android.jack.ir.types.JNumericType;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.collect.Lists;
import com.android.sched.util.config.ThreadConfig;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Make implicit casting, boxing and unboxing become explicit.
 */
@Description("Make implicit casting, boxing and unboxing become explicit.")
@Name("TypeLegalizer")
@Constraint(
    no = {SideEffectOperation.class, InitInNewArray.class, JSwitchStatement.SwitchWithEnum.class,
        JCastOperation.WithIntersectionType.class, JSwitchStatement.SwitchWithString.class})
@Transform(add = {JMethodCall.class, JDynamicCastOperation.class},
    remove = {ImplicitCast.class, ImplicitBoxingAndUnboxing.class, ThreeAddressCodeForm.class})
@Filter(TypeWithoutPrebuiltFilter.class)
// Use getOrCreateMethodIdWide that scan hierarchy.
@Access(JSession.class)
public class TypeLegalizer implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final JClass javaLangObject =
      Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT);

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  static class TypeLegalizerVisitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    TypeLegalizerVisitor(@Nonnull TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public void endVisit(@Nonnull JReturnStatement returnStatement) {
      JExpression returnExpr = returnStatement.getExpr();

      if (returnExpr != null) {
        JType expectedType = returnStatement.getParent(JMethod.class).getType();
        castIfNeeded(maybeBoxOrUnbox(returnExpr, expectedType), expectedType);
      }

      super.endVisit(returnStatement);
    }

    @Override
    public void endVisit(@Nonnull JForStatement forStmt) {
      maybeUnbox(forStmt.getTestExpr());
      super.endVisit(forStmt);
    }

    @Override
    public void endVisit(@Nonnull JWhileStatement whileStmt) {
      maybeUnbox(whileStmt.getTestExpr());
      super.endVisit(whileStmt);
    }

    @Override
    public void endVisit(@Nonnull JDoStatement doStmt) {
      maybeUnbox(doStmt.getTestExpr());
      super.endVisit(doStmt);
    }

    @Override
    public void endVisit(@Nonnull JConditionalExpression conditional) {
      maybeUnbox(conditional.getIfTest());

      JType conditionalType = conditional.getType();

      castIfNeeded(maybeBoxOrUnbox(conditional.getThenExpr(), conditionalType), conditionalType);

      castIfNeeded(maybeBoxOrUnbox(conditional.getElseExpr(), conditionalType), conditionalType);

      super.endVisit(conditional);
    }

    @Override
    public void endVisit(@Nonnull JIfStatement ifStmt) {
      maybeUnbox(ifStmt.getIfExpr());
      super.endVisit(ifStmt);
    }

    @Override
    public void endVisit(@Nonnull JSwitchStatement switchStmt) {
      maybeUnbox(switchStmt.getExpr());
      super.endVisit(switchStmt);
    }

    @Override
    public void endVisit(@Nonnull JDynamicCastOperation cast) {
      JExpression expr = cast.getExpr();
      if (needNarrowing(cast.getExpr().getType()) && cast.getType() instanceof JPrimitiveType) {
        assert cast.getType() != JPrimitiveTypeEnum.VOID.getType();

        JDynamicCastOperation castToWrapperType = new JDynamicCastOperation(expr.getSourceInfo(),
            expr, ((JPrimitiveType) cast.getType()).getWrapperType());
        tr.append(new Replace(expr, castToWrapperType));
        expr = castToWrapperType;
      }
      maybeBoxOrUnbox(expr, cast.getType());
      super.endVisit(cast);
    }

    private boolean needNarrowing(@Nonnull JType type) {
      return type instanceof JReferenceType
         && !type.isSameType(JPrimitiveTypeEnum.BOOLEAN.getType().getWrapperType())
         && !type.isSameType(JPrimitiveTypeEnum.BYTE.getType().getWrapperType())
         && !type.isSameType(JPrimitiveTypeEnum.CHAR.getType().getWrapperType())
         && !type.isSameType(JPrimitiveTypeEnum.DOUBLE.getType().getWrapperType())
         && !type.isSameType(JPrimitiveTypeEnum.FLOAT.getType().getWrapperType())
         && !type.isSameType(JPrimitiveTypeEnum.INT.getType().getWrapperType())
         && !type.isSameType(JPrimitiveTypeEnum.LONG.getType().getWrapperType())
         && !type.isSameType(JPrimitiveTypeEnum.SHORT.getType().getWrapperType());
    }

    @Override
    public void endVisit(@Nonnull JBinaryOperation binary) {
      JExpression rhs = binary.getRhs();
      JType rhsType = rhs.getType();
      JExpression lhs = binary.getLhs();
      JType lhsType = lhs.getType();

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
          JExpression castTo = maybeBoxOrUnbox(rhs, lhsType);

          if (lhsType instanceof JNumericType) {
            castIfNeeded(castTo, lhsType);
          }
          break;
        }
        case SHL:
        case SHR:
        case SHRU: {
          castIfNeeded(maybeUnbox(lhs), binary.getType());
          castIfNeeded(maybeUnbox(rhs), JPrimitiveTypeEnum.INT.getType());
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
          castIfNeeded(maybeUnbox(lhs), expectedType);
          castIfNeeded(maybeUnbox(rhs), expectedType);
          break;
        }
        case GT:
        case GTE:
        case LT:
        case LTE: {
           JType expectedType = JPrimitiveType.getBinaryPromotionType(lhsType, rhsType);
           castIfNeeded(maybeUnbox(lhs), expectedType);
           castIfNeeded(maybeUnbox(rhs), expectedType);
          break;
        }
        case EQ:
        case NEQ: {
        if (lhsType instanceof JNumericType || rhsType instanceof JNumericType) {
          JType expectedType = JPrimitiveType.getBinaryPromotionType(lhsType, rhsType);
          castIfNeeded(maybeUnbox(lhs), expectedType);
          castIfNeeded(maybeUnbox(rhs), expectedType);
        } else if (rhsType == JPrimitiveTypeEnum.BOOLEAN.getType()
            || lhsType == JPrimitiveTypeEnum.BOOLEAN.getType()) {
          maybeUnbox(lhs);
          maybeUnbox(rhs);
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
      castIfNeeded(maybeBoxOrUnbox(initializer, expectedType), expectedType);

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
        castIfNeeded(maybeBoxOrUnbox(jExpression, expectedType), expectedType);
      }
      super.endVisit(call);
    }

    @Override
    public void endVisit(@Nonnull JNewArray newArray) {

      for (JExpression dimension : newArray.getDims()) {
        if (!(dimension instanceof JAbsentArrayDimension)) {
          JExpression newDimension = dimension;
          if (!(dimension.getType() instanceof JPrimitiveType)) {
            newDimension = unbox(dimension);
            tr.append(new Replace(dimension, newDimension));
            assert newDimension.getType() instanceof JIntegralType32;
          }
          castIfNeeded(newDimension, JPrimitiveTypeEnum.INT.getType());
        }
      }

      assert newArray.getInitializers().isEmpty() || newArray.hasConstantInitializer();

      super.endVisit(newArray);
    }

    @Override
    public void endVisit(@Nonnull JArrayRef arrayRef) {
      JExpression indexExpr = arrayRef.getIndexExpr();

      JExpression unboxedExpr = maybeUnbox(indexExpr);

      assert unboxedExpr.getType() instanceof JIntegralType32;

      castIfNeeded(unboxedExpr, JPrimitiveTypeEnum.INT.getType());

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
          castIfNeeded(maybeUnbox(unary.getArg()), unary.getType());
          break;
        }
      }

      super.endVisit(unary);
    }

    @Nonnull
    private JExpression maybeUnbox(@Nonnull JExpression expr) {
      JExpression unboxedExpr = expr;

      if (!(expr.getType() instanceof JPrimitiveType)) {
        unboxedExpr = unbox(expr);
        tr.append(new Replace(expr, unboxedExpr));
      }

      return unboxedExpr;
    }

    @Nonnull
    private JExpression maybeBoxOrUnbox(@Nonnull JExpression expr, @Nonnull JType expectedType) {
      JExpression boxUnboxExpr = expr;
      JType type = expr.getType();
      if (!(expectedType instanceof JPrimitiveType) && (type instanceof JPrimitiveType)) {
        assert expectedType instanceof JClassOrInterface;
        boxUnboxExpr = box(expr, (JClassOrInterface) expectedType);
        tr.append(new Replace(expr, boxUnboxExpr));
      } else if ((expectedType instanceof JPrimitiveType) && !(type instanceof JPrimitiveType)) {
        boxUnboxExpr = unbox(expr);
        tr.append(new Replace(expr, boxUnboxExpr));
      }

      return boxUnboxExpr;
    }

    private void castIfNeeded(@Nonnull JExpression exprToCast, @Nonnull JType expectedType) {
      if (expectedType instanceof JNumericType && !exprToCast.getType().isSameType(expectedType)) {
        tr.append(new Replace(exprToCast, new JDynamicCastOperation(
            exprToCast.getSourceInfo(), exprToCast, expectedType)));
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    TypeLegalizerVisitor rca =
        new TypeLegalizerVisitor(tr);
    rca.accept(method);
    tr.commit();
  }

  @Nonnull
  public static JExpression box(@Nonnull JExpression exprToBox,
      @Nonnull JClassOrInterface expectedType) {
    assert exprToBox.getType() instanceof JPrimitiveType;

    JMethodCall boxMethodCall = getBoxingCall(exprToBox, expectedType,
        (JPrimitiveType) exprToBox.getType());

    return boxMethodCall;
  }

  @Nonnull
  public static JExpression unbox(@Nonnull JExpression exprToUnbox,
      @Nonnull JClassOrInterface typeToUnbox) {

    String methodName;
    JType returnType;

    JPhantomLookup lookup = Jack.getSession().getPhantomLookup();

    if (typeToUnbox.isSameType(lookup.getType(CommonTypes.JAVA_LANG_BOOLEAN))) {
      methodName = "booleanValue";
      returnType = JPrimitiveTypeEnum.BOOLEAN.getType();
    } else if (typeToUnbox.isSameType(lookup.getType(CommonTypes.JAVA_LANG_BYTE))) {
      methodName = "byteValue";
      returnType = JPrimitiveTypeEnum.BYTE.getType();
    } else if (typeToUnbox.isSameType(lookup.getType(CommonTypes.JAVA_LANG_CHAR))) {
      methodName = "charValue";
      returnType = JPrimitiveTypeEnum.CHAR.getType();
    } else if (typeToUnbox.isSameType(lookup.getType(CommonTypes.JAVA_LANG_SHORT))) {
      methodName = "shortValue";
      returnType = JPrimitiveTypeEnum.SHORT.getType();
    } else if (typeToUnbox.isSameType(lookup.getType(CommonTypes.JAVA_LANG_INTEGER))) {
      methodName = "intValue";
      returnType = JPrimitiveTypeEnum.INT.getType();
    } else if (typeToUnbox.isSameType(lookup.getType(CommonTypes.JAVA_LANG_FLOAT))) {
      methodName = "floatValue";
      returnType = JPrimitiveTypeEnum.FLOAT.getType();
    } else if (typeToUnbox.isSameType(lookup.getType(CommonTypes.JAVA_LANG_DOUBLE))) {
      methodName = "doubleValue";
      returnType = JPrimitiveTypeEnum.DOUBLE.getType();
    } else if (typeToUnbox.isSameType(lookup.getType(CommonTypes.JAVA_LANG_LONG))) {
      methodName = "longValue";
      returnType = JPrimitiveTypeEnum.LONG.getType();
    } else {
      throw new AssertionError();
    }


    JMethodIdWide unboxMethod =
        typeToUnbox.getOrCreateMethodIdWide(methodName, Lists.<JType>create(),
    MethodKind.INSTANCE_VIRTUAL);
    JMethodCall unboxMethodCall = new JMethodCall(
        exprToUnbox.getSourceInfo(), exprToUnbox, typeToUnbox,
        unboxMethod, returnType, unboxMethod.canBeVirtual());

    return unboxMethodCall;
  }

  @Nonnull
  private static JExpression unbox(@Nonnull JExpression exprToUnbox) {
    JType typeToUnbox = exprToUnbox.getType();
    assert !(typeToUnbox instanceof JPrimitiveType);
    assert typeToUnbox instanceof JClassOrInterface;
    return unbox(exprToUnbox, (JClassOrInterface) typeToUnbox);
  }

  // TODO(mikaelpeltier): Put it into JPrimitiveType
  @Nonnull
  private static JMethodCall getBoxingCall(
      @Nonnull JExpression exprToBox,
      @Nonnull JClassOrInterface type,
      @Nonnull JPrimitiveType pType) {
    JClassOrInterface wrapperType = type;
    JType argType;
    JPhantomLookup lookup = Jack.getSession().getPhantomLookup();
    if (wrapperType.isSameType(lookup.getType(CommonTypes.JAVA_LANG_BOOLEAN))) {
      argType = JPrimitiveTypeEnum.BOOLEAN.getType();
    } else if (wrapperType.isSameType(lookup.getType(CommonTypes.JAVA_LANG_BYTE))) {
      argType = JPrimitiveTypeEnum.BYTE.getType();
    } else if (wrapperType.isSameType(lookup.getType(CommonTypes.JAVA_LANG_CHAR))) {
      argType = JPrimitiveTypeEnum.CHAR.getType();
    } else if (wrapperType.isSameType(lookup.getType(CommonTypes.JAVA_LANG_SHORT))) {
      argType = JPrimitiveTypeEnum.SHORT.getType();
    } else if (wrapperType.isSameType(lookup.getType(CommonTypes.JAVA_LANG_INTEGER))) {
      argType = JPrimitiveTypeEnum.INT.getType();
    } else if (wrapperType.isSameType(lookup.getType(CommonTypes.JAVA_LANG_FLOAT))) {
      argType = JPrimitiveTypeEnum.FLOAT.getType();
    } else if (wrapperType.isSameType(lookup.getType(CommonTypes.JAVA_LANG_DOUBLE))) {
      argType = JPrimitiveTypeEnum.DOUBLE.getType();
    } else if (wrapperType.isSameType(lookup.getType(CommonTypes.JAVA_LANG_LONG))) {
      argType = JPrimitiveTypeEnum.LONG.getType();
    } else {
      argType = pType;
      switch (pType.getPrimitiveTypeEnum()) {
        case BOOLEAN: {
          wrapperType = lookup.getClass(CommonTypes.JAVA_LANG_BOOLEAN);
          break;
        }
        case BYTE: {
          wrapperType = lookup.getClass(CommonTypes.JAVA_LANG_BYTE);
          break;
        }
        case CHAR: {
          wrapperType = lookup.getClass(CommonTypes.JAVA_LANG_CHAR);
          break;
        }
        case SHORT: {
          wrapperType = lookup.getClass(CommonTypes.JAVA_LANG_SHORT);
          break;
        }
        case INT: {
          wrapperType = lookup.getClass(CommonTypes.JAVA_LANG_INTEGER);
          break;
        }
        case FLOAT: {
          wrapperType = lookup.getClass(CommonTypes.JAVA_LANG_FLOAT);
          break;
        }
        case DOUBLE: {
          wrapperType = lookup.getClass(CommonTypes.JAVA_LANG_DOUBLE);
          break;
        }
        case LONG: {
          wrapperType = lookup.getClass(CommonTypes.JAVA_LANG_LONG);
          break;
        }
        default: {
          throw new AssertionError();
        }
      }
    }


    JMethodIdWide methodId = wrapperType.getOrCreateMethodIdWide("valueOf", Lists.create(argType),
        MethodKind.STATIC);
    JMethodCall boxMethodCall = new JMethodCall(
        exprToBox.getSourceInfo(), null, wrapperType, methodId, wrapperType,
        methodId.canBeVirtual());
    List<JType> paramTypes = methodId.getParamTypes();
    assert paramTypes.size() == 1;
    JType paramType = paramTypes.get(0);
    JType exprToBoxType = exprToBox.getType();
    JExpression arg;
    if (exprToBoxType instanceof JNumericType && !paramType.isSameType(exprToBoxType)) {
      assert paramType instanceof JNumericType;
      arg = new JDynamicCastOperation(exprToBox.getSourceInfo(), exprToBox, paramType);
    } else {
      arg = exprToBox;
    }
    boxMethodCall.addArg(arg);

    return boxMethodCall;
  }
}
