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

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/** Represents a list of value trackers */
public abstract class LiteralValueListTracker extends LiteralValueTrackerBase {
  public abstract int size();

  /** Get a raw tracker value */
  @CheckForNull
  abstract JValueLiteral getRawValue(@Nonnegative int index);

  /** Merge the value with a raw literal */
  abstract void mergeWith(@Nonnegative int index, @CheckForNull JValueLiteral literal);

  /** Is any of the tracked values is actually a constant. */
  public boolean hasAtLeastOneLiteral() {
    for (int i = 0; i < size(); i++) {
      if (!isMultipleOrNonLiteralValue(getRawValue(i))) {
        return true;
      }
    }
    return false;
  }

  /** Returns true if there are multiple values, or the value is non-literal */
  public final boolean isMultipleOrNonLiteralValue(@Nonnegative int arg) {
    return isMultipleOrNonLiteralValue(getRawValue(arg));
  }

  /**
   * Returns the consolidated single value. Note that the value may be null.
   * Should only be called if isMultipleOrNonLiteralValue() returns false.
   */
  @CheckForNull
  public final JValueLiteral getConsolidatedValue(@Nonnegative int arg) {
    assert !isMultipleOrNonLiteralValue(arg);
    return getRawValue(arg);
  }

  /** Updates tracked values with the new expression list */
  public final void updateWith(@Nonnull List<JExpression> args) {
    int size = size();
    assert size == args.size();
    for (int i = 0; i < size; i++) {
      mergeWith(i, asLiteral(args.get(i)));
    }
  }

  /** Updates tracked values with the new values from another tracker */
  public final void updateWith(@Nonnull LiteralValueListTracker other) {
    int size = size();
    assert size == other.size();
    for (int i = 0; i < size; i++) {
      mergeWith(i, other.getRawValue(i));
    }
  }
}
