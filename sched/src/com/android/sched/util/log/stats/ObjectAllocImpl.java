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

import com.android.sched.util.log.tracer.probe.MemoryBytesProbe;
import com.android.sched.util.table.DataHeader;
import com.android.sched.util.table.DataRow;

import java.util.Iterator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a statistic on object allocation.
 */
public class ObjectAllocImpl extends ObjectAlloc implements DataRow, DataHeader {
  @Nonnegative
  private long number;
  @Nonnegative
  private long size = 0;

  protected ObjectAllocImpl(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  /**
   * Record an object allocation.
   *
   * @param size size in bytes of object in memory.
   */
  @Override
  public synchronized void recordObjectAllocation(@Nonnegative long size) {
    this.number++;
    if (this.size == 0) {
      this.size = size;
    } else {
      assert this.size == size : "Object size are not constant";
    }
  }

  @Override
  public synchronized void merge(@Nonnull Statistic statistic) {
    ObjectAllocImpl stat = (ObjectAllocImpl) statistic;

    synchronized (stat) {
      this.number += stat.number;
      if (this.size == 0) {
        this.size = stat.size;
      } else {
        assert this.size == stat.size : "Object size are not constant";
      }
    }
  }

  @Override
  @Nonnull
  @Deprecated
  public synchronized Object getValue(@Nonnegative int columnIdx) {
    switch (columnIdx) {
      case 0:
        return Long.valueOf(number);
      case 1:
        return Long.valueOf(size);
      case 2:
        return Long.valueOf(size * number);
      default:
        throw new AssertionError();
    }
  }

  @Override
  @Nonnull
  @Deprecated
  public synchronized String getHumanReadableValue(@Nonnegative int columnIdx) {
    switch (columnIdx) {
      case 0:
        return Long.toString(number);
      case 1:
        return MemoryBytesProbe.formatBytes(size);
      case 2:
        return MemoryBytesProbe.formatBytes(size * number);
      default:
        throw new AssertionError();
    }
  }

  @Override
  @Nonnull
  public synchronized Iterator<Object> iterator() {
    return Iterators.<Object> forArray(
        Long.valueOf(number),
        Long.valueOf(size),
        Long.valueOf(size * number));
  }
}
