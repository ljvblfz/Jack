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

package com.android.sched.util.log.stats;

import com.android.sched.util.codec.ByteFormatter;
import com.android.sched.util.codec.Formatter;
import com.android.sched.util.codec.LongCodec;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a statistic on object allocation when statistic is not enabled.
 */
public class Alloc extends Statistic {
  protected Alloc(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  /**
   * Record an object allocation.
   *
   * @param size size in bytes of object in memory.
   */
  public void recordAllocation(@Nonnegative long size) {
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
        return "Size";
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
      default:
        throw new AssertionError();
    }
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "Allocation";
  }


  @Nonnull
  private static final String[] HEADER = new String[] {
    "Count",
    "Size",
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
        new LongCodec(),
        new ByteFormatter()
    };
  }
}
