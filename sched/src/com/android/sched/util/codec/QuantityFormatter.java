/*
 * Copyright (C) 2014 The Android Open Source Project
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

import java.text.NumberFormat;
import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * Class to format a {@link Long} representing a quantity in bytes as a {@link String}.
 */
public class QuantityFormatter implements Formatter<Long> {

  @Nonnull
  private static final char[] UNIT_PREFIX_SI  = new char[]{'k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'};
  @Nonnull
  private static final char[] UNIT_PREFIX_IEC = new char[]{'K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'};

  @Nonnull
  private char[]  prefix;
  @Nonnull
  private String unit = "";
  private boolean si;
  private int     base;

  @Nonnull
  private NumberFormat formatter;

  public QuantityFormatter() {
    si = true;
    prefix = UNIT_PREFIX_SI;
    base = 1000;
    formatter = NumberFormat.getIntegerInstance();
    formatter.setMaximumFractionDigits(4);
    formatter.setGroupingUsed(false);
  }

  public QuantityFormatter(@Nonnull Locale locale) {
    si = true;
    prefix = UNIT_PREFIX_SI;
    base = 1000;
    formatter = NumberFormat.getIntegerInstance(locale);
    formatter.setMaximumFractionDigits(4);
    formatter.setGroupingUsed(false);
  }

  @Nonnull
  public QuantityFormatter setSI() {
    si = true;
    prefix = UNIT_PREFIX_SI;
    base = 1000;

    return this;
  }

  @Nonnull
  public QuantityFormatter setUnit(@Nonnull String unit) {
    this.unit = unit;

    return this;
  }

  @Nonnull
  public QuantityFormatter setIEC() {
    si = false;
    prefix = UNIT_PREFIX_IEC;
    base = 1024;

    return this;
  }

  @Nonnull
  public QuantityFormatter setPrecise() {
    this.formatter.setMaximumFractionDigits(340);

    return this;
  }

  @Nonnull
  public NumberFormat getNumberFormatter() {
    return formatter;
  }

  @Nonnull
  public QuantityFormatter setNumberFormatter(@Nonnull NumberFormat formatter) {
    this.formatter = formatter;

    return this;
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull Long data) {
    StringBuilder sb = new StringBuilder();
    long value = data.longValue();
    long absValue = (value < 0) ? -value : value;

    if (absValue < base) {
      sb.append(formatter.format(value));
      if (!unit.isEmpty()) {
        sb.append(' ');
      }
    } else {
      int exp = (int) (Math.log(absValue) / Math.log(base));
      double d = Double.valueOf(absValue / Math.pow(base, exp)).doubleValue();
      sb.append(formatter.format((value < 0) ? -d : d));
      sb.append(' ');
      sb.append(prefix[exp - 1]);
      if (!si) {
        sb.append('i');
      }
    }

    sb.append(unit);

    return sb.toString();
  }
}
