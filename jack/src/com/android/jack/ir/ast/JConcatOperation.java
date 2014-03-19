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

package com.android.jack.ir.ast;

import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.sched.item.Description;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Binary operator expression for {@code concat}.
 */
@Description("Concat operator expression")
public class JConcatOperation extends JBinaryOperation {

  private static final long serialVersionUID = 1L;
  @Nonnull
  private JClass jlString;

  public JConcatOperation(
      @Nonnull SourceInfo info, @Nonnull JClass jlString,
      @Nonnull JExpression lhs, @Nonnull JExpression rhs) {
    super(info, lhs, rhs);
    this.jlString = jlString;
  }

  @Nonnull
  @Override
  // Section String Concatenation Operator + (JLS-7 15.18.1)
  public JType getType() {
    assert hasValidOperand();

    return jlString;
  }

  private boolean hasValidOperand() {
    JType lhsType = getLhs().getType();
    JType rhsType = getRhs().getType();
    return ((lhsType == jlString) &&
        (rhsType == jlString))
      || ((lhsType == jlString) &&
            (rhsType == JPrimitiveTypeEnum.BOOLEAN.getType()
            || rhsType == JPrimitiveTypeEnum.BYTE.getType()
            || rhsType == JPrimitiveTypeEnum.CHAR.getType()
            || rhsType == JPrimitiveTypeEnum.SHORT.getType()
            || rhsType == JPrimitiveTypeEnum.INT.getType()
            || rhsType == JPrimitiveTypeEnum.FLOAT.getType()
            || rhsType == JPrimitiveTypeEnum.LONG.getType()
            || rhsType == JPrimitiveTypeEnum.DOUBLE.getType()
            || rhsType instanceof JReferenceType))
      || ((rhsType == jlString) &&
          (lhsType == JPrimitiveTypeEnum.BOOLEAN.getType()
          || lhsType == JPrimitiveTypeEnum.BYTE.getType()
          || lhsType == JPrimitiveTypeEnum.CHAR.getType()
          || lhsType == JPrimitiveTypeEnum.SHORT.getType()
          || lhsType == JPrimitiveTypeEnum.INT.getType()
          || lhsType == JPrimitiveTypeEnum.FLOAT.getType()
          || lhsType == JPrimitiveTypeEnum.LONG.getType()
          || lhsType == JPrimitiveTypeEnum.DOUBLE.getType()
          || lhsType instanceof JReferenceType));
  }

  public void setType(@Nonnull JClass jlString) {
    this.jlString = jlString;
  }

  @Override
  @Nonnull
  public JBinaryOperator getOp() {
    return JBinaryOperator.CONCAT;
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
