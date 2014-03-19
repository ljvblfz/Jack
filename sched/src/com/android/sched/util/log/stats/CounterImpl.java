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

import com.android.sched.util.table.DataRow;

import java.util.Iterator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


/**
 * Represents a counter statistic.
 */
public class CounterImpl extends Counter implements DataRow {
  private long value;

  protected CounterImpl(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  @Override
  public synchronized void incValue() {
    this.value++;
  }

  @Override
  public synchronized void incValue(long value) {
    this.value += value;
  }

  @Override
  public synchronized void decValue() {
    this.value--;
  }

  @Override
  public synchronized void decValue(long value) {
    this.value -= value;
  }

  @Override
  public synchronized void mulValue(long value) {
    this.value *= value;
  }

  @Override
  public synchronized void divValue(long value) {
    this.value /= value;
  }

  @Override
  public synchronized long getValue() {
    return value;
  }

  @Override
  public synchronized void merge(@Nonnull Statistic statistic) {
    CounterImpl counter = (CounterImpl) statistic;

    synchronized (counter) {
      this.value += counter.value;
    }
  }

  @Override
  @Nonnull
  @Deprecated
  public Object getValue(@Nonnegative int columnIdx) {
    assert columnIdx == 0;

    return Long.valueOf(value);
  }

  @Override
  @Nonnull
  @Deprecated
  public String getHumanReadableValue(@Nonnegative int columnIdx) {
    assert columnIdx == 0;

    return Long.valueOf(value).toString();
  }

  @Override
  @Nonnull
  public synchronized Iterator<Object> iterator() {
    return Iterators.<Object> forArray(Long.valueOf(value));
  }
}
