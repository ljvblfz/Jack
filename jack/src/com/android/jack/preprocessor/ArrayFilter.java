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

import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JType;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Array type set.
 */
public class ArrayFilter implements Expression<Collection<JArrayType>, Scope> {

  @Nonnull
  private final Expression<Collection<? extends JType>, Scope> leafType;

  @Nonnegative
  private final int dim;

  public ArrayFilter(@Nonnull Expression<Collection<? extends JType>, Scope> leafType,
      @Nonnegative int dim) {
    this.leafType = leafType;
    this.dim = dim;
  }

  @Override
  @Nonnull
  public Collection<JArrayType> eval(@Nonnull Scope scope, @Nonnull Context context) {
    if (scope instanceof SingleTypeScope) {
      JType toMatch = ((SingleTypeScope) scope).getElement();
      if (toMatch instanceof JArrayType) {
        JArrayType arrayToMatch = (JArrayType) toMatch;
        if (arrayToMatch.getDims() == dim) {
          if (!leafType.eval(new SingleTypeScope(arrayToMatch.getLeafType()), context).isEmpty()) {
            return Collections.singleton(arrayToMatch);
          }
        }
      }
    }
    return Collections.emptySet();
  }
}
