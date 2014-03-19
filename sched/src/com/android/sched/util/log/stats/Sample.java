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

package com.android.sched.util.log.stats;

import com.android.sched.util.codec.DoubleCodec;
import com.android.sched.util.codec.Formatter;
import com.android.sched.util.codec.LongCodec;
import com.android.sched.util.codec.ToStringFormatter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Simple statistic computation on a set of values.
 */
public class Sample extends Statistic {
  protected Sample(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  public void add(double value, @CheckForNull Object obj) {
  }

  @Override
  public void merge(@Nonnull Statistic statistic) {
    throw new AssertionError();
  }

  @Override
  @Nonnull
  @Deprecated
  public Object getValue(@Nonnegative int columnIdx) {
    throw new AssertionError();
  }

  @Override
  @Nonnull
  @Deprecated
  public String getHumanReadableValue(@Nonnegative int columnIdx) {
    throw new AssertionError();
  }

  @Override
  @Nonnull
  @Deprecated
  public String getDescription(@Nonnegative int columnIdx) {
    switch (columnIdx) {
      case 0:
        return "Count";
      case 1:
        return "Total";
      case 2:
        return "Min";
      case 3:
        return "Average";
      case 4:
        return "Max";
      case 5:
        return "Min Marker";
      case 6:
        return "Max Marker";
      default:
        throw new AssertionError();
    }
  }

  @Override
  @Nonnull
  @Deprecated
  public String getType(@Nonnegative int columnIdx) {
    switch (columnIdx) {
      case 0:
        return "number";
      case 1:
        return "number";
      case 2:
        return "number";
      case 3:
        return "number";
      case 4:
        return "number";
      case 5:
        return "string";
      case 6:
        return "string";
      default:
        throw new AssertionError();
    }
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "Sample";
  }


  @Nonnegative
  private static final String[] HEADER = new String[] {
    "Count",
    "Total",
    "Min",
    "Average",
    "Max",
    "Min Marker",
    "Max Marker",
  };

  @Override
  @Nonnull
  public String[] getHeader() {
    return HEADER.clone();
  }

  @Nonnull
  public static String[] getStaticHeader() {
    return HEADER.clone();
  }

  @Nonnull
  public static Formatter<? extends Object>[] getStaticFormatters() {
    return new Formatter<?>[] {
        new LongCodec(),
        new DoubleCodec(),
        new DoubleCodec(),
        new DoubleCodec(),
        new DoubleCodec(),
        new ToStringFormatter(),
        new ToStringFormatter()
    };
  }

  @Override
  @Nonnull
  public Formatter<? extends Object>[] getFormatters() {
    return getStaticFormatters();
  }

  @Override
  @Nonnegative
  public int getColumnCount() {
    return HEADER.length;
  }
}
