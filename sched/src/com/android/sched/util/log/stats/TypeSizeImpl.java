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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a statistic on object allocation.
 */
public class TypeSizeImpl extends TypeSize {
  @Nonnegative
  private long size = 0;

  protected TypeSizeImpl(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  /**
   * Record an object allocation.
   *
   * @param size size in bytes of object in memory.
   */
  @Override
  public synchronized void recordType(@Nonnegative long size) {
    if (this.size == 0) {
      this.size = size;
    } else {
      assert this.size == size : "Object size are not constant";
    }
  }

  @Override
  public synchronized void merge(@Nonnull Statistic statistic) {
    TypeSizeImpl stat = (TypeSizeImpl) statistic;

    synchronized (stat) {
      if (this.size == 0) {
        this.size = stat.size;
      } else {
        assert this.size == stat.size : "Object size are not constant";
      }
    }
  }

  @Nonnegative
  @Override
  public long getSize() {
    return size;
  }
}
