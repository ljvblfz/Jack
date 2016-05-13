/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.optimizations.common;

import com.android.jack.ir.ast.JValueLiteral;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/** Represents a list of literal value trackers allowing concurrent modifications */
public class ConcurrentLiteralValueListTracker extends LiteralValueListTracker {
  @Nonnull
  private final ConcurrentLiteralValueTracker[] values;

  public ConcurrentLiteralValueListTracker(@Nonnegative int size) {
    values = new ConcurrentLiteralValueTracker[size];
    for (int i = 0; i < size; i++) {
      values[i] = new ConcurrentLiteralValueTracker();
    }
  }

  @Override
  public int size() {
    return values.length;
  }

  @CheckForNull
  @Override
  JValueLiteral getRawValue(@Nonnegative int index) {
    return values[index].getRawValue();
  }

  @Override
  void mergeWith(@Nonnegative int index, @CheckForNull JValueLiteral literal) {
    if (literal != null) {
      values[index].markExpression(literal);
    }
  }
}
