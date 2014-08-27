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

package com.android.jack.preprocessor;

import javax.annotation.Nonnull;

/**
 * Boolean expression.
 */
public class BooleanExpression {

  @Nonnull
  public static final Expression<Boolean, Object> TRUE = new Expression<Boolean, Object>() {
    @Override
    @Nonnull
    public Boolean eval(@Nonnull Object scope, @Nonnull Context context) {
      return Boolean.TRUE;
    }

    @Override
    @Nonnull
    public String toString() {
      return "true";
    }
  };

  @Nonnull
  public static final Expression<Boolean, Object> FALSE = new Expression<Boolean, Object>() {
    @Override
    @Nonnull
    public Boolean eval(@Nonnull Object scope, @Nonnull Context context) {
      return Boolean.FALSE;
    }

    @Override
    @Nonnull
    public String toString() {
      return "true";
    }
  };

  @Nonnull
  @SuppressWarnings("unchecked")
  public static <T> Expression<Boolean, T> getTrue() {
    return (Expression<Boolean, T>) TRUE;
  }

  @Nonnull
  @SuppressWarnings("unchecked")
  public static <T> Expression<Boolean, T> getFalse() {
    return (Expression<Boolean, T>) FALSE;
  }

}
