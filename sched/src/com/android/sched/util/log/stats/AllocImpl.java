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

import com.android.sched.util.log.tracer.probe.MemoryBytesProbe;
import com.android.sched.util.table.DataHeader;
import com.android.sched.util.table.DataRow;

import java.util.Iterator;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a statistic on object allocation.
 */
public class AllocImpl extends Alloc implements DataRow, DataHeader {
  @Nonnegative
  private long number;
  @Nonnegative
  private long size = 0;

  protected AllocImpl(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  /**
   * Record an object allocation.
   *
   * @param size size in bytes of object in memory.
   */
  @Override
  public synchronized void recordAllocation(@Nonnegative long size) {
    this.number++;
    this.size += size;
  }

  @Override
  public synchronized void merge(@Nonnull Statistic statistic) {
    AllocImpl stat = (AllocImpl) statistic;

    synchronized (stat) {
      this.number += stat.number;
      this.size   += stat.size;
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
      default:
        throw new AssertionError();
    }
  }

  @Override
  @Nonnull
  public synchronized Iterator<Object> iterator() {
    return Iterators.<Object> forArray(
        Long.valueOf(number),
        Long.valueOf(size));
  }
}
