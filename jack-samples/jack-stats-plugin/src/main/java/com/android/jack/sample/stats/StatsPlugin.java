/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.sample.stats;

import com.android.jack.plugin.v01.SchedAnnotationProcessorBasedPlugin;
import com.android.sched.item.Component;
import com.android.sched.item.Feature;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.ProductionSet;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.util.SubReleaseKind;
import com.android.sched.util.Version;
import com.android.sched.util.config.Config;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The class describing this plugin.
 */
public class StatsPlugin extends SchedAnnotationProcessorBasedPlugin {
  @Nonnull
  @Override
  public String getCanonicalName() {
    return StatsPlugin.class.getName();
  }

  @Nonnull
  @Override
  public String getFriendlyName() {
    return "Stats plugin";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Compute statistics about compiled source files";
  }

  @Nonnull
  @Override
  public Version getVersion() {
    return new Version("stats-plugin", "1.0", 1, 0, SubReleaseKind.RELEASE);
  }

  @Override
  public boolean isCompatibileWithJack(@Nonnull Version version) {
    return true;
  }

  @Nonnull
  @Override
  public FeatureSet getFeatures(@Nonnull Config config, @Nonnull Scheduler scheduler) {
    FeatureSet featureSet = scheduler.createFeatureSet();
    if (config.get(ClassStats.ENABLE_CLASS_STATS).booleanValue()) {
      featureSet.add(ClassStats.class);
    }
    if (config.get(FieldStats.ENABLE_FIELD_STATS).booleanValue()) {
      featureSet.add(FieldStats.class);
    }
    if (config.get(MethodStats.ENABLE_METHOD_STATS).booleanValue()) {
      featureSet.add(MethodStats.class);
    }
    return featureSet;
  }

  @Nonnull
  @Override
  public ProductionSet getProductions(@Nonnull Config config, @Nonnull Scheduler scheduler) {
    return scheduler.createProductionSet();
  }

  @Nonnull
  @Override
  public List<Class<? extends RunnableSchedulable<? extends Component>>> getSortedRunners() {
    return Arrays.<Class<? extends RunnableSchedulable<? extends Component>>>asList(
            ClassStatsComputer.class,
            FieldStatsComputer.class,
            MethodStatsComputer.class
    );
  }

  @Nonnull
  @Override
  public Collection<Class<? extends RunnableSchedulable<? extends Component>>> getCheckerRunners() {
    return Collections.emptyList();
  }
}
