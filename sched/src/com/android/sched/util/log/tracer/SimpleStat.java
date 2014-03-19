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

package com.android.sched.util.log.tracer;

/**
 * Simple statistic computation on a set of values.
 */
public class SimpleStat {
  private long   count;

  private double min = Float.POSITIVE_INFINITY;
  private Object minObject;

  private double max = Float.NEGATIVE_INFINITY;
  private Object maxObject;
  private double total;
  private double squareSum;

  public SimpleStat() {
    clear();
  }

  public void add(double value, Object obj) {
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

      squareSum += value * value;
    }

    count++;
  }

  public long getCount() {
    return count;
  }

  public double getMin() {
    return min;
  }

  public Object getMinObject() {
    return minObject;
  }

  public double getMax() {
    return max;
  }

  public Object getMaxObject() {
    return maxObject;
  }

  public double getAverage() {
    return total / count;
  }

  public double getTotal() {
    return total;
  }

  public double getVariance() {
    double d;
    double average = getAverage();

    d = squareSum / count;
    d -= average * average;

    return Math.sqrt(d);
  }

  public void clear() {
    count = 0;
    squareSum = 0;
  }
}
