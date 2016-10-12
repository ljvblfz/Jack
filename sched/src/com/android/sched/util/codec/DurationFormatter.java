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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Class to format a {@link Long} representing a duration as a {@link String}.
 */
public class DurationFormatter implements Formatter<Long> {
  @Nonnull
  private static final TimeUnit[] UNIT_TIME = new TimeUnit[]{
      TimeUnit.DAYS,
      TimeUnit.HOURS,
      TimeUnit.MINUTES,
      TimeUnit.SECONDS,
      TimeUnit.MILLISECONDS,
      TimeUnit.MICROSECONDS,
      TimeUnit.NANOSECONDS
    };

  @Nonnull
  private static final String[]   UNIT_PREFIX = {
    "d", "h", "min", "s", "ms", "µs", "ns"
  };

  @Nonnull
  private TimeUnit ref = TimeUnit.NANOSECONDS;

  @Nonnull
  private NumberFormat formatter;

  public DurationFormatter() {
    formatter = NumberFormat.getIntegerInstance();
    formatter.setMaximumFractionDigits(4);
  }

  public DurationFormatter(@Nonnull Locale locale) {
    formatter = NumberFormat.getIntegerInstance(locale);
    formatter.setMaximumFractionDigits(4);
  }

  @Nonnull
  public NumberFormat getNumberFormatter() {
    return formatter;
  }

  @Nonnull
  public DurationFormatter setNumberFormatter(@Nonnull DecimalFormat formatter) {
    this.formatter = formatter;

    return this;
  }

  @Nonnull
  public DurationFormatter setInputUnit(@Nonnull TimeUnit unit) {
    this.ref = unit;

    return this;
  }

  @Nonnull
  public DurationFormatter setPrecise() {
    this.formatter.setMaximumFractionDigits(340);

    return this;
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull Long data) {
    long ns = data.longValue();

    if (ns == 0) {
      return "0";
    }

    StringBuilder sb = new StringBuilder();

    if (ns < 0) {
      sb.append('-');
      ns = -ns;
    }

    int idx = 0;
    for (TimeUnit unit : UNIT_TIME) {
      double u = (double) ns / ref.convert(1, unit);
      if (u >= 1) {
        sb.append(formatter.format(Double.valueOf(u)));
        sb.append(' ');
        sb.append(UNIT_PREFIX[idx]);
        break;
      }
      idx++;
    }

    return sb.toString();
  }
}
