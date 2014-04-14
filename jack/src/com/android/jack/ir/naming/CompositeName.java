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

package com.android.jack.ir.naming;

import javax.annotation.Nonnull;

/**
 * Composition of {@link AbstractName}s. This implementation is not thread-safe. If multiple threads
 * modify the {@link AbstractName}s that composes this name, it must be synchronized externally.
 */
public class CompositeName extends AbstractName {

  @Nonnull
  private final CharSequence leftStr;

  @Nonnull
  private final CharSequence rightStr;

  public CompositeName(@Nonnull CharSequence leftStr, CharSequence rightStr) {
    this.leftStr = leftStr;
    this.rightStr = rightStr;
  }

  @Override
  public int length() {
    return leftStr.length() + rightStr.length();
  }

  @Override
  public String toString() {
    return leftStr.toString() + rightStr.toString();
  }
}
