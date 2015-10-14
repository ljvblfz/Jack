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
public class Counter extends Statistic {
  protected Counter(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  public void incValue() {
  }

  public void incValue(long value) {
  }

  public void decValue() {
  }

  public void decValue(long value) {
  }

  public void mulValue(long value) {
  }

  public void divValue(long value) {
  }

  public long getValue() {
    return 0;
  }

  @Override
  public void merge(@Nonnull Statistic statistic) {
    throw new AssertionError();
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "Counter";
  }

  @Override
  @Nonnull
  public synchronized Iterator<Object> iterator() {
    return Iterators.<Object> forArray(Long.valueOf(getValue()));
  }

  @Nonnull
  private static final DataView DATA_VIEW = DataViewBuilder.getStructure()
      .addField("count", DataType.NUMBER)
      .build();

  @Override
  @Nonnull
  public DataView getDataView() {
    return DATA_VIEW;
  }
}
