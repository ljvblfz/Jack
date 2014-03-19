/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.transformations.booleanoperators;

import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JIfStatement;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * Marker to specify fallThrough of if statement or conditional expression.
 */
@Description("FallThroughMarker")
@ValidOn(value = {JIfStatement.class, JConditionalExpression.class})
public class FallThroughMarker implements Marker {

  /**
   * Fall through of conditional statement.
   */
  public static enum FallThroughEnum {
    THEN,
    ELSE
  }

  @Nonnull
  private final FallThroughEnum fallThrough;

  public FallThroughMarker(@Nonnull FallThroughEnum fallThrough) {
    this.fallThrough = fallThrough;
  }

  @Nonnull
  public FallThroughEnum getFallThrough() {
    return fallThrough;
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }
}
