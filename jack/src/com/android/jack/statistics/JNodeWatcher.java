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

package com.android.jack.statistics;

import com.android.jack.ir.ast.JNode;
import com.android.sched.item.Items;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.log.EventType;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.util.log.tracer.watcher.ObjectWatcher;
import com.android.sched.util.log.tracer.watcher.WatcherInstaller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Class to watch {@link JNode} creation.
 */
public class JNodeWatcher implements ObjectWatcher<JNode> {
  static class Statistics implements ObjectWatcher.Statistics {
    @Override
    public Iterator<Statistic> iterator() {
      throw new AssertionError();
    }
  }

  @Nonnull
  private static final Map<Class<? extends JNode>, StatisticId<Counter>> map =
      new HashMap<Class<? extends JNode>, StatisticId<Counter>>();

  @Override
  public boolean notifyInstantiation(
      @Nonnull JNode node,
      @Nonnegative long size,
      int count,
      @Nonnull EventType type,
      @CheckForNull StackTraceElement site) {
    StatisticId<Counter> id = map.get(node.getClass());
    if (id == null) {
      String name = Items.getName(node.getClass());

      id = new StatisticId<Counter>(
          "jack.allocation.jnode." + name, "JNode allocation of type " + node.getClass().getName(),
          CounterImpl.class, Counter.class);
      map.put(node.getClass(), id);
    }

    try {
      TracerFactory.getTracer().getStatistic(id).incValue();
    } catch (RuntimeException e) {
      // Best effort in tracking object allocation
    }

    return false;
  }

  @Override
  @Nonnull
  public ObjectWatcher.Statistics addSample(@Nonnull JNode node,
      @CheckForNull ObjectWatcher.Statistics raw, @Nonnull EventType type) {
    throw new AssertionError();
  }

  /**
   * Install a {@link JNodeWatcher}
   */
  @ImplementationName(iface = WatcherInstaller.class, name = "jnode-alloc",
      description = "record all JNode allocations")
  public static class JNodeWatcherInstaller implements WatcherInstaller {
    @Override
    public void install(@Nonnull Tracer tracer) {
      tracer.registerWatcher(JNode.class, JNodeWatcher.class);
    }
  }
}
