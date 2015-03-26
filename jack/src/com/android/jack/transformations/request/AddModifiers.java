/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.transformations.request;

import com.android.jack.ir.ast.HasModifier;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@code TransformationStep} allowing to add modifiers
 * to a {@link HasModifier}.
 */
public class AddModifiers implements TransformationStep, TransformStep {
  @Nonnull
  private final HasModifier hasModifier;

  private final int toAdd;

  public AddModifiers(@Nonnull HasModifier hasModifier, int toAdd) {
    this.hasModifier = hasModifier;
    this.toAdd = toAdd;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    hasModifier.setModifier(hasModifier.getModifier() | toAdd);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Add modifiers ");
    sb.append(toAdd);
    sb.append(" to ");
    sb.append(hasModifier);
    return sb.toString();
  }
}
