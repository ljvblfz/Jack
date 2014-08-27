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

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * A boolean expression testing the not emptiness of a Collection expression.
 */
public class IsNotEmpty<T> implements Expression<Boolean, T> {

  @Nonnull
  private final Expression<Collection<?>, T> collectionExpression;

  public IsNotEmpty(@Nonnull Expression<Collection<?>, T> collectionExpression) {
    this.collectionExpression = collectionExpression;
  }

  @Override
  public Boolean eval(@Nonnull T scope, @Nonnull Context context) {
    return Boolean.valueOf(!collectionExpression.eval(scope, context).isEmpty());
  }

}
