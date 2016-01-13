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

import com.android.sched.util.codec.VariableName;
import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.util.log.tracer.watcher.ObjectWatcher;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Trace performance metrics for internal development purposes.
 */
@VariableName("tracer")
public interface Tracer {
  /**
   * Signals that a new event has started. You must end each event for each corresponding call to
   * {@code start}. Nesting calls are allowed. Parent events are handled automatically for the same
   * thread.
   *
   * @param type the type of event.
   * @return an Event object to be ended by the caller.
   */
  @Nonnull
  public Event start(@Nonnull EventType type);

  /**
   * Signals that a new event has started. You must end each event for each corresponding call to
   * {@code start}. Nesting calls is allowed. Parent events are handled automatically for the same
   * thread.
   *
   * @param name the name of the event.
   * @return an Event object to be ended by the caller.
   */
  @Nonnull
  public Event start(@Nonnull String name);

  @Nonnull
  public ThreadTracerState getThreadState();
  public void pushThreadState(@Nonnull ThreadTracerState state);
  public void popThreadState(@Nonnull ThreadTracerState state);

  /**
   * Get if the tracer is enabled. The method can be invoked to know if a resource consuming
   * code to add mark or key/value pair is useful.
   *
   * @return true if the tracer implementation do a real trace, false otherwise.
   */
  public boolean isTracing();

  @Nonnull
  public EventType getCurrentEventType();

  @Nonnull
  public <T extends Statistic> T getStatistic(@Nonnull StatisticId<T> id);

  @Nonnull
  public EventType getDynamicEventType(@Nonnull String name);

  public <T> void registerWatcher(@Nonnull Class<T> objectClass,
      @Nonnull Class<? extends ObjectWatcher<? extends T>> watcherClass);

  public void registerObject(@Nonnull Object object, @Nonnegative long size, int count);
}
