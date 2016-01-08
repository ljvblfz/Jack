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
  protected int      count    = 0;
  private   double   total;
  private   boolean  isSorted = true;

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
    ensureCapacity(count);

    samples[count++] = value;
    total += value;
    isSorted = false;
  }

  @Override
  @Nonnegative
  public int getCount() {
    return count;
  }

  @Override
  public double getTotal() {
    return total;
  }

  @Override
  public synchronized double getMin() {
    ensureSorted();
    return samples[0];
  }

  @Override
  public synchronized double getAverage() {
    return total / count;
  }

  @Override
  public synchronized double getMax() {
    ensureSorted();
    return samples[count - 1];
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
      ensureCapacity(count + samples.count);

      System.arraycopy(samples.samples, 0, this.samples, count, samples.count);
      count += samples.count;
      total += samples.total;
      isSorted = false;
    }
  }

  @Override
  protected void ensureSorted() {
    if (!isSorted) {
      Arrays.sort(samples, 0, count);
      isSorted = true;
    }
  }

  private void ensureCapacity (@Nonnegative int index) {
    if (index >= samples.length) {
      int newLength;

      if (increment <= 0) {
        newLength = samples.length;
        while (index >= newLength) {
          newLength = newLength * 2 + 1;
        }
      } else {
        // newLnegth is index aligned on INCREMENT
        newLength = ((index + INCREMENT) / INCREMENT) * INCREMENT;
      }

      double[] newArray = new double[newLength];
      System.arraycopy(samples, 0, newArray, 0, count);

      samples = newArray;
    }
  }

  private double getNth(int n, int d) {
    ensureSorted();

    if (count == 0) {
      return Double.NaN;
    }

    if (count == 1) {
      return samples[0];
    }

    double pos   = (double) (n * (count + 1)) / (double) d;

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
