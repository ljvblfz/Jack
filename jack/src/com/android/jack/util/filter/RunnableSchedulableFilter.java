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

import com.android.jack.ir.ast.JMethod;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;
import com.android.sched.schedulable.RunnableSchedulable;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Marker used by filter allowing to filter {@link RunnableSchedulable}.
 */
@Description("Marker allowing to filter runnable schedulable.")
@ValidOn(JMethod.class)
public class RunnableSchedulableFilter implements Marker {
  @Nonnull
  private final List<Class<? extends RunnableSchedulable<?>>> runnableSchedulables;

  public RunnableSchedulableFilter(
      @Nonnull List<Class<? extends RunnableSchedulable<?>>> runnableSchedulables) {
    this.runnableSchedulables = runnableSchedulables;
  }

  public boolean accept(Class<? extends RunnableSchedulable<?>> runnableSchedulable) {
    return (runnableSchedulables.contains(runnableSchedulable));
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }
}
