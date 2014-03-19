/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.shrob.spec;

import javax.annotation.Nonnull;

/**
 * Represents a specification which supports negators.
 * @param <T> the type the specification applies to
 */
public abstract class SpecificationWithNegator<T> implements Specification<T> {

  private boolean hasNegator;

  protected abstract boolean matchesWithoutNegator(@Nonnull T t);

  public void setNegator(boolean negator) {
    this.hasNegator = negator;
  }

  @Override
  public boolean matches(@Nonnull T t) {
    if (hasNegator) {
      return !matchesWithoutNegator(t);
    } else {
      return matchesWithoutNegator(t);
    }
  }

  @Override
  public String toString() {
    if (hasNegator) {
      return "!";
    } else {
      return "";
    }
  }
}
