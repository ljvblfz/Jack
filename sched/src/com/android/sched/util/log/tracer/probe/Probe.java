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

package com.android.sched.util.log.tracer.probe;

import com.android.sched.util.HasDescription;
import com.android.sched.util.codec.VariableName;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Abstract class for a Probe.
 */
@VariableName("probe")
public abstract class Probe implements Comparable<Probe>, HasDescription {
  public static final int MAX_PRIORITY = 0;
  public static final int MIN_PRIORITY = 12;

  @Nonnegative
  private final int priority;

  @Nonnull
  private final String description;

  @Nonnegative
  public abstract long read();

  @Nonnegative
  public abstract void start();

  @Nonnegative
  public abstract void stop();

  @Nonnull
  public abstract String formatValue(long value);

  @Nonnegative
  public int getPriority() {
    return priority;
  }

  protected Probe(@Nonnull String description, @Nonnegative int priority) {
    this.description = description;
    this.priority = priority;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return this.description;
  }

  @Override
  public int compareTo(@CheckForNull Probe o) {
    assert(o != null);

    return priority - o.priority;
  }
}
