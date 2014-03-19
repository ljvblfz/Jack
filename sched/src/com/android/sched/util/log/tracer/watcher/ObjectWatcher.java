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

import com.android.sched.util.log.EventType;
import com.android.sched.util.log.stats.Statistic;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Interface for an object watcher
 *
 * @param <T> type of the object watched
 */
public interface ObjectWatcher<T> {
  /**
   * Interface to compute statistics
   */
  public interface Statistics extends Iterable<Statistic> {
  }

  /**
   * Callback to start watching an object.
   *
   * @param object watched object.
   * @param type current event type when the watcher is started.
   * @param size the size of the object being allocated.
   * @param count the <code>int</code> count of how many instances are being
   *     allocated.  -1 means a simple new to distinguish from a 1-element array.  0
   *     shows up as a value here sometimes; one reason is T[] toArray()-type
   *     methods that require an array type argument (see ArrayList.toArray() for
   *     example).
   * @return true if the watcher want to sample the object at every event ending (see
   *         {@link ObjectWatcher#addSample}), or false otherwise.
   */
  public abstract boolean notifyInstantiation(
      @Nonnull T object, @Nonnegative long size, int count, @Nonnull EventType type);

  /**
   * Callback to update statistics when ending an event.
   *
   * @param object watched object.
   * @param statistics if non {@code null}, statistics must be collected into this
   *        {@link Statistics} object, if {@code null} a new {@link Statistics} have to be allocated
   *        and statistics must be collected into this newly allocated {@link Statistics} object.
   * @param type the event type of the ending event.
   * @return the {@link Statistics} object used to collect statistics.
   */
  @Nonnull
  public abstract Statistics addSample(
      @Nonnull T object, @CheckForNull Statistics statistics, @Nonnull EventType type);
}
