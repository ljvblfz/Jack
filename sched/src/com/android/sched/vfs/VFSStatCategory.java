/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.sched.vfs;

import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.PercentImpl;
import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * VFS statistic category.
 */
public enum VFSStatCategory {

  ZIP_READ("zip", "opened-for-reading-entries", "Zip entries opened for reading",
      DummyStat.COUNTER),
  ZIP_CREATED_ENTRIES("zip", "created-entries", "Created zip entries", DummyStat.COUNTER),
  DIR_READ("dir", "opened-for-reading-files", "Physical files opened for reading",
      DummyStat.COUNTER),
  DIR_WRITE("dir", "opened-for-writing-files", "Physical files opened for writing",
      DummyStat.COUNTER),
  DIR_CREATE("dir", "created-physical-files", "Created physical files", DummyStat.PERCENT),
  OPTIMIZED_COPIES("deflate", "optimized-copies",
      "Optimized copies (without inflation/deflation)", DummyStat.PERCENT);

  private static class DummyStat {
    @Nonnull
    static final Percent PERCENT =
        new StatisticId<Percent>("dummy.percent", "Dummy Percent", Percent.class, Percent.class)
            .getDummyInstance();

    @Nonnull
    static final Counter COUNTER =
        new StatisticId<Counter>("dummy.counter", "Dummy Counter", Counter.class, Counter.class)
            .getDummyInstance();
  }

  @CheckForNull
  private Tracer tracer;

  @Nonnull
  private static final String NAME_GLOBAL_PREFIX = "sched.vfs";

  @Nonnull
  private final String namePrefix;

  @Nonnull
  private final String nameSuffix;

  @Nonnull
  private final String description;

  @CheckForNull
  Map<String, StatisticId<Statistic>> statMap;

  @Nonnull
  private Statistic dummy;

  private VFSStatCategory(@Nonnull String namePrefix, @Nonnull String nameSuffix,
      @Nonnull String description, @Nonnull Statistic dummy) {
    this.namePrefix = namePrefix;
    this.nameSuffix = nameSuffix;
    this.description = description;
    this.dummy = dummy;
  }

  @SuppressWarnings("unchecked")
  public synchronized StatisticId<Counter> getCounterStatId(@Nonnull String infoString) {
    if (statMap == null) {
      statMap = new HashMap<String, StatisticId<Statistic>>();
    }
    StatisticId<Counter> id = (StatisticId<Counter>) (Object) statMap.get(infoString);
    if (id == null) {
      id = new StatisticId<Counter>(
          NAME_GLOBAL_PREFIX + '.' + namePrefix + '.' + infoString + '.' + nameSuffix,
          description + " ('" + infoString + "')", CounterImpl.class, Counter.class);
      statMap.put(infoString, ((StatisticId<Statistic>) (Object) id));
    }
    return id;
  }

  @SuppressWarnings("unchecked")
  public synchronized StatisticId<Percent> getPercentStatId(@Nonnull String infoString) {
    if (statMap == null) {
      statMap = new HashMap<String, StatisticId<Statistic>>();
    }
    StatisticId<Percent> id = (StatisticId<Percent>) (Object) statMap.get(infoString);
    if (id == null) {
      id = new StatisticId<Percent>(
          NAME_GLOBAL_PREFIX + '.' + namePrefix + '.' + infoString + '.' + nameSuffix,
          description + " ('" + infoString + "')", PercentImpl.class, Percent.class);
      statMap.put(infoString, ((StatisticId<Statistic>) (Object) id));
    }
    return id;
  }

  @Nonnull
  public Counter getCounterStat(@CheckForNull Tracer tracer, @CheckForNull String infoString) {
    assert dummy instanceof Counter;
    if (infoString == null) {
      infoString = "other";
    }
    if (tracer != null) {
      if (tracer.isTracing()) {
        StatisticId<Counter> id = getCounterStatId(infoString);
        return tracer.getStatistic(id);
      }
    }
    return (Counter) dummy;
  }

  @Nonnull
  public Percent getPercentStat(@CheckForNull Tracer tracer, @CheckForNull String infoString) {
    assert dummy instanceof Percent;
    if (infoString == null) {
      infoString = "other";
    }

    if (tracer != null) {
      if (tracer.isTracing()) {
        StatisticId<Percent> id = getPercentStatId(infoString);
        return tracer.getStatistic(id);
      }
    }
    return (Percent) dummy;
  }
}
