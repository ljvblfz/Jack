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

import com.google.common.collect.Iterators;

import com.android.sched.util.print.DataType;
import com.android.sched.util.print.DataView;
import com.android.sched.util.print.DataViewBuilder;

import java.util.Iterator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Extended statistic computation on a set of values when statistic is not enabled. Have Median,
 * First quartile, Third quartile.
 */
public class ExtendedSample extends Statistic {
  protected ExtendedSample(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  public void add(double value) {
  }

  @Nonnegative
  public int getCount() {
    return 0;
  }

  public double getTotal() {
    return 0;
  }

  public double getMin() {
    return Double.NaN;
  }

  public double getAverage() {
    return Double.NaN;
  }

  public double getMax() {
    return Double.NaN;
  }

  public double getFirstQuartile() {
    return Double.NaN;
  }

  public double getMedian() {
    return Double.NaN;
  }

  public double getThirdQuartile() {
    return Double.NaN;
  }

  @Override
  public void merge(@Nonnull Statistic statistic) {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "Sample";
  }

  protected void ensureSorted() {
  }

  @Override
  @Nonnull
  public synchronized Iterator<Object> iterator() {
    ensureSorted();

    return Iterators.<Object>forArray(
           Long.valueOf(getCount()),
           Double.valueOf(getTotal()),
           Double.valueOf(getMin()),
           Double.valueOf(getAverage()),
           Double.valueOf(getFirstQuartile()),
           Double.valueOf(getMedian()),
           Double.valueOf(getThirdQuartile()),
           Double.valueOf(getMax()));
  }

  @Nonnull
  private static final DataView DATA_VIEW = DataViewBuilder.getStructure()
      .addField("sampleCount", DataType.NUMBER)
      .addField("sampleTotal", DataType.NUMBER)
      .addField("sampleMin", DataType.NUMBER)
      .addField("sampleAverage", DataType.NUMBER)
      .addField("sampleFirstQuartile", DataType.NUMBER)
      .addField("sampleMedian", DataType.NUMBER)
      .addField("sampleThirdQuartile", DataType.NUMBER)
      .addField("sampleMax", DataType.NUMBER)
      .build();

  @Override
  @Nonnull
  public DataView getDataView() {
    return DATA_VIEW;
  }
}
