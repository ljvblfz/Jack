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

  public void removeTrue() {
  }

  public void removeFalse() {
  }

  public void remove(boolean value) {
  }

  public double getPercent() {
    return Double.NaN;
  }

  public long getTotal() {
    return 0;
  }

  public long getTrueCount() {
    return 0;
  }

  @Override
  public void merge(@Nonnull Statistic statistic) {
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "Percent";
  }

  @Nonnull
  @Override
  public synchronized Iterator<Object> iterator() {
    return Iterators.<Object>forArray(
        Double.valueOf(getPercent()),
        Long.valueOf(getTrueCount()),
        Long.valueOf(getTotal()));
  }

  @Nonnull
  private static final DataView DATA_VIEW = DataViewBuilder.getStructure()
      .addField("percent", DataType.PERCENT)
      .addField("count", DataType.NUMBER)
      .addField("total", DataType.NUMBER)
      .build();

  @Override
  @Nonnull
  public DataView getDataView() {
    return DATA_VIEW;
  }
}
