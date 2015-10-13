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

package com.android.sched.util.codec;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.annotation.Nonnull;

/**
 * This {@link Formatter} is used to format a instance of {@link Number} as a {@link String}.
 */
public class NumberFormatter implements Formatter<Number> {
  @Nonnull
  private NumberFormat formatter = NumberFormat.getIntegerInstance();

  @Nonnull
  public NumberFormat getNumberFormatter() {
    return formatter;
  }

  @Nonnull
  public NumberFormatter setNumberFormatter(@Nonnull DecimalFormat formatter) {
    this.formatter = formatter;

    return this;
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull Number n) {
    return formatter.format(n);
  }
}