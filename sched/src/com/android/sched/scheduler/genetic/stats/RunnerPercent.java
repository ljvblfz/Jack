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

package com.android.sched.scheduler.genetic.stats;

import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;

import javax.annotation.Nonnull;

/**
 * Percent for runner constraints satisfied
 */
public class RunnerPercent extends Percent {
  protected RunnerPercent(StatisticId<? extends Statistic> id) {
    super(id);
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "Percent of runner constraints satisfied";
  }
}
