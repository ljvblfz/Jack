/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.android.sched.util.codec.ImplementationSelector;
import com.android.sched.util.codec.ListCodec;
import com.android.sched.util.collect.Lists;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.EventType;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.util.log.tracer.probe.Probe;
import com.android.sched.util.log.tracer.watcher.ObjectWatcher;
import com.android.sched.util.log.tracer.watcher.WatcherInstaller;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Abstract class for logging performance metrics for internal development purposes.
 */
@HasKeyId
public abstract class AbstractTracer implements Tracer {
  @Nonnull
  public static final PropertyId<List<WatcherInstaller>> WATCHER_INSTALL = PropertyId.create(
      "sched.tracer.watchers",
      "Define which watchers use for tracing",
      new ListCodec<WatcherInstaller>(new ImplementationSelector<WatcherInstaller>(
          WatcherInstaller.class)).setMin(0)).addDefaultValue("");

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  /**
   * Construct a Tracer
   */
  public AbstractTracer() {
    eventsToWrite = openQueue();

    shutDownSentinel = new TracerEvent();
    shutDownLatch    = new CountDownLatch(1);

    pendingEvents = initPendingEvents();

    List<WatcherInstaller> watchers = ThreadConfig.get(WATCHER_INSTALL);
    if (watchers.size() > 0) {
      for (WatcherInstaller watcher : watchers) {
        watcher.install(this);
      }
    }
  }

  //
  // Watchers
  //

  private final
      Map<Class<? extends ObjectWatcher<?>>, WeakHashMap<Object, ObjectWatcher<Object>>>
      objects = new HashMap<
          Class<? extends ObjectWatcher<?>>, WeakHashMap<Object, ObjectWatcher<Object>>>();
  private final Map<Class<?>, Class<? extends ObjectWatcher<?>>> watchers =
      new HashMap<Class<?>, Class<? extends ObjectWatcher<?>>>();
  private final Set<Class<?>> notWatched = new HashSet<Class<?>>();
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

  @Nonnull
  protected final ProbeManager probeManager = ProbeManager.getProbeManager();

  abstract void stopTracer();
  abstract void processEvent(@Nonnull Event event);
  abstract void flush();

  //
  // Statistics
  //

  /**
   * Define category of statistics
   */
  protected enum Children {
    WITH, WITHOUT;
  }

  @Nonnull
  protected final
      Map<EventType, Map<StatisticId<? extends Statistic>, Statistic>[]> globalStatistics =
          new HashMap<EventType, Map<StatisticId<? extends Statistic>, Statistic>[]>();

  @Override
  @Nonnull
  public <T extends Statistic> T getStatistic(@Nonnull StatisticId<T> id) {
    Stack<TracerEvent> threadPendingEvents = pendingEvents.get();

    if (threadPendingEvents.isEmpty()) {
      throw new IllegalStateException("Tried to get statistic to an event that never started!");
    }

    return threadPendingEvents.peek().getStatistic(id);
  }

  @Nonnull
  private final Set<StatisticId<? extends Statistic>> setOfStatisticIds =
      new HashSet<StatisticId<? extends Statistic>>();

  @SuppressWarnings("unchecked")
  private void mergeStatistic(@Nonnull EventType type, @Nonnull StatisticId<? extends Statistic> id,
      @Nonnull Children kind, @Nonnull Statistic local) {
    Map<StatisticId<? extends Statistic>, Statistic>[] staticticById;
    synchronized (globalStatistics) {
      staticticById = globalStatistics.get(type);
      if (staticticById == null) {
        staticticById = new Map[Children.values().length];
        globalStatistics.put(type, staticticById);

        for (int i = 0; i < staticticById.length; i++) {
          staticticById[i] = new HashMap<StatisticId<? extends Statistic>, Statistic>();
        }
      }
    }

    Statistic global;
    synchronized (staticticById[kind.ordinal()]) {
      global = staticticById[kind.ordinal()].get(id);
      if (global == null) {
        global = id.newInstance();
        staticticById[kind.ordinal()].put(id, global);
        synchronized (setOfStatisticIds) {
          setOfStatisticIds.add(id);
        }
      }
    }

    synchronized (global) {
      global.merge(local);
    }
  }

  @Nonnull
  protected Collection<StatisticId<? extends Statistic>> getStatisticsIds() {
    return setOfStatisticIds;
  }

  //
  // Dynamic Event Type
  //

  // Map associates name with a DynamicEventType
  @Nonnull
  private final Map<String, DynamicEventType> dynalicEventByName =
      new HashMap<String, DynamicEventType>();

  @Override
  @Nonnull
  public TracerEvent start(@Nonnull String name) {
    DynamicEventType event = dynalicEventByName.get(name);

    if (event == null) {
      event = new DynamicEventType(name);
      dynalicEventByName.put(name, event);
    }

    return start(event);
  }

  @Override
  @Nonnull
  public EventType getDynamicEventType(@Nonnull String name) {
    EventType type = dynalicEventByName.get(name);

    if (type != null) {
      return type;
    } else {
      return TracerEventType.NOTYPE;
    }
  }

  //
  // Other public API
  //

  @Override
  @Nonnull
  public TracerEvent start(@Nonnull EventType type) {
    Stack<TracerEvent> threadPendingEvents = pendingEvents.get();
    TracerEvent parent = null;

    if (!threadPendingEvents.isEmpty()) {
      parent = threadPendingEvents.peek();
    }

    TracerEvent newEvent = new TracerEvent(parent, type);
    threadPendingEvents.push(newEvent);

    return newEvent;
  }

  @Override
  public boolean isTracing() {
    return probeManager.isStarted();
  }

  @Nonnull
  ProbeManager getProbeManager() {
    return probeManager;
  }

  @Override
  @Nonnull
  public EventType getCurrentEventType() {
    Stack<TracerEvent> threadPendingEvents = pendingEvents.get();

    if (threadPendingEvents.isEmpty()) {
      return TracerEventType.NOEVENT;
    } else {
      return threadPendingEvents.peek().getType();
    }
  }

  //
  // Private
  //

  @Nonnull
  private final BlockingQueue<TracerEvent> eventsToWrite;

  @Nonnull
  private final ThreadLocal<Stack<TracerEvent>> pendingEvents;

  @Nonnull
  private final CountDownLatch shutDownLatch;

  @Nonnull
  private final TracerEvent shutDownSentinel;

  private class LogWriterThread extends Thread {
    private static final int FLUSH_TIMER_MSECS = 1000;
    @Nonnull
    private final BlockingQueue<TracerEvent> threadEventQueue;

    public LogWriterThread(@Nonnull final BlockingQueue<TracerEvent> eventQueue) {
      super();
      this.threadEventQueue = eventQueue;
    }

    @Override
    public void run() {
      long nextFlush = System.currentTimeMillis() + FLUSH_TIMER_MSECS;
      try {
        while (true) {
          TracerEvent event =
              threadEventQueue.poll(nextFlush - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
          if (event == null) {
            // ignore.
          } else if (event == shutDownSentinel) {
            try {
              stopTracer();
            } catch (Throwable e) {
              logger.log(Level.SEVERE, "Problem during tracer shutdown", e);
            }
            break;
          } else {
            processEvent(event);
          }

          if (System.currentTimeMillis() >= nextFlush) {
            flush();
            nextFlush = System.currentTimeMillis() + FLUSH_TIMER_MSECS;
          }
        }
      } catch (InterruptedException ignored) {
        // Ignored
      } finally {
        shutDownLatch.countDown();
      }
    }
  }

  @Nonnull
  private ThreadLocal<Stack<TracerEvent>> initPendingEvents() {
    return new ThreadLocal<Stack<TracerEvent>>() {
      @Override
      protected Stack<TracerEvent> initialValue() {
        return new Stack<TracerEvent>();
      }
    };
  }

  @Nonnull
  private BlockingQueue<TracerEvent> openQueue() {
    final BlockingQueue<TracerEvent> eventQueue = new LinkedBlockingQueue<TracerEvent>();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          // Wait for the other thread to drain the queue.
          eventQueue.add(shutDownSentinel);
          shutDownLatch.await();
        } catch (InterruptedException e) {
          // Ignored
        }
      }
    });

    // Background thread to write SpeedTracer events to log
    Thread logWriterWorker = new LogWriterThread(eventQueue);

    // Lower than normal priority.
    logWriterWorker.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);

    /*
     * This thread must be daemon, otherwise shutdown hooks would never begin to run, and an app
     * wouldn't finish.
     */
    logWriterWorker.setDaemon(true);
    logWriterWorker.setName("StatsLogger writer");
    logWriterWorker.start();

    return eventQueue;
  }

  //
  // Event
  //

  private class TracerEvent implements Event {
    @Nonnull
    protected final EventType type;
    @Nonnull
    List<Event> children;

    @Nonnull
    long elapsedValue[];
    @Nonnull
    long startValue[];

    @CheckForNull
    Map<StatisticId<? extends Statistic>, Statistic> statisticsById;


    TracerEvent() {
      this.children = Lists.create();
      this.type = TracerEventType.NOTYPE;
      this.elapsedValue = new long[probeManager.getProbes().size()];
      this.startValue = probeManager.read(type);
    }

    TracerEvent(@CheckForNull TracerEvent parent, @Nonnull EventType type) {
      if (parent != null) {
        probeManager.stop();
        parent.children = Lists.add(parent.children, this);
      }

      this.children = Lists.create();
      this.type = type;
      this.elapsedValue = new long[probeManager.getProbes().size()];
      this.startValue = probeManager.readAndStart(type);
    }

    TracerEvent(@CheckForNull TracerEvent parent, @Nonnull EventType type, @Nonnull long[] values) {
      if (parent != null) {
        parent.children = Lists.add(parent.children, this);
      }

      this.children = Lists.create();
      this.type = type;
      this.elapsedValue = new long[probeManager.getProbes().size()];
      this.startValue = values;
    }

    @Override
    public void end() {
      long values[] = probeManager.stopAndRead(type);

      Stack<TracerEvent> threadPendingEvents = pendingEvents.get();
      if (threadPendingEvents.isEmpty() || threadPendingEvents.peek() != this) {
        throw new IllegalStateException("Event '" + this.getType().getName() +
            "' is not the current one");
      }

      TracerEvent currentEvent = threadPendingEvents.pop();

      for (int i = 0; i < values.length; i++) {
        currentEvent.elapsedValue[i] = values[i] - currentEvent.startValue[i];
      }

      TracerEvent[] stack =
          threadPendingEvents.toArray(new TracerEvent[threadPendingEvents.size()]);

      // Managed watched objects
      for (WeakHashMap<Object, ObjectWatcher<Object>> weak : objects.values()) {
        // Cumul statistics
        ObjectWatcher.Statistics statistics = null;
        for (Entry<Object, ObjectWatcher<Object>> e : weak.entrySet()) {
          statistics = e.getValue().addSample(e.getKey(), statistics, currentEvent.getType());
        }

        // Merge in global statistics
        if (statistics != null) {
          for (Statistic statistic : statistics) {
            mergeStatistic(currentEvent.getType(), statistic.getId(), Children.WITHOUT, statistic);
            mergeStatistic(currentEvent.getType(), statistic.getId(), Children.WITH, statistic);

            for (TracerEvent event : stack) {
              if (event.getType() != currentEvent.getType()) {
                mergeStatistic(event.getType(), statistic.getId(), Children.WITH, statistic);
              }
            }
          }
        }
      }

      // Managed statistics
      for (Statistic stat : currentEvent.getStatistics()) {
        mergeStatistic(currentEvent.getType(), stat.getId(), Children.WITHOUT, stat);
        mergeStatistic(currentEvent.getType(), stat.getId(), Children.WITH, stat);

        for (TracerEvent event : stack) {
          if (event.getType() != currentEvent.getType()) {
            mergeStatistic(event.getType(), stat.getId(), Children.WITH, stat);
          }
        }
      }
      currentEvent.removeStatistics();

      // Ending event
      if (threadPendingEvents.isEmpty()) {
        eventsToWrite.add(currentEvent);
      } else {
        TracerEvent parent = threadPendingEvents.peek();

        TracerEvent overhead = new TracerEvent(parent, TracerEventType.OVERHEAD, values);
        long[] now = probeManager.readAndStart(type);
        for (int idx = 0; idx < now.length; idx++) {
          overhead.elapsedValue[idx] = now[idx] - values[idx];
        }
      }
    }

    @Override
    @Nonnull
    public Collection<Statistic> getStatistics() {
      if (statisticsById != null) {
        return statisticsById.values();
      } else {
        return Collections.emptyList();
      }
    }

    @Override
    @Nonnull
    public <T extends Statistic> T getStatistic(@Nonnull StatisticId<T> id) {
      probeManager.stop();
      try {
        if (statisticsById == null) {
          statisticsById = new HashMap<StatisticId<? extends Statistic>, Statistic>();
        }

        @SuppressWarnings("unchecked")
        T statistic = (T) statisticsById.get(id);

        if (statistic == null) {
          statistic = id.newInstance();
          statisticsById.put(id, statistic);
        }

        return statistic;
      } finally {
        probeManager.start();
      }
    }

    @Override
    @Nonnegative
    public long getElapsedValue(@Nonnull Probe probe) {
      return elapsedValue[probeManager.getIndex(probe)];
    }

    @Override
    @Nonnegative
    public long getStartValue(@Nonnull Probe probe) {
      return startValue[probeManager.getIndex(probe)];
    }

    @Override
    public void adjustElapsedValue(@Nonnull Probe probe, long value) {
      elapsedValue[probeManager.getIndex(probe)] += value;
    }

    @Override
    @Nonnull
    public EventType getType() {
      return type;
    }

    @Override
    @Nonnull
    public String toString() {
      return type.getName();
    }

    @Override
    @Nonnull
    public List<Event> getChildren() {
      return children;
    }

    private void removeStatistics() {
      statisticsById = null;
    }
  }
}
