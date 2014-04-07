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

import com.google.common.collect.ObjectArrays;

import com.android.sched.util.codec.ByteFormatter;
import com.android.sched.util.codec.Formatter;
import com.android.sched.util.codec.LongCodec;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a statistic on array allocation when statistic is not enabled.
 */
public class ArrayAlloc extends Statistic {
  protected ArrayAlloc(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  /**
   * Record an array allocation.
   *
   * @param count number of element if it is an array.
   * @param size size in bytes of object in memory.
   */
  public void recordObjectAllocation(@Nonnegative int count, @Nonnegative long size) {
  }

  @Override
  public void merge(@Nonnull Statistic statistic) {
    throw new AssertionError();
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "Array allocation";
  }

  @Nonnull
  private static final String[] HEADER;

  static {
    HEADER = ObjectArrays.concat(new String[] {
        "Array count",
        "Total size",
      }, SampleImpl.getStaticHeader(), String.class);
  }

  @Override
  @Nonnull
  public String[] getHeader() {
    return HEADER.clone();
  }

  @Override
  @Nonnull
  public Formatter<?>[] getFormatters() {
    return ObjectArrays.concat(new Formatter<?>[] {
        new LongCodec(),
        new ByteFormatter()
      }, SampleImpl.getStaticFormatters(), Formatter.class);
  }

  @Override
  @Nonnegative
  public int getColumnCount() {
    return HEADER.length;
  }
}
