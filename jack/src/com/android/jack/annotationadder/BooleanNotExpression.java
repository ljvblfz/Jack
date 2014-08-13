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
 * '!' boolean expression.
 */
public class BooleanNotExpression<U> implements Expression<Boolean, U> {

  @Nonnull
  private final Expression<Boolean, U> arg;

  public BooleanNotExpression(@Nonnull Expression<Boolean, U> arg) {
    this.arg = arg;
  }

  @Override
  @Nonnull
  public Boolean eval(@Nonnull U tested, @Nonnull Context context) {
    return Boolean.valueOf(!arg.eval(tested, context).booleanValue());
  }
}
