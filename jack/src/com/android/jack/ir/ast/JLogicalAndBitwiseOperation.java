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

import javax.annotation.Nonnull;

/**
 * Logical and bitwise expression.
 */
@Description("Logical and bitwise expression")
public abstract class JLogicalAndBitwiseOperation extends JBinaryOperation {

  private static final long serialVersionUID = 1L;

  public JLogicalAndBitwiseOperation(@Nonnull SourceInfo info,
      @Nonnull JExpression lhs, @Nonnull JExpression rhs) {
    super(info, lhs, rhs);
  }

  @Override
  // Section Bitwise and Logical Operators (JLS-7 15.22, 15.22.1)
  public JType getType() {
    JType lhsType = getLhs().getType();
    JType rhsType = getRhs().getType();

    if (JPrimitiveTypeEnum.BOOLEAN.getType().isEquivalent(lhsType)
        && JPrimitiveTypeEnum.BOOLEAN.getType().isEquivalent(rhsType)) {
      return JPrimitiveTypeEnum.BOOLEAN.getType();
    }

    assert JPrimitiveTypeEnum.BYTE.getType().isEquivalent(lhsType)
    || JPrimitiveTypeEnum.CHAR.getType().isEquivalent(lhsType)
    || JPrimitiveTypeEnum.SHORT.getType().isEquivalent(lhsType)
    || JPrimitiveTypeEnum.INT.getType().isEquivalent(lhsType)
    || JPrimitiveTypeEnum.LONG.getType().isEquivalent(lhsType);

    assert JPrimitiveTypeEnum.BYTE.getType().isEquivalent(rhsType)
    || JPrimitiveTypeEnum.CHAR.getType().isEquivalent(rhsType)
    || JPrimitiveTypeEnum.SHORT.getType().isEquivalent(rhsType)
    || JPrimitiveTypeEnum.INT.getType().isEquivalent(rhsType)
    || JPrimitiveTypeEnum.LONG.getType().isEquivalent(rhsType);

    return JPrimitiveType.getBinaryPromotionType(lhsType, rhsType);
  }
}
