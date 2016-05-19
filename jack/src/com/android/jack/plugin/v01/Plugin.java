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
  /**
   * Returns the canonical name of this plug-in. This is a unique string that
   * identifies the plug-in in Jack.
   *
   * @return the canonical name of this plug-in
   */
  @Nonnull
  String getCanonicalName();

  /**
   * Returns the friendly name of this plug-in. It can be different than the one
   * returned by {@link #getCanonicalName()}.
   *
   * @return the friendly name of this plug-in
   */
  @Nonnull
  String getFriendlyName();

  /**
   * Returns a description of this plug-in. It can be a brief sentence
   * describing the high-level functions of the plug-in.
   *
   * @return a brief description of this plug-in
   */
  @Nonnull
  String getDescription();

  /**
   * Returns the version of this plug-in.
   *
   * @return a non-null {@link Version} representing the version of this plug-in
   */
  @Nonnull
  Version getVersion();

  /**
   * Indicates whether this plug-in is compatible with the given Jack version.
   * This is used to load the plug-in only in compatible Jack versions.
   *
   * @param jackVersion the version of Jack that is running the plug-in
   * @return true if this plug-in is compatible with the given Jack version,
   * false otherwise
   */
  boolean isCompatibileWithJack(@Nonnull Version jackVersion);

  /**
   * Returns the features that are active for the given configuration.
   *
   * <p>A plug-in can support multiple features. However some features may be
   * enabled or disabled depending on the current configuration of Jack. It allows
   * the plug-in's users to select which feature(s) they want to enable/disable by
   * using properties.</p>
   *
   * <p>The {@link FeatureSet} can be created with {@link Scheduler#createFeatureSet}.
   * It can be empty if no feature is active for the given configuration.</p>
   *
   * @param config a non-null {@link Config}
   * @param scheduler a non-null {@link Scheduler}
   * @return a non-null {@link FeatureSet}
   */
  @Nonnull
  FeatureSet getFeatures(@Nonnull Config config, @Nonnull Scheduler scheduler);

  /**
   * Returns the productions that are active for the given configuration.
   *
   * <p>A plug-in can provide multiple productions. However some productions may be
   * enabled or disabled depending on the current configuration of Jack. It allows
   * the plug-in's users to select which productions(s) will be created by using
   * properties.</p>
   *
   * <p>The {@link ProductionSet} can be created with {@link Scheduler#createProductionSet()}.
   * It can be empty if no production is active for the given configuration.</p>
   *
   * @param config a non-null {@link Config}
   * @param scheduler a non-null {@link Scheduler}
   * @return a non-null {@link ProductionSet}
   */
  @Nonnull
  ProductionSet getProductions(@Nonnull Config config, @Nonnull Scheduler scheduler);

  /**
   * Returns an ordered list of the transformations that this plug-in can execute.
   * The order is relative to the dependencies between transformations: if a
   * transformation {@code T2} depends on another transformation {@code T1} then
   * {@code T2} must come after {@code T1}.
   *
   * @return a non-null list of {@link RunnableSchedulable} classes
   */
  @Nonnull
  List<Class<? extends RunnableSchedulable<? extends Component>>> getSortedRunners();

  /**
   * Returns a list of checkers that this plug-in wants to be executed when sanity checks
   * are enabled.
   *
   * @return a non-null list of {@link RunnableSchedulable} classes
   */
  @Nonnull
  Collection<Class<? extends RunnableSchedulable<? extends Component>>> getCheckerRunners();

  /**
   * Returns a reflection manager used to discover plug-in components (markers, features, ...).
   *
   * @return a non-null {@link ReflectionManager}
   */
  @Nonnull
  ReflectionManager getReflectionManager();
}
