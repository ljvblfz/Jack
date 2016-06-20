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

import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JValueLiteral;

import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Concurrent implementation of single value tracker */
public final class ConcurrentLiteralValueTracker extends LiteralValueTrackerBase {
  @Nonnull
  private final AtomicReference<JValueLiteral> value = new AtomicReference<>();

  /**
   * Returns the consolidated single value. Note that the value may be null.
   * Should only be called if isMultipleOrNonLiteralValue() returns false.
   */
  @CheckForNull
  public final JValueLiteral getConsolidatedValue() {
    assert !isMultipleOrNonLiteralValue();
    return value.get();
  }

  @CheckForNull
  final JValueLiteral getRawValue() {
    return value.get();
  }

  /** Marks an expression */
  public final void markExpression(@Nonnull JExpression expression) {
    if (isMultipleOrNonLiteralValue(value.get())) {
      return;
    }
    JValueLiteral candidate = asLiteral(expression);
    while (true) {
      // The value is either already set or not
      JValueLiteral existing = value.get();

      if (existing == null) {
        // No value, just try to set it
        if (value.compareAndSet(null, candidate)) {
          return;
        }
      } else {
        // Value already exists, if it's already a non-single value
        // or is the same value, no change is needed
        if (isMultipleOrNonLiteralValue(existing) ||
            OptimizerUtils.areSameValueLiterals(existing, candidate)) {
          return;
        }
        // There are multiple different values
        if (value.compareAndSet(existing, getMultipleOrNonLiteralValue())) {
          return;
        }
      }
    }
  }

  /** Returns true if there are multiple values, or the value is non-literal */
  public final boolean isMultipleOrNonLiteralValue() {
    return isMultipleOrNonLiteralValue(value.get());
  }
}
