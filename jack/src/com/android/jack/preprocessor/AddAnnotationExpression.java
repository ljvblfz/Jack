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

import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAnnotationType;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Adds an annotation on {@link Annotable} matched by a filtering expression.
 */
public class AddAnnotationExpression<T> implements Expression<Collection<T>, Scope> {

  @Nonnull
  private final JAnnotationType toAdd;
  @Nonnull
  private final Expression<Collection<T>, Scope> on;

  public AddAnnotationExpression(@Nonnull JAnnotationType toAdd,
      @Nonnull Expression<Collection<T>, Scope> on) {
    this.toAdd = toAdd;
    this.on = on;
  }

  @Override
  public Collection<T> eval(@Nonnull Scope scope, @Nonnull Context context) {
    Collection<T> collection = on.eval(scope, context);
    if (!collection.isEmpty()) {
      context.addAnnotate(toAdd, collection);
    }
    return collection;
  }
}
