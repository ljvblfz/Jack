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


import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


/**
 * Abstract class for {@link Probe} which is base on Bytes value.
 */
public abstract class MemoryBytesProbe extends Probe {
  private static final boolean UNIT_IN_SI  = false;
  private static final int     UNIT_BASE   = (UNIT_IN_SI) ? 1000 : 1024;
  private static final char[]  UNIT_PREFIX = (UNIT_IN_SI)
                                             ? new char[]{'k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'}
                                             : new char[]{'K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'};
  private static final boolean PRECISE     = false;
  private static final String  FORMAT      = (PRECISE) ? "%f" : "%.4f";

  protected MemoryBytesProbe(@Nonnull String description, @Nonnegative int priority) {
    super(description, priority);
  }

  @Override
  @Nonnull
  public String formatValue(long b) {
    return formatBytes(b);
  }

  @Nonnull
  public static String formatBytes(long b) {
    StringBuilder sb = new StringBuilder();

    if (b < 0) {
      sb.append('-');
      b = -b;
    }

    if (b < UNIT_BASE) {
      sb.append(b);
      sb.append(' ');
    } else {
      int exp = (int) (Math.log(b) / Math.log(UNIT_BASE));
      sb.append(String.format(FORMAT, Double.valueOf(b / Math.pow(UNIT_BASE, exp))));
      sb.append(' ');
      sb.append(UNIT_PREFIX[exp - 1]);
      if (!UNIT_IN_SI) {
        sb.append('i');
      }
    }
    sb.append('B');

    return sb.toString();
  }
}
