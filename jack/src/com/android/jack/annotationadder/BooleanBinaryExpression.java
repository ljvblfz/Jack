/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.annotationadder;

import javax.annotation.Nonnull;

/**
 * Binary boolean expression.
 */
public class BooleanBinaryExpression<U> implements Expression<Boolean, U> {

  @Nonnull
  private final Expression<Boolean, U> left;

  @Nonnull
  private final BooleanBinaryOperator operator;

  @Nonnull
  private final Expression<Boolean, U> right;

  public BooleanBinaryExpression(
      @Nonnull Expression<Boolean, U> left,
      @Nonnull BooleanBinaryOperator operator,
      @Nonnull Expression<Boolean, U> right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  @Override
  @Nonnull
  public Boolean eval(@Nonnull U tested, @Nonnull Context context) {
    boolean leftValue = left.eval(tested, context).booleanValue();
    boolean rightValue = right.eval(tested, context).booleanValue();
    return Boolean.valueOf(operator.eval(leftValue, rightValue));
  }

}
