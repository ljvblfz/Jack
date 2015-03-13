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

package com.android.jack.util.filter;

import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.codec.VariableName;

import javax.annotation.Nonnull;

/**
 * Filter interface that must be implemented to define filters.
 *
 * @param <T> The type of object to filter.
 */
@VariableName("filter")
public interface Filter<T> {

  /**
   * Accepts or rejects an instance of type {@code T}.
   *
   * @param runnableSchedulable runnable schedulable where the method must be filtered.
   * @param t instance of type {@code T} that must be filtered.
   * @return true if {@code t} must be accepted, false otherwise.
   */
  public boolean accept(@Nonnull Class<? extends RunnableSchedulable<?>> runnableSchedulable,
      @Nonnull T t);
}
