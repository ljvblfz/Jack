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

package com.android.jack.plugin.v01;

import com.android.sched.item.Component;
import com.android.sched.reflections.ReflectionManager;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.ProductionSet;
import com.android.sched.scheduler.Scheduler;
import com.android.sched.util.Version;
import com.android.sched.util.config.Config;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * An interface for implementing a plugin in Jack.
 */
public interface Plugin {
  @Nonnull
  String getCanonicalName();
  @Nonnull
  String getFriendlyName();
  @Nonnull
  String getDescription();

  @Nonnull
  Version getVersion();
  boolean isCompatibileWithJack(@Nonnull Version jackVersion);

  @Nonnull
  FeatureSet getFeatures(@Nonnull Config config, @Nonnull Scheduler scheduler);
  @Nonnull
  ProductionSet getProductions(@Nonnull Config config, @Nonnull Scheduler scheduler);

  @Nonnull
  List<Class<? extends RunnableSchedulable<? extends Component>>> getSortedRunners();
  @Nonnull
  Collection<Class<? extends RunnableSchedulable<? extends Component>>> getCheckerRunners();

  @Nonnull
  ReflectionManager getReflectionManager();
}
