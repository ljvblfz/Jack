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

import com.google.common.collect.Iterators;

import com.android.sched.util.print.DataType;
import com.android.sched.util.print.DataView;
import com.android.sched.util.print.DataViewBuilder;

import java.util.Iterator;

import javax.annotation.CheckForNull;
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
   * @param object marker object for that allocation.
   */
  public void recordAllocation(@Nonnegative int count, @Nonnegative long size,
      @CheckForNull Object object) {
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

  @Nonnegative
  public long getNumber() {
    return 0;
  }

  @Nonnegative
  public long getSize() {
    return 0;
  }

  @Override
  @Nonnull
  public synchronized Iterator<Object> iterator() {
    return Iterators.concat(
        Iterators.forArray(
            Long.valueOf(getNumber()),
            Long.valueOf(getSize())),
            new Sample(getId()).iterator());
  }

  @Nonnull
  private static final DataView DATA_VIEW = DataViewBuilder.getStructure()
      .addField("arrayCount", DataType.NUMBER)
      .addField("arrayTotalCount", DataType.QUANTITY)
      .addDataView(SampleImpl.getStaticDataView())
      .build();

  @Override
  @Nonnull
  public DataView getDataView() {
    return DATA_VIEW;
  }
}
