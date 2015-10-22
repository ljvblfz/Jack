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

package com.android.sched.util.log;

import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.util.log.tracer.TracerEventType;
import com.android.sched.util.log.tracer.probe.Probe;
import com.android.sched.util.log.tracer.watcher.ObjectWatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Dummy implementation of tracer performance metrics for internal development purposes.
 */
@ImplementationName(iface = Tracer.class, name = "none")
public final class DummyTracer implements Tracer {
  @Nonnull
  private final DummyTracerEvent dummyEvent = new DummyTracerEvent();

  @Nonnull
  private final Config config;

  /**
   * Represents event.
   */
  public static class DummyTracerEvent implements Event {
    DummyTracerEvent() {
    }

    @Override
    public void end() {
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
      return Collections.emptyList();
    }

    @Override
    @Nonnull
    public <T extends Statistic> T getStatistic(@Nonnull StatisticId<T> id) {
      return id.getDummyInstance();
    }
  }

  public DummyTracer() {
    config = ThreadConfig.getConfig();
  }

  @Override
  @Nonnull
  public DummyTracerEvent start(@Nonnull EventType type) {
    assert config == ThreadConfig.getConfig();

    return dummyEvent;
  }

  @Override
  @Nonnull
  public DummyTracerEvent start(@Nonnull String name) {
    assert config == ThreadConfig.getConfig();

    return dummyEvent;
  }

  @Override
  public boolean isTracing() {
    assert config == ThreadConfig.getConfig();

    return false;
  }

  @Override
  @Nonnull
  public EventType getCurrentEventType() {
    assert config == ThreadConfig.getConfig();

    return TracerEventType.NOEVENT;
  }

  @Override
  @Nonnull
  public <T extends Statistic> T getStatistic(@Nonnull StatisticId<T> id) {
    assert config == ThreadConfig.getConfig();

    return dummyEvent.getStatistic(id);
  }

  @Override
  @Nonnull
  public EventType getDynamicEventType(@Nonnull String name) {
    assert config == ThreadConfig.getConfig();

    return TracerEventType.NOTYPE;
  }

  @Override
  public <T> void registerWatcher(@Nonnull Class<T> objectClass,
      @Nonnull Class<? extends ObjectWatcher<? extends T>> watcherClass) {
    assert config == ThreadConfig.getConfig();

    throw new UnsupportedOperationException();
  }

  @Override
  public void registerObject(@Nonnull Object object, @Nonnegative long size, int count) {
    assert config == ThreadConfig.getConfig();

    throw new UnsupportedOperationException();
  }
}
