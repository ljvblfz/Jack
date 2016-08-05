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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Trace performance metrics for internal development purposes.
 */
@VariableName("tracer")
public interface Tracer {
  /**
   * Open a new event. You must call {@link Event#close()} on each event you open.
   * The returned {@link Event} is {@Link AutoCloseable} so this method can be called in a
   * try-with-resources pattern for your convenience.
   * Nesting opens are allowed. Parent events are handled automatically for the same thread.
   *
   * @param type the type of event.
   * @return an Event object to be closed by the caller.
   */
  @Nonnull
  public Event open(@Nonnull EventType type);

  /**
   * Open a new event. You must call {@link Event#close()} on each event you open.
   * The returned {@link Event} is {@Link AutoCloseable} so this method can be called in a
   * try-with-resources pattern for your convenience.
   * Nesting opens are allowed. Parent events are handled automatically for the same thread.
   *
   * @param name the name of event.
   * @return an Event object to be closed by the caller.
   */
  @Nonnull
  public Event open(@Nonnull String name);

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

  public void registerObject(@Nonnull Object object, @Nonnegative long size, int count,
      @CheckForNull StackTraceElement site);
}
