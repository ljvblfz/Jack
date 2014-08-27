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

import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

/**
 * Type set for primitive.
 */
public class PrimitiveTypeFilter implements Expression<Collection<JPrimitiveType>, Scope> {

  @Nonnull
  public static final PrimitiveTypeFilter VOID = new PrimitiveTypeFilter(JPrimitiveTypeEnum.VOID);

  @Nonnull
  public static final PrimitiveTypeFilter BOOLEAN =
    new PrimitiveTypeFilter(JPrimitiveTypeEnum.BOOLEAN);

  @Nonnull
  public static final PrimitiveTypeFilter BYTE = new PrimitiveTypeFilter(JPrimitiveTypeEnum.BYTE);

  @Nonnull
  public static final PrimitiveTypeFilter CHAR = new PrimitiveTypeFilter(JPrimitiveTypeEnum.CHAR);

  @Nonnull
  public static final PrimitiveTypeFilter SHORT = new PrimitiveTypeFilter(JPrimitiveTypeEnum.SHORT);

  @Nonnull
  public static final PrimitiveTypeFilter INT = new PrimitiveTypeFilter(JPrimitiveTypeEnum.INT);

  @Nonnull
  public static final PrimitiveTypeFilter FLOAT = new PrimitiveTypeFilter(JPrimitiveTypeEnum.FLOAT);

  @Nonnull
  public static final PrimitiveTypeFilter LONG = new PrimitiveTypeFilter(JPrimitiveTypeEnum.LONG);

  @Nonnull
  public static final PrimitiveTypeFilter DOUBLE =
    new PrimitiveTypeFilter(JPrimitiveTypeEnum.DOUBLE);

  @Nonnull
  private final JPrimitiveTypeEnum element;

  private PrimitiveTypeFilter(@Nonnull JPrimitiveTypeEnum element) {
    this.element = element;
  }

  @Nonnull
  @Override
  public Collection<JPrimitiveType> eval(@Nonnull Scope scope, @Nonnull Context context) {
    if (scope instanceof SingleTypeScope
        && ((SingleTypeScope) scope).getElement() == element.getType()) {
      return  Collections.singleton(element.getType());
    } else {
      return Collections.emptySet();
    }
  }

}
