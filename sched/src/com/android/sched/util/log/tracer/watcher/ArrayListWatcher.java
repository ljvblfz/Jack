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
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.stats.Sample;
import com.android.sched.util.log.stats.SampleImpl;
import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Class to watch size and capacity of {@link ArrayList} objects.
 */
public class ArrayListWatcher implements ObjectWatcher<ArrayList<?>> {
  static class Statistics implements ObjectWatcher.Statistics {
    @Nonnull
    private static final StatisticId<Sample> INCREASE_CAPACITY = new StatisticId<Sample>(
        "sched.collection.arraylist.entry.increase",
        "Additional entries in the underlying arrays of ArrayList instances",
        SampleImpl.class, Sample.class);
    @Nonnull
    private static final StatisticId<Sample> INCREASE_UNUSED = new StatisticId<Sample>(
        "sched.collection.arraylist.entry.unused.increase",
        "Additionnal unused entries in the underlying arrays of ArrayList instances",
        SampleImpl.class, Sample.class);
    @Nonnull
    private static final StatisticId<Sample> UNUSED = new StatisticId<Sample>(
        "sched.collection.arraylist.entry.unused",
        "Unused entries in the underlying arrays of ArrayList instances",
        SampleImpl.class, Sample.class);

    @Nonnull
    private final Sample increaseCapacity;
    @Nonnull
    private final Sample increaseUnused;
    @Nonnull
    private final Sample unused;

    Statistics () {
      increaseCapacity = INCREASE_CAPACITY.newInstance();
      increaseUnused   = INCREASE_UNUSED.newInstance();
      unused           = UNUSED.newInstance();
    }

    @Override
    public Iterator<Statistic> iterator() {
      List<Statistic> list = new ArrayList<Statistic>(3);

      list.add(increaseCapacity);
      list.add(increaseUnused);
      list.add(unused);

      return list.iterator();
    }
  }

  @CheckForNull
  private String eventTypeName = null;
  @Nonnull
  private static Field arrayRef;

  private int previousCapacity;
  private int previousUnused;

  @Override
  public boolean notifyInstantiation(
      @Nonnull ArrayList<?> list, @Nonnegative long size, int count, @Nonnull EventType type) {
    this.eventTypeName = type.getName();
    try {
      previousCapacity = ((Object[]) (arrayRef.get(list))).length;
      previousUnused = previousCapacity - list.size();

      return true;
    } catch (IllegalAccessException e) {
      LoggerFactory.getLogger().log(Level.WARNING, "Can not get ArrayList array", e);
    }

    return false;
  }

  @Override
  @Nonnull
  public ObjectWatcher.Statistics addSample(@Nonnull ArrayList<?> list,
      @CheckForNull ObjectWatcher.Statistics raw, @Nonnull EventType type) {
    Statistics statistics = (Statistics) raw;

    if (statistics == null) {
      statistics = new Statistics();
    }

    try {
      int capacity = ((Object[]) (arrayRef.get(list))).length;
      int unused = capacity - list.size();

      if (capacity - previousCapacity != 0) {
        statistics.increaseCapacity.add(capacity - previousCapacity, eventTypeName);
      }

      if (unused - previousUnused != 0) {
        statistics.increaseUnused.add(unused - previousUnused, eventTypeName);
      }

      statistics.unused.add(unused, eventTypeName);

      previousCapacity = capacity;
      previousUnused   = unused;
    } catch (IllegalAccessException e) {
      LoggerFactory.getLogger().log(Level.WARNING, "Can not instanciate Watcher", e);
    }

    return statistics;
  }

  static {
    try {
      arrayRef = ArrayList.class.getDeclaredField("elementData");
      arrayRef.setAccessible(true);
    } catch (NoSuchFieldException e) {
      LoggerFactory.getLogger()
          .log(Level.WARNING, "Can not get 'elementData' reference on type ArrayList", e);
      throw new ExceptionInInitializerError(e);
    }
  }

  /**
   * Install a {@link ArrayListWatcher}
   */
  @ImplementationName(iface = WatcherInstaller.class, name = "arraylist-capacity",
      description = "record state of the array backed by ArrayList")
  public static class ArrayListWatcherInstaller implements WatcherInstaller {
    @Override
    public void install(@Nonnull Tracer tracer) {
      tracer.registerWatcher(ArrayList.class, ArrayListWatcher.class);
    }
  }
}
