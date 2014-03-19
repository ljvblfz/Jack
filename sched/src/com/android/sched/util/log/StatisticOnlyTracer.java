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

package com.android.sched.util.log;

import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.codec.ToStringFormatter;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.util.log.tracer.AbstractTracer;
import com.android.sched.util.log.tracer.TracerEventType;
import com.android.sched.util.log.tracer.probe.HeapAllocationProbe;
import com.android.sched.util.log.tracer.probe.Probe;
import com.android.sched.util.log.tracer.watcher.ObjectWatcher;
import com.android.sched.util.log.tracer.watcher.WatcherInstaller;
import com.android.sched.util.table.ConcatTable;
import com.android.sched.util.table.DataHeaderBuilder;
import com.android.sched.util.table.DataRow;
import com.android.sched.util.table.MultiDataRow;
import com.android.sched.util.table.Report;
import com.android.sched.util.table.ReportPrinterFactory;
import com.android.sched.util.table.SimpleTable;
import com.android.sched.util.table.Table;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Tracer implementation which only collects statistics on a pseudo-single event.
 */
@ImplementationName(iface = Tracer.class, name = "stat-only")
public final class StatisticOnlyTracer implements Tracer {
  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final SingletonEvent event = new SingletonEvent();

  public StatisticOnlyTracer() {
    List<WatcherInstaller> watchers = ThreadConfig.get(AbstractTracer.WATCHER_INSTALL);
    if (watchers.size() > 0) {
      for (WatcherInstaller watcher : watchers) {
        watcher.install(this);
      }

      HeapAllocationProbe.ensureInstall();
    }
  }

  /**
   * Represents event.
   */
  public static class SingletonEvent implements Event {
    @Nonnull
    private final AtomicInteger eventCount = new AtomicInteger(0);

    @Nonnull
    private final Map<StatisticId<? extends Statistic>, Statistic> statisticsById =
        new ConcurrentHashMap<StatisticId<? extends Statistic>, Statistic>();

    @Override
    public void end() {
      if (eventCount.decrementAndGet() == 0) {
        try {
          enable.set(Boolean.FALSE);

          Map<Class<? extends Statistic>, ConcatTable> tables =
              new HashMap<Class<? extends Statistic>, ConcatTable>();
          Report report = new Report(new Date().toString(), "");

          for (final Statistic statistic : statisticsById.values()) {
            ConcatTable table = tables.get(statistic.getClass());

            if (table == null) {
              Table left = new SimpleTable("N/A", "N/A", DataHeaderBuilder.get()
                  .addColumn("Name", new ToStringFormatter())
                  .addColumn("Description", new ToStringFormatter()).build());
              Table right = new SimpleTable("N/A", "N/A", statistic);

              table = new ConcatTable(statistic.getDescription(), "", left, right);

              tables.put(statistic.getClass(), table);
              report.addTable(table);
            }

            if (statistic instanceof DataRow) {
              table.addLeftRow(new MultiDataRow(statistic.getId().getName(),
                  statistic.getId().getDescription()));
              table.addRightRow(((DataRow) statistic));
            }
          }

          ReportPrinterFactory.getReportPrinter().printReport(report);
        } finally {
          enable.set(Boolean.TRUE);
        }
      }
    }

    @Override
    @Nonnegative
    public long getElapsedValue(@Nonnull Probe probe) {
      throw new UnsupportedOperationException();
    }

    @Override
    @Nonnegative
    public long getStartValue(@Nonnull Probe probe) {
      throw new UnsupportedOperationException();
    }

    @Override
    @Nonnull
    public EventType getType() {
      return TracerEventType.NOTYPE;
    }

    @Override
    @Nonnull
    public String toString() {
      return "Dummy";
    }

    @Override
    @Nonnull
    public List<Event> getChildren() {
      return Collections.emptyList();
    }

    @Override
    public void adjustElapsedValue(@Nonnull Probe probe, long elapsedValue) {
    }

    @Override
    @Nonnull
    public Collection<Statistic> getStatistics() {
      try {
        enable.set(Boolean.FALSE);
        return statisticsById.values();
      } finally {
        enable.set(Boolean.TRUE);
      }
    }

    @Override
    @Nonnull
    public synchronized <T extends Statistic> T getStatistic(@Nonnull StatisticId<T> id) {
      try {
        enable.set(Boolean.FALSE);
        @SuppressWarnings("unchecked")
        T statistic = (T) statisticsById.get(id);

        if (statistic == null) {
          statistic = id.newInstance();
          statisticsById.put(id, statistic);
        }

        return statistic;
      } finally {
        enable.set(Boolean.TRUE);
      }
    }
  }

  @Override
  @Nonnull
  public SingletonEvent start(@Nonnull EventType type) {
    event.eventCount.incrementAndGet();
    return event;
  }

  @Override
  @Nonnull
  public SingletonEvent start(@Nonnull String name) {
    event.eventCount.incrementAndGet();
    return event;
  }

  @Nonnull
  public static final ThreadLocal<Boolean> enable = new ThreadLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return Boolean.TRUE;
    }
  };

  @Override
  public boolean isTracing() {
    return enable.get().booleanValue();
  }

  @Override
  @Nonnull
  public EventType getCurrentEventType() {
    return TracerEventType.NOEVENT;
  }

  @Override
  @Nonnull
  public <T extends Statistic> T getStatistic(@Nonnull StatisticId<T> id) {
    return event.getStatistic(id);
  }

  @Override
  @Nonnull
  public EventType getDynamicEventType(@Nonnull String name) {
    return TracerEventType.NOTYPE;
  }

  //
  // Watchers
  //

  @Nonnull
  private final
      Map<Class<? extends ObjectWatcher<?>>, WeakHashMap<Object, ObjectWatcher<Object>>>
      objects = new HashMap<
          Class<? extends ObjectWatcher<?>>, WeakHashMap<Object, ObjectWatcher<Object>>>();

  @Nonnull
  private final Map<Class<?>, Class<? extends ObjectWatcher<?>>> watchers =
      new HashMap<Class<?>, Class<? extends ObjectWatcher<?>>>();

  @Nonnull
  private final Set<Class<?>> notWatched = new HashSet<Class<?>>();
  @Nonnull
  private final Object watcherLock = new Object();

  @Override
  public synchronized <T> void registerWatcher(@Nonnull Class<T> objectClass,
      @Nonnull Class<? extends ObjectWatcher<? extends T>> watcherClass) {
    WeakHashMap<Object, ObjectWatcher<Object>> map =
        new WeakHashMap<Object, ObjectWatcher<Object>>();

    synchronized (watcherLock) {
      objects.put(watcherClass, map);
      watchers.put(objectClass, watcherClass);

      for (Class<?> cls : notWatched) {
        if (objectClass.isAssignableFrom(cls)) {
          logger.log(Level.INFO, "Watcher ''{0}'' missed some instances of type ''{1}''",
              new Object[] {watcherClass.getName(), cls.getName()});

          watchers.put(cls, watcherClass);
          notWatched.remove(objectClass);
        }
      }
    }
  }

  @Override
  public void registerObject(@Nonnull Object object, @Nonnegative long size, int count) {
    Class<? extends ObjectWatcher<?>> watcherClass = null;

    synchronized (watcherLock) {
      if (notWatched.contains(object.getClass())) {
        return;
      }

      watcherClass = watchers.get(object.getClass());
      if (watcherClass == null) {
        for (Entry<Class<?>, Class<? extends ObjectWatcher<?>>> entry : watchers.entrySet()) {
          if (entry.getKey().isAssignableFrom(object.getClass())) {
            watcherClass = entry.getValue();
            break;
          }
        }

        if (watcherClass != null) {
          watchers.put(object.getClass(), watcherClass);
        } else {
          notWatched.add(object.getClass());
          return;
        }
      }
    }

    try {
      @SuppressWarnings("unchecked")
      ObjectWatcher<Object> watcher = (ObjectWatcher<Object>) watcherClass.newInstance();
      WeakHashMap<Object, ObjectWatcher<Object>> weak = objects.get(watcherClass);
      assert weak != null; // If watchers contains object.getClass, then objects contains it also,
                           // see registerWatcher
      if (watcher.notifyInstantiation(object, size, count, getCurrentEventType())) {
        weak.put(object, watcher);
      }
    } catch (InstantiationException e) {
      logger.log(Level.WARNING, "Can not instantiate Watcher", e);
    } catch (IllegalAccessException e) {
      logger.log(Level.WARNING, "Can not instantiate Watcher", e);
    }
  }
}
