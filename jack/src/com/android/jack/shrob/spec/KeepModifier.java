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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Modifier for keep rules
 */
public class KeepModifier {

  private static final int NONE = 0;

  private static final int ALLOW_SHRINKING       = 0x0001;
  private static final int ALLOW_OBFUSCATION      = 0x0002;

  private int modifier = NONE;

  public KeepModifier() {

  }

  private KeepModifier(@Nonnegative int modifier) {
    this.modifier = modifier;
  }

  public boolean allowShrinking() {
    return ((modifier & ALLOW_SHRINKING) == ALLOW_SHRINKING);
  }

  public boolean allowObfuscation() {
    return ((modifier & ALLOW_OBFUSCATION) == ALLOW_OBFUSCATION);
  }

  @Nonnull
  public KeepModifier setAllowShrinking() {
    modifier |= ALLOW_SHRINKING;
    return this;
  }

  @Nonnull
  public KeepModifier setAllowObfuscation() {
    modifier |= ALLOW_OBFUSCATION;
    return this;
  }

  @Nonnull
  public static KeepModifier combineModifiers(
      @Nonnull KeepModifier modifier1, @Nonnull KeepModifier modifier2) {
    return new KeepModifier(modifier1.modifier & modifier2.modifier);
  }
}
