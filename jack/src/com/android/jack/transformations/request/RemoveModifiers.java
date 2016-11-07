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
 * A {@link TransformationStep} allowing to remove modifiers from a {@link HasModifier}.
 *
 * @see AddModifiers
 */
public class RemoveModifiers implements TransformationStep, TransformStep {
  @Nonnull
  private final HasModifier hasModifier;

  private final int toRemove;

  /**
   * Constructor specifying the modifiers to remove from the {@link HasModifier} instance.
   *
   * @param hasModifier the object whose modifiers will be updated
   * @param toRemove the modifiers to remove from the object
   */
  public RemoveModifiers(@Nonnull HasModifier hasModifier, int toRemove) {
    this.hasModifier = hasModifier;
    this.toRemove = toRemove;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    hasModifier.setModifier(hasModifier.getModifier() & (~toRemove));
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Remove modifiers ");
    sb.append(toRemove);
    sb.append(" from ");
    sb.append(hasModifier);
    return sb.toString();
  }
}
