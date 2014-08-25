/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.types.JIntegralType;
import com.android.jack.ir.types.JIntegralType32;
import com.android.jack.ir.types.JNumericType;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Conditional expression.
 */
@Description("Conditional expression")
public class JConditionalExpression extends JExpression {

  @Nonnull
  private JExpression elseExpr;
  @Nonnull
  private JExpression ifTest;
  @Nonnull
  private JExpression thenExpr;

  public JConditionalExpression(@Nonnull SourceInfo info,
      @Nonnull JExpression ifTest,
      @Nonnull JExpression thenExpr,
      @Nonnull JExpression elseExpr) {
    super(info);
    this.ifTest = ifTest;
    this.thenExpr = thenExpr;
    this.elseExpr = elseExpr;
  }

  @Nonnull
  public JExpression getElseExpr() {
    return elseExpr;
  }

  @Nonnull
  public JExpression getIfTest() {
    return ifTest;
  }

  @Nonnull
  public JExpression getThenExpr() {
    return thenExpr;
  }

  @Nonnull
  @Override
  // section Conditional Operator ? (JLS-7 15.25)
  public JType getType() {

    assert JPrimitiveTypeEnum.BOOLEAN.getType().isEquivalent(ifTest.getType());

    JType thenType = thenExpr.getType();
    JType elseType = elseExpr.getType();


    // JLS-7 15.25 first bullet
    if (thenType.isSameType(elseType)) {
      return thenType;
    }

    // JLS-7 15.25 second bullet
    if (thenType instanceof JPrimitiveType && ((JPrimitiveType) thenType).isEquivalent(elseType)) {
      return thenType;
    }
    if (elseType instanceof JPrimitiveType && ((JPrimitiveType) elseType).isEquivalent(thenType)) {
      return elseType;
    }


    // JLS-7 15.25 third bullet
    if (JNullType.isNullType(thenType) && (elseType instanceof JReferenceType)) {
      return elseType;
    }
    if (JNullType.isNullType(elseType) && (thenType instanceof JReferenceType)) {
      return thenType;
    }

    // JLS-7 15.25 fourth bullet
    JPhantomLookup lookup = Jack.getSession().getPhantomLookup();
    if (isNumber(thenType) && isNumber(elseType)) {
      // first sub-bullet
      if ((JPrimitiveTypeEnum.BYTE.getType().isEquivalent(thenType)
            && JPrimitiveTypeEnum.SHORT.getType().isEquivalent(elseType))
          || (JPrimitiveTypeEnum.BYTE.getType().isEquivalent(elseType)
            && JPrimitiveTypeEnum.SHORT.getType().isEquivalent(thenType))) {
        return JPrimitiveTypeEnum.SHORT.getType();
      }

      // second sub-bullet
      if ((thenType.isSameType(JPrimitiveTypeEnum.BYTE.getType())
          || thenType.isSameType(JPrimitiveTypeEnum.CHAR.getType())
          || thenType.isSameType(JPrimitiveTypeEnum.SHORT.getType()))
          && ((elseExpr instanceof JIntegralConstant32) && elseType instanceof JIntegralType)) {
        if (((JIntegralType32) thenType).isValidValue(
            ((JIntegralConstant32) elseExpr).getIntValue())) {
          return thenType;
        }
      }
      if ((elseType.isSameType(JPrimitiveTypeEnum.BYTE.getType())
          || elseType.isSameType(JPrimitiveTypeEnum.CHAR.getType())
          || elseType.isSameType(JPrimitiveTypeEnum.SHORT.getType()))
          && ((thenExpr instanceof JIntegralConstant32) && thenType instanceof JIntegralType)) {
        if (((JIntegralType32) elseType).isValidValue(
            ((JIntegralConstant32) thenExpr).getIntValue())) {
          return elseType;
        }
      }

      // third sub-bullet
      if ((JPrimitiveTypeEnum.BYTE.getType().isWrapperType(thenType)
          || JPrimitiveTypeEnum.CHAR.getType().isWrapperType(thenType)
          || JPrimitiveTypeEnum.SHORT.getType().isWrapperType(thenType))
          && ((elseExpr instanceof JIntegralConstant32) && elseType instanceof JIntegralType32)) {
         JPrimitiveType unboxedThenType = ((JClassOrInterface) thenType).getWrappedType();
         assert unboxedThenType != null;
        if (((JIntegralType32) unboxedThenType).isValidValue(
            ((JIntegralConstant32) elseExpr).getIntValue())) {
          return unboxedThenType;
        }
      }
      if ((JPrimitiveTypeEnum.BYTE.getType().isWrapperType(elseType)
          || JPrimitiveTypeEnum.CHAR.getType().isWrapperType(elseType)
          || JPrimitiveTypeEnum.SHORT.getType().isWrapperType(elseType))
          && ((thenExpr instanceof JIntegralConstant32) && thenType instanceof JIntegralType32)) {
        JPrimitiveType unboxedElseType = ((JClassOrInterface) elseType).getWrappedType();
        assert unboxedElseType != null;
        if (((JIntegralType32) unboxedElseType).isValidValue(
            ((JIntegralConstant32) thenExpr).getIntValue())) {
          return unboxedElseType;
        }
      }

      // fourth sub-bullet
      return JPrimitiveType.getBinaryPromotionType(thenType, elseType);
    }

    // JLS-7 15.25 fifth bullet
    // TODO(yroussel) make a finer response even if incomplete or imprecise ?

    // Implementation of only a tiny case needed for unboxing. To be removed if fifth bullet is
    // really implemented.
    if (JNullType.isNullType(thenType) && (elseType instanceof JPrimitiveType)) {
      return ((JPrimitiveType) elseType).getWrapperType();
    }
    if (JNullType.isNullType(elseType) && (thenType instanceof JPrimitiveType)) {
      return ((JPrimitiveType) thenType).getWrapperType();
    }

    if (thenType instanceof JArrayType && elseType instanceof JArrayType) {
      JArrayType thenArrayType = ((JArrayType) thenType);
      JArrayType elseArrayType = ((JArrayType) elseType);
      int thenArrayTypeDims = thenArrayType.getDims();
      int elseArrayTypeDims = elseArrayType.getDims();

      int minDim = thenArrayTypeDims;
      if (minDim > elseArrayTypeDims) {
        minDim = elseArrayTypeDims;
      }

      if (!(thenArrayType.getLeafType() instanceof JPrimitiveType ||
          elseArrayType.getLeafType() instanceof JPrimitiveType)) {
        return lookup.getArrayType(lookup.getClass(CommonTypes.JAVA_LANG_OBJECT), minDim);
      }
    }

    return lookup.getClass(CommonTypes.JAVA_LANG_OBJECT);
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(ifTest);
      visitor.accept(thenExpr);
      visitor.accept(elseExpr);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    ifTest.traverse(schedule);
    thenExpr.traverse(schedule);
    elseExpr.traverse(schedule);
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    assert newNode != null;

    if (ifTest == existingNode) {
      ifTest = (JExpression) newNode;
    } else if (thenExpr == existingNode) {
      thenExpr = (JExpression) newNode;
    } else if (elseExpr == existingNode) {
      elseExpr = (JExpression) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  private boolean isNumber(@Nonnull JType type) {
    return type instanceof JNumericType
        || (type instanceof JClassOrInterface &&
            ((JClassOrInterface) type).getWrappedType() instanceof JNumericType);
  }
}
