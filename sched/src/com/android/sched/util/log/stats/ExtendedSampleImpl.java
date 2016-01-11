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

import java.util.Arrays;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Extended statistic computation on a set of values. Have Median, First quartile, Third quartile.
 */
public class ExtendedSampleImpl extends ExtendedSample {
  private static final int INITIAL_CAPACITY = 16;
  private static final int INCREMENT        = 0;

  @Nonnull
  protected double[] samples  = new double[INITIAL_CAPACITY];
  @Nonnegative
  private   int      validCount;

  @Nonnegative
  private int     nanCount;
  private double  total;
  private boolean isSorted = true;

  private double min = Double.POSITIVE_INFINITY;
  @CheckForNull
  private Object minObject;

  private double max = Double.NEGATIVE_INFINITY;
  @CheckForNull
  private Object maxObject;

  @Nonnegative
  private final int increment;

  public ExtendedSampleImpl(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);

    this.increment = INCREMENT;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public synchronized void add(double value) {
    add(value, null);
  }

  @Override
  public synchronized void add(double value, @CheckForNull Object obj) {
    if (!Double.isNaN(value)) {
      ensureCapacity(validCount);

      samples[validCount++] = value;
      isSorted = false;

      if (value < min) {
        min = value;
        minObject = obj;
      }
      if (value > max) {
        max = value;
        maxObject = obj;
      }

      total += value;
    } else {
      nanCount++;
    }
  }

  @Override
  @Nonnegative
  public int getCount() {
    return validCount;
  }

  @Override
  @Nonnegative
  public int getNaNCount() {
    return nanCount;
  }

  @Override
  public double getTotal() {
    return total;
  }

  @Override
  public synchronized double getMin() {
    return min;
  }

  @Override
  public synchronized double getAverage() {
    return total / validCount;
  }

  @Override
  public synchronized double getMax() {
    return max;
  }

  @Override
  @CheckForNull
  public Object getMinObject() {
    return minObject;

  }

  @Override
  @CheckForNull
  public Object getMaxObject() {
    return maxObject;
  }

  @Override
  public synchronized double getFirstQuartile() {
    return getNth(1, 4);
  }

  @Override
  public synchronized double getMedian() {
    return getNth(1, 2);
  }

  @Override
  public synchronized double getThirdQuartile() {
    return getNth(3, 4);
  }

  @Override
  public synchronized void merge(@Nonnull Statistic statistic) {
    ExtendedSampleImpl samples = (ExtendedSampleImpl) statistic;

    synchronized (samples) {
      ensureCapacity(validCount + samples.validCount);

      System.arraycopy(samples.samples, 0, this.samples, validCount, samples.validCount);
      total += samples.total;
      validCount += samples.validCount;
      nanCount += samples.nanCount;
      isSorted = false;

      if (samples.min < this.min) {
        this.min = samples.min;
        this.minObject = samples.minObject;
      }
      if (samples.max > this.max) {
        this.max = samples.max;
        this.maxObject = samples.maxObject;
      }
    }
  }

  @Override
  protected void ensureSorted() {
    if (!isSorted) {
      Arrays.sort(samples, 0, validCount);
      isSorted = true;
    }
  }

  private void ensureCapacity (@Nonnegative int goal) {
    if (goal >= samples.length) {
      int newLength;

      if (increment <= 0) {
        newLength = samples.length;
        while (goal >= newLength) {
          newLength = newLength * 2 + 1;
        }
      } else {
        // newLnegth is index aligned on INCREMENT
        newLength = ((goal + INCREMENT) / INCREMENT) * INCREMENT;
      }

      double[] newArray = new double[newLength];
      System.arraycopy(samples, 0, newArray, 0, validCount);

      samples = newArray;
    }
  }

  private double getNth(int n, int d) {
    ensureSorted();

    if (validCount == 0) {
      return Double.NaN;
    }

    if (validCount == 1) {
      return samples[0];
    }

    double pos   = (double) (n * (validCount + 1)) / (double) d;

    if (pos < 1.0) {
      return samples[0];
    }

    double floor = Math.floor(pos);
    double diff  = pos - floor;
    double vLow  = samples[(int) pos - 1];

    if (diff == 0) {
      return vLow;
    } else {
      double vHigh = samples[(int) pos];
      return vLow + diff * (vHigh - vLow);
    }
  }
}
