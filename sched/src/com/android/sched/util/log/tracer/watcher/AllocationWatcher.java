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

package com.android.sched.util.log.tracer.watcher;

import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.log.EventType;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Alloc;
import com.android.sched.util.log.stats.AllocImpl;
import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Class to watch {@link Object} creation and make a global statistic about allocation.
 */
public class AllocationWatcher implements ObjectWatcher<Object> {
  static class Statistics implements ObjectWatcher.Statistics {
    @Override
    public Iterator<Statistic> iterator() {
      throw new AssertionError();
    }
  }

  @Nonnull
  private static final StatisticId<Alloc> ALLOCATIONS = new StatisticId<Alloc>(
      "jack.allocation.object.total",
      "Total object and array allocations",
      AllocImpl.class, Alloc.class);

  @Override
  public boolean notifyInstantiation(
      @Nonnull Object object, @Nonnegative long size, int count, @Nonnull EventType notUsed) {
    Class<?> type = object.getClass();

    if (count == -1) {
      notifyObject(type, size);
    } else {
      notifyArray(type, size, count);
    }

    return false;
  }

  private void notifyObject(@Nonnull Class<?> type, @Nonnegative long size) {
    try {
      TracerFactory.getTracer().getStatistic(ALLOCATIONS).recordAllocation(size);
    } catch (RuntimeException e) {
      // Do best effort here
    }
  }

  private synchronized void notifyArray(@Nonnull Class<?> type, @Nonnegative long size,
      @Nonnegative int count) {
    try {
      TracerFactory.getTracer().getStatistic(ALLOCATIONS).recordAllocation(size);
    } catch (RuntimeException e) {
      // Do best effort here
    }
  }

  @Override
  @Nonnull
  public ObjectWatcher.Statistics addSample(@Nonnull Object node,
      @CheckForNull ObjectWatcher.Statistics raw, @Nonnull EventType type) {
    throw new AssertionError();
  }

  /**
   * Install a {@link AllocationWatcher}
   */
  @ImplementationName(iface = WatcherInstaller.class, name = "object-alloc")
  public static class AllocationWatcherInstaller implements WatcherInstaller {
    @Override
    public void install(@Nonnull Tracer tracer) {
      tracer.registerWatcher(Object.class, AllocationWatcher.class);
    }
  }
}
