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

package com.android.sched.scheduler;

import com.android.sched.item.Component;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.log.LoggerFactory;

import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * Implementation of a {@code Planner} which throws an exception.
 *
 * @param <T> the root <i>data</i> type
 */
@ImplementationName(iface = Planner.class, name = "manual")
public class NoPlanner<T extends Component> implements Planner<T> {
  private NoPlanner () {
  }

  @Nonnull
  @Override
  public Plan<T> buildPlan(@Nonnull Request request, @Nonnull Class<T> on) {
    LoggerFactory.getLogger().log(Level.FINE, "No automatic planner");
    throw new UnsupportedOperationException();
  }
}
