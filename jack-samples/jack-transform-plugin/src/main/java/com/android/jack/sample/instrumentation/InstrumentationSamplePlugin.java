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

package com.android.jack.sample.instrumentation;

import com.android.jack.plugin.v01.SchedAnnotationProcessorBasedPlugin;
import com.android.sched.item.Component;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.ProductionSet;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.util.SubReleaseKind;
import com.android.sched.util.Version;
import com.android.sched.util.config.Config;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A class that describes this plugin.
 */
public class InstrumentationSamplePlugin extends SchedAnnotationProcessorBasedPlugin {
  @Nonnull
  @Override
  public String getCanonicalName() {
    return InstrumentationSamplePlugin.class.getCanonicalName();
  }

  @Nonnull
  @Override
  public String getFriendlyName() {
    return "Instrumentation plugin sample";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Instrument the code to print the method being executed";
  }

  @Nonnull
  @Override
  public Version getVersion() {
    return new Version("instrumentation-plugin", "1.0", 1, 0, SubReleaseKind.RELEASE);
  }

  @Override
  public boolean isCompatibileWithJack(@Nonnull Version version) {
    return true;
  }

  @Nonnull
  @Override
  public FeatureSet getFeatures(@Nonnull Config config, @Nonnull Scheduler scheduler) {
    FeatureSet featureSet = scheduler.createFeatureSet();
    return featureSet;
  }

  @Nonnull
  @Override
  public ProductionSet getProductions(@Nonnull Config config, @Nonnull Scheduler scheduler) {
    // We do not have any production.
    return scheduler.createProductionSet();
  }

  @Nonnull
  @Override
  public List<Class<? extends RunnableSchedulable<? extends Component>>> getSortedRunners() {
    return Collections.
            <Class<? extends RunnableSchedulable<? extends Component>>>singletonList(
                    Instrumenter.class);
  }

  @Nonnull
  @Override
  public Collection<Class<? extends RunnableSchedulable<? extends Component>>> getCheckerRunners() {
    return Collections.emptyList();
  }
}
