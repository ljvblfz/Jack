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
    return (lhsType.equals(jlString) &&
        rhsType.equals(jlString))
      || (lhsType.equals(jlString) &&
            (rhsType.equals(JPrimitiveTypeEnum.BOOLEAN.getType())
            || rhsType.equals(JPrimitiveTypeEnum.BYTE.getType())
            || rhsType.equals(JPrimitiveTypeEnum.CHAR.getType())
            || rhsType.equals(JPrimitiveTypeEnum.SHORT.getType())
            || rhsType.equals(JPrimitiveTypeEnum.INT.getType())
            || rhsType.equals(JPrimitiveTypeEnum.FLOAT.getType())
            || rhsType.equals(JPrimitiveTypeEnum.LONG.getType())
            || rhsType.equals(JPrimitiveTypeEnum.DOUBLE.getType())
            || rhsType instanceof JReferenceType))
      || (rhsType.equals(jlString) &&
          (lhsType.equals(JPrimitiveTypeEnum.BOOLEAN.getType())
          || lhsType.equals(JPrimitiveTypeEnum.BYTE.getType())
          || lhsType.equals(JPrimitiveTypeEnum.CHAR.getType())
          || lhsType.equals(JPrimitiveTypeEnum.SHORT.getType())
          || lhsType.equals(JPrimitiveTypeEnum.INT.getType())
          || lhsType.equals(JPrimitiveTypeEnum.FLOAT.getType())
          || lhsType.equals(JPrimitiveTypeEnum.LONG.getType())
          || lhsType.equals(JPrimitiveTypeEnum.DOUBLE.getType())
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
