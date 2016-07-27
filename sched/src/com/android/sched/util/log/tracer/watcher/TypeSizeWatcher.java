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
import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.util.log.stats.TypeSize;
import com.android.sched.util.log.stats.TypeSizeImpl;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Class to watch {@link Object} creation and make a global statistic about allocation.
 */
public class TypeSizeWatcher implements ObjectWatcher<Object> {
  static class Statistics implements ObjectWatcher.Statistics {
    @Override
    public Iterator<Statistic> iterator() {
      throw new AssertionError();
    }
  }

  @Nonnull
  private static final StatisticId<TypeSize> SIZES = new StatisticId<TypeSize>(
      "jack.allocation.type",
      "Type size",
      TypeSizeImpl.class, TypeSize.class);

  @Override
  public boolean notifyInstantiation(
      @Nonnull Object object,
      @Nonnegative long size,
      int count,
      @Nonnull EventType notUsed,
      @CheckForNull StackTraceElement site) {
    try {
      TracerFactory.getTracer().getStatistic(SIZES).recordType(size);
    } catch (RuntimeException e) {
      // Do best effort here
    }

    return false;
  }

  @Override
  @Nonnull
  public ObjectWatcher.Statistics addSample(@Nonnull Object node,
      @CheckForNull ObjectWatcher.Statistics raw, @Nonnull EventType type) {
    throw new AssertionError();
  }

  /**
   * Install a {@link TypeSizeWatcher}
   */
  @ImplementationName(iface = WatcherInstaller.class, name = "type-size",
      description = "record type size")
  public static class AllocationWatcherInstaller implements WatcherInstaller {
    @Override
    public void install(@Nonnull Tracer tracer) {
      tracer.registerWatcher(Object.class, TypeSizeWatcher.class);
    }
  }
}
