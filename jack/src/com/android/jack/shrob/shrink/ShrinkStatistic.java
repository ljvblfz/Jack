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

package com.android.jack.shrob.shrink;

import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.PercentImpl;
import com.android.sched.util.log.stats.StatisticId;

/**
 * Statistics for the shrink feature
 */
public class ShrinkStatistic {
  public static final StatisticId<Percent> TYPES_REMOVED = new StatisticId<Percent>(
      "jack.shrinker.type", "Types removed by the shrinker", PercentImpl.class,
      Percent.class);
  public static final StatisticId<Percent> METHODS_REMOVED = new StatisticId<Percent>(
      "jack.shrinker.method", "Methods removed by the shrinker", PercentImpl.class,
      Percent.class);
  public static final StatisticId<Percent> FIELDS_REMOVED = new StatisticId<Percent>(
      "jack.shrinker.field", "Fields removed by the shrinker", PercentImpl.class,
      Percent.class);
}
