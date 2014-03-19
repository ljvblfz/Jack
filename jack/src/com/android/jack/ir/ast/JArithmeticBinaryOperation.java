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
import com.android.sched.item.Description;

import javax.annotation.Nonnull;

/**
 * Binary arithmetic expression.
 */
@Description("Binary arithmetic expression")
public abstract class JArithmeticBinaryOperation extends JBinaryOperation {

  private static final long serialVersionUID = 1L;

  public JArithmeticBinaryOperation(@Nonnull SourceInfo info,
      @Nonnull JExpression lhs, @Nonnull JExpression rhs) {
    super(info, lhs, rhs);
  }

  @Override
  // Section Additive Operators (+ and -) for Numeric Types (JLS-7 15.18.2)
  // Section Multiplicative Operators (JLS-7 15.17)
  public JType getType() {
    return JPrimitiveType.getBinaryPromotionType(getLhs().getType(), getRhs().getType());
  }
}
