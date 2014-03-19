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

import com.android.sched.util.table.DataHeader;
import com.android.sched.util.table.DataRow;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Simple statistic computation on a set of values.
 */
public class SampleImpl extends Sample implements DataRow, DataHeader {
  private long   count;

  private double min = Double.POSITIVE_INFINITY;
  @CheckForNull
  private Object minObject;

  private double max = Double.NEGATIVE_INFINITY;
  @CheckForNull
  private Object maxObject;
  private double total;

  protected SampleImpl(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  @Override
  public synchronized void add(double value, @CheckForNull Object obj) {
    if (!Double.isNaN(value)) {
      if (value < min) {
        min = value;
        minObject = obj;
      }
      if (value > max) {
        max = value;
        maxObject = obj;
      }

      total += value;
    }

    count++;
  }

  @Override
  public synchronized void merge(@Nonnull Statistic statistic) {
    SampleImpl samples = (SampleImpl) statistic;

    this.count += samples.count;
    this.total += samples.total;
    if (samples.min < this.min) {
      this.min = samples.min;
      this.minObject = samples.minObject;
    }
    if (samples.max > this.max) {
      this.max = samples.max;
      this.maxObject = samples.maxObject;
    }
  }

  @Nonnull
  @Override
  @Deprecated
  public Object getValue(@Nonnegative int columnIdx) {
    switch (columnIdx) {
      case 0:
        return Long.valueOf(count);
      case 1:
        return Double.valueOf(total);
      case 2:
        if (min == Double.POSITIVE_INFINITY) {
          return Double.valueOf(Double.MAX_VALUE);
        } else {
          return Double.valueOf(min);
        }
      case 3:
        if (count == 0) {
          return Long.valueOf(0);
        } else {
          return Double.valueOf(total / count);
        }
      case 4:
        if (max == Double.NEGATIVE_INFINITY) {
          return Double.valueOf(Double.MIN_VALUE);
        } else {
          return Double.valueOf(max);
        }
      case 5:
        return "";
      case 6:
        return "";
      default:
        throw new AssertionError();
    }
  }

  @Nonnull
  @Override
  @Deprecated
  public String getHumanReadableValue(@Nonnegative int columnIdx) {
    switch (columnIdx) {
      case 0:
        return Long.toString(count);
      case 1:
        return Double.toString(total);
      case 2:
        if (min == Double.POSITIVE_INFINITY) {
          return "--";
        } else {
          return Double.toString(min);
        }
      case 3:
        if (count == 0) {
          return "--";
        } else {
          return Double.toString(total / count);
        }
      case 4:
        if (max == Double.NEGATIVE_INFINITY) {
          return "--";
        } else {
            return Double.toString(max);
        }
      case 5:
        if (minObject == null) {
          return "--";
        } else {
          return minObject.toString();
        }
      case 6:
        if (maxObject == null) {
          return "--";
        } else {
          return maxObject.toString();
        }
      default:
        throw new AssertionError();
    }
  }

  @Nonnull
  @Override
  public Iterator<Object> iterator() {
    return Iterators.forArray(
        Long.valueOf(count),
        Double.valueOf(total),
        Double.valueOf(min),
        Double.valueOf(total / count),
        Double.valueOf(max),
        minObject,
        maxObject);
  }
}
