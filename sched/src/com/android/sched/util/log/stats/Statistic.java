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

package com.android.sched.util.log.stats;

import com.android.sched.util.table.DataHeader;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a statistic.
 */
public abstract class Statistic implements DataHeader {
  @Nonnull
  private final StatisticId<? extends Statistic> id;

  public abstract void merge (@Nonnull Statistic statistic);

  protected Statistic(@Nonnull StatisticId<? extends Statistic> id) {
    this.id = id;
  }

  @Nonnull
  public StatisticId<? extends Statistic> getId() {
    return id;
  }

  @Nonnull
  @Deprecated
  public abstract Object getValue(@Nonnegative int columnIdx);

  @Nonnull
  @Deprecated
  public abstract String getHumanReadableValue(@Nonnegative int columnIdx);

  @Nonnull
  @Deprecated
  public abstract String getDescription(@Nonnegative int columnIdx);

  @Nonnull
  @Deprecated
  public abstract String getType(@Nonnegative int columnIdx);

  @Nonnull
  public abstract String getDescription();

  @Override
  @Nonnull
  public String toString() {
    return id.getName();
  }
}
