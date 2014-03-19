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

import com.android.sched.util.codec.Formatter;
import com.android.sched.util.codec.LongCodec;
import com.android.sched.util.codec.PercentFormatter;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a counter statistic when statistic is not enabled.
 */
public class Percent extends Statistic {
  protected Percent(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  public void addTrue() {
  }

  public void addFalse() {
  }

  public void add(boolean value) {
  }

  public double getPercent() {
    return Double.NaN;
  }

  @Override
  public void merge(@Nonnull Statistic statistic) {
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
        return "Percent";
      case 1:
        return "Number";
      case 2:
        return "Total";
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
      default:
        throw new AssertionError();
    }
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "Percent";
  }


  @Nonnull
  private static final String[] HEADER = new String[] {
    "Percent",
    "Number",
    "Total"
  };

  @Override
  @Nonnegative
  public int getColumnCount() {
    return HEADER.length;
  }

  @Override
  @Nonnull
  public String[] getHeader() {
    return HEADER.clone();
  }

  @Override
  @Nonnull
  public Formatter<?>[] getFormatters() {
    return new Formatter<?>[] {
        new PercentFormatter(),
        new LongCodec(),
        new LongCodec()
    };
  }
}
