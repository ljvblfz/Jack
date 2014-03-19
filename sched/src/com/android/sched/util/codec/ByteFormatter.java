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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.annotation.Nonnull;

/**
 * Class to format a {@link Long} representing a quantity in bytes to a {@link String}.
 */
public class ByteFormatter implements Formatter<Long> {

  @Nonnull
  private static final char[] UNIT_PREFIX_SI  = new char[]{'k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'};
  @Nonnull
  private static final char[] UNIT_PREFIX_IEC = new char[]{'K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'};

  @Nonnull
  private char[]  prefix;
  private boolean si;
  private int     base;

  @Nonnull
  private DecimalFormat formatter = (DecimalFormat) NumberFormat.getIntegerInstance();

  public ByteFormatter() {
    si = true;
    prefix = UNIT_PREFIX_SI;
    base = 1000;
    formatter.setMaximumFractionDigits(4);
  }

  @Nonnull
  public ByteFormatter setSI() {
    si = true;
    prefix = UNIT_PREFIX_SI;
    base = 1000;

    return this;
  }

  @Nonnull
  public ByteFormatter setIEC() {
    si = false;
    prefix = UNIT_PREFIX_IEC;
    base = 1024;

    return this;
  }

  @Nonnull
  public DecimalFormat getNumberFormatter() {
    return formatter;
  }

  public void setNumberFormatter(@Nonnull DecimalFormat formatter) {
    this.formatter = formatter;
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull Long data) {
    StringBuilder sb = new StringBuilder();
    long value = data.longValue();
    boolean negative = value < 0;

    if (negative) {
      sb.append(formatter.getNegativePrefix());
      value = -value;
    }

    if (value < base) {
      sb.append(value);
      sb.append(' ');
    } else {
      int exp = (int) (Math.log(value) / Math.log(base));
      sb.append(formatter.format(Double.valueOf(value / Math.pow(base, exp))));
      sb.append(' ');
      sb.append(prefix[exp - 1]);
      if (!si) {
        sb.append('i');
      }
    }
    sb.append('B');

    if (negative) {
      sb.append(formatter.getNegativeSuffix());
    }

    return sb.toString();
  }
}
