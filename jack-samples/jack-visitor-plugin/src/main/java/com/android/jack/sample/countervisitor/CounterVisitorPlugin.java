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

package com.android.jack.sample.countervisitor;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class declares our plugin to Jack.
 * <p>
 * We choose to name the plugin with the name of this class. This name must be the one that appears
 * in the {@code META-INF/services/com.android.jack.plugin.v01.Plugin} file of the plugin JAR file.
 * <p>
 * This plugin provides the feature {@link PostfixCountingFeature} that is implemented by the
 * {@link PostfixCountPrinter} schedulable.
 * <p>
 * This plugin does not generate any {@link com.android.sched.item.Production}.
 */
public class CounterVisitorPlugin extends SchedAnnotationProcessorBasedPlugin {

  @Override
  @Nonnull
  public String getCanonicalName() {
    // We use the class name as plugin name.
    return CounterVisitorPlugin.class.getCanonicalName();
  }

  @Override
  @Nonnull
  public String getFriendlyName() {
    return "Counter visitor plugin";
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "A plugin that counts the number of postfix operator (i++ and i--) in the IR";
  }

  @Override
  @Nonnull
  public FeatureSet getFeatures(@Nonnull Config config, @Nonnull Scheduler scheduler) {
    FeatureSet featureSet = scheduler.createFeatureSet();
    // PostfixCounter will be scheduled by Jack only if we declare that we provide its
    // PostfixCountingFeature.
    featureSet.add(PostfixCountingFeature.class);
    return featureSet;
  }

  @Override
  @Nonnull
  public ProductionSet getProductions(@Nonnull Config config, @Nonnull Scheduler scheduler) {
    // We do not produce anything so we return an empty set.
    return scheduler.createProductionSet();
  }

  @Override
  @Nonnull
  public List<Class<? extends RunnableSchedulable<? extends Component>>> getSortedRunners() {
    // We add our schedulables in the dependency order.
    return Arrays.<Class<? extends RunnableSchedulable<? extends Component>>>asList(
            PostfixCounter.class,
            PostfixCountPrinter.class  // needs PostfixCounter so inserted after it
    );
  }

  @Override
  @Nonnull
  public Collection<Class<? extends RunnableSchedulable<? extends Component>>> getCheckerRunners() {
    // We do not have any checker to run so we return an empty list.
    return Collections.emptyList();
  }

  @Override
  @Nonnull
  public Version getVersion() {
    // We declare a static version "1.0" of our plugin. It is recommended to manage versions of the
    // plugin using a version file. It allows to create a corresponding {@link Version} object.
    return new Version("plugin", "1.0", 1, 0, SubReleaseKind.RELEASE);
  }

  @Override
  public boolean isCompatibileWithJack(@Nonnull Version version) {
    // We are compatible with any version of Jack.
    return true;
  }
}
