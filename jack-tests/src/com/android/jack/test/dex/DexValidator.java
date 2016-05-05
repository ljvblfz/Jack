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

package com.android.jack.test.dex;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Abstract DEX validator */
public abstract class DexValidator<T> {
  @CheckForNull
  private DexValidator<T> next = null;

  protected abstract void validateImpl(@Nonnull T element);

  public final void validate(@Nonnull T element) {
    validateImpl(element);
    if (next != null) {
      next.validate(element);
    }
  }

  @Nonnull
  public DexValidator<T> andAlso(@Nonnull DexValidator<T> other) {
    if (next == null) {
      next = other;
    } else {
      next.andAlso(other);
    }
    return this;
  }
}
