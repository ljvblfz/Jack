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

package com.android.sched.util.log.tracer.probe;


import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Abstract class for {@link Probe} which is base on Nanoseconds value.
 */
public abstract class TimeNanosProbe extends Probe {
  private static final TimeUnit[] UNIT_TIME = new TimeUnit[]{
    TimeUnit.DAYS,
    TimeUnit.HOURS,
    TimeUnit.MINUTES,
    TimeUnit.SECONDS,
    TimeUnit.MILLISECONDS,
    TimeUnit.MICROSECONDS,
    TimeUnit.NANOSECONDS
  };
  private static final String[]   UNIT_PREFIX = {
    "d", "h", "min", "s", "ms", "µs", "ns"
  };
  private static final boolean PRECISE = false;
  private static final String  FORMAT  = (PRECISE) ? "%f" : "%.4f";

  protected TimeNanosProbe(@Nonnull String description, @Nonnegative int priority) {
    super(description, priority);
  }

  @Override
  @Nonnull
  public String formatValue(long ns) {
    return formatDuration(ns);
  }

  @Nonnull
  public static String formatDuration(long ns) {
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
      double u = (double) ns / TimeUnit.NANOSECONDS.convert(1, unit);
      if (u >= 1) {
        sb.append(String.format(FORMAT, Double.valueOf(u)));
        sb.append(' ');
        sb.append(UNIT_PREFIX[idx]);
        break;
      }
      idx++;
    }

    return sb.toString();
  }
}

