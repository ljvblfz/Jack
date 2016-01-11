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
 * Simple statistic computation on a set of values when statistic is not enabled.
 */
public class Sample extends Statistic {
  protected Sample(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  public void add(double value) {
  }

  public void add(double value, @CheckForNull Object obj) {
  }

  @Override
  public void merge(@Nonnull Statistic statistic) {
    throw new AssertionError();
  }

  @Nonnegative
  public int getCount() {
    return 0;
  }

  @Nonnegative
  public int getNaNCount() {
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

  @CheckForNull
  public Object getMinObject() {
    return null;

  }

  @CheckForNull
  public Object getMaxObject() {
    return null;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "Sample";
  }


  @Nonnull
  @Override
  public synchronized Iterator<Object> iterator() {
    return Iterators.forArray(
        Integer.valueOf(getCount()),
        Integer.valueOf(getNaNCount()),
        Double.valueOf(getTotal()),
        Double.valueOf(getMin()),
        Double.valueOf(getAverage()),
        Double.valueOf(getMax()),
        getMinObject(),
        getMaxObject());
  }

  @Nonnull
  private static final DataView DATA_VIEW = DataViewBuilder.getStructure()
      .addField("sampleCount", DataType.NUMBER)
      .addField("sampleNaNCount", DataType.NUMBER)
      .addField("sampleTotal", DataType.NUMBER)
      .addField("sampleMin", DataType.NUMBER)
      .addField("sampleAverage", DataType.NUMBER)
      .addField("sampleMax", DataType.NUMBER)
      .addField("sampleMinMarker", DataType.STRING)
      .addField("sampleMaxMarker", DataType.STRING)
      .build();

  @Override
  @Nonnull
  public DataView getDataView() {
    return DATA_VIEW;
  }

  @Nonnull
  static DataView getStaticDataView() {
    return DATA_VIEW;
  }
}
