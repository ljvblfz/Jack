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

package com.android.jack.sample.structureprinting;

import com.android.jack.plugin.v01.SchedAnnotationProcessorBasedPlugin;
import com.android.sched.item.Component;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.ProductionSet;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.util.SubReleaseKind;
import com.android.sched.util.Version;
import com.android.sched.util.config.Config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This class declares our plugin to Jack.
 * <p>
 * We choose to name the plugin with the name of this class. This name must be the one that appears
 * in the {@code META-INF/services/com.android.jack.plugin.v01.Plugin} file of the plugin JAR file.
 * <p>
 * This plugin provides the production {@link StructurePrinting} that is implemented by the
 * {@link StructurePrinter} schedulable.
 * <p>
 * This plugin does not support any {@link com.android.sched.item.Feature}.
 */
public class StructurePrintingSamplePlugin extends SchedAnnotationProcessorBasedPlugin {
  @Nonnull
  @Override
  public String getCanonicalName() {
    return StructurePrintingSamplePlugin.class.getCanonicalName();
  }

  @Nonnull
  @Override
  public String getFriendlyName() {
    return "Structure printing plugin sample";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "List all types, fields and methods";
  }

  @Nonnull
  @Override
  public Version getVersion() {
    return new Version("structure-printing-plugin", "1.0", 1, 0, SubReleaseKind.RELEASE);
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
    ProductionSet productionSet = scheduler.createProductionSet();
    productionSet.add(StructurePrinting.class);
    return productionSet;
  }

  @Nonnull
  @Override
  public List<Class<? extends RunnableSchedulable<? extends Component>>> getSortedRunners() {
    return Collections.<Class<? extends RunnableSchedulable<? extends Component>>>singletonList(
        StructurePrinter.class);
  }

  @Nonnull
  @Override
  public Collection<Class<? extends RunnableSchedulable<? extends Component>>> getCheckerRunners() {
    return Collections.emptyList();
  }
}
