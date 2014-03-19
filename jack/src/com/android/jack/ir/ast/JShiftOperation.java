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
 * Binary shift expression.
 */
@Description("Binary shift expression")
public abstract class JShiftOperation extends JBinaryOperation {

  private static final long serialVersionUID = 1L;

  public JShiftOperation(@Nonnull SourceInfo info,
      @Nonnull JExpression lhs, @Nonnull JExpression rhs) {
    super(info, lhs, rhs);
  }

  @Override
  // Section Shift Operators (JLS-7 15.19)
  @Nonnull
  public JType getType() {
    JType lhsPromotedType = JPrimitiveType.getUnaryPromotion(getLhs().getType());

    assert JPrimitiveTypeEnum.BYTE.getType().isEquivalent(lhsPromotedType)
        || JPrimitiveTypeEnum.CHAR.getType().isEquivalent(lhsPromotedType)
        || JPrimitiveTypeEnum.SHORT.getType().isEquivalent(lhsPromotedType)
        || JPrimitiveTypeEnum.INT.getType().isEquivalent(lhsPromotedType)
        || JPrimitiveTypeEnum.LONG.getType().isEquivalent(lhsPromotedType);

    assert JPrimitiveTypeEnum.BYTE.getType()
        .isEquivalent(JPrimitiveType.getUnaryPromotion(getRhs().getType()))
        || JPrimitiveTypeEnum.CHAR.getType()
            .isEquivalent(JPrimitiveType.getUnaryPromotion(getRhs().getType()))
        || JPrimitiveTypeEnum.SHORT.getType()
            .isEquivalent(JPrimitiveType.getUnaryPromotion(getRhs().getType()))
        || JPrimitiveTypeEnum.INT.getType()
            .isEquivalent(JPrimitiveType.getUnaryPromotion(getRhs().getType()))
        || JPrimitiveTypeEnum.LONG.getType()
            .isEquivalent(JPrimitiveType.getUnaryPromotion(getRhs().getType()));

    return lhsPromotedType;
  }
}
