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
 * Equality binary expression.
 */
@Description("Equality binary expression")
public abstract class JEqualityOperation extends JBinaryOperation {

  public JEqualityOperation(@Nonnull SourceInfo info,
      @Nonnull JExpression lhs, @Nonnull JExpression rhs) {
    super(info, lhs, rhs);
  }

  @Override
  // Section Equality Operators (JLS-7 15.21)
  public JType getType() {
    assert isValidTypes();

    return JPrimitiveTypeEnum.BOOLEAN.getType();
  }

  private boolean isValidTypes() {
    return ((getLhs().getType() instanceof JReferenceType &&
        getRhs().getType() instanceof JReferenceType))
        ||
        ((JPrimitiveTypeEnum.BOOLEAN.getType().isEquivalent(getLhs().getType()) &&
            JPrimitiveTypeEnum.BOOLEAN.getType().isEquivalent(getRhs().getType())))
        ||
        ((JPrimitiveTypeEnum.BYTE.getType().isEquivalent(getLhs().getType())
        || JPrimitiveTypeEnum.CHAR.getType().isEquivalent(getLhs().getType())
        || JPrimitiveTypeEnum.SHORT.getType().isEquivalent(getLhs().getType())
        || JPrimitiveTypeEnum.INT.getType().isEquivalent(getLhs().getType())
        || JPrimitiveTypeEnum.FLOAT.getType().isEquivalent(getLhs().getType())
        || JPrimitiveTypeEnum.LONG.getType().isEquivalent(getLhs().getType())
        || JPrimitiveTypeEnum.DOUBLE.getType().isEquivalent(getLhs().getType())) &&
        (JPrimitiveTypeEnum.BYTE.getType().isEquivalent(getRhs().getType())
        || JPrimitiveTypeEnum.CHAR.getType().isEquivalent(getRhs().getType())
        || JPrimitiveTypeEnum.SHORT.getType().isEquivalent(getRhs().getType())
        || JPrimitiveTypeEnum.INT.getType().isEquivalent(getRhs().getType())
        || JPrimitiveTypeEnum.FLOAT.getType().isEquivalent(getRhs().getType())
        || JPrimitiveTypeEnum.LONG.getType().isEquivalent(getRhs().getType())
        || JPrimitiveTypeEnum.DOUBLE.getType().isEquivalent(getRhs().getType())));
  }
}
