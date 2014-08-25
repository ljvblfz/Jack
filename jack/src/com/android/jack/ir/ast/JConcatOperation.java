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

import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;
import com.android.sched.transform.TransformRequest;

import javax.annotation.Nonnull;

/**
 * Binary operator expression for {@code concat}.
 */
@Description("Concat operator expression")
public class JConcatOperation extends JBinaryOperation {

  @Nonnull
  private final JClass jlString;

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
    return (lhsType.isSameType(jlString) && rhsType.isSameType(jlString))
      || (lhsType.isSameType(jlString) &&
            (rhsType.isSameType(JPrimitiveTypeEnum.BOOLEAN.getType())
            || rhsType.isSameType(JPrimitiveTypeEnum.BYTE.getType())
            || rhsType.isSameType(JPrimitiveTypeEnum.CHAR.getType())
            || rhsType.isSameType(JPrimitiveTypeEnum.SHORT.getType())
            || rhsType.isSameType(JPrimitiveTypeEnum.INT.getType())
            || rhsType.isSameType(JPrimitiveTypeEnum.FLOAT.getType())
            || rhsType.isSameType(JPrimitiveTypeEnum.LONG.getType())
            || rhsType.isSameType(JPrimitiveTypeEnum.DOUBLE.getType())
            || rhsType instanceof JReferenceType))
      || (rhsType.isSameType(jlString) &&
          (lhsType.isSameType(JPrimitiveTypeEnum.BOOLEAN.getType())
          || lhsType.isSameType(JPrimitiveTypeEnum.BYTE.getType())
          || lhsType.isSameType(JPrimitiveTypeEnum.CHAR.getType())
          || lhsType.isSameType(JPrimitiveTypeEnum.SHORT.getType())
          || lhsType.isSameType(JPrimitiveTypeEnum.INT.getType())
          || lhsType.isSameType(JPrimitiveTypeEnum.FLOAT.getType())
          || lhsType.isSameType(JPrimitiveTypeEnum.LONG.getType())
          || lhsType.isSameType(JPrimitiveTypeEnum.DOUBLE.getType())
          || lhsType instanceof JReferenceType));
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
