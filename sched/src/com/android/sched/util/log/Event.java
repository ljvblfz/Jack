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

import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.util.log.tracer.probe.Probe;

import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a event whose performance is tracked.
 */
public interface Event {
  /**
   * Signals the end of the current event.
   */
  public void end();

  /**
   * @return the type of an event.
   */
  @Nonnull
  public EventType getType();

  /**
   * @return the list of children
   */
  @Nonnull
  public Collection<Event> getChildren();

  /**
   * Returns the event elapsed value.
   */
  @Nonnegative
  public long getElapsedValue(@Nonnull Probe probe);

  /**
   * Returns the event start value.
   */
  @Nonnegative
  public long getStartValue(@Nonnull Probe probe);

  public void adjustElapsedValue(@Nonnull Probe probe, long elapsedValue);

  @Nonnull
  public Collection<Statistic> getStatistics();

  @Nonnull
  public <T extends Statistic> T getStatistic(@Nonnull StatisticId<T> id);
}
