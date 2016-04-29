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

import com.android.sched.filter.ComponentFilterManager;
import com.android.sched.item.AbstractItemManager;
import com.android.sched.item.Feature;
import com.android.sched.item.Production;
import com.android.sched.item.TagOrMarkerOrComponent;
import com.android.sched.reflections.CompositeReflectionManager;
import com.android.sched.reflections.ReflectionFactory;
import com.android.sched.reflections.ReflectionManager;
import com.android.sched.util.sched.ManagedDataListenerFactory;

import javax.annotation.Nonnull;

/**
 * Entry point of the {@code SchedLib}.
 */
public class Scheduler {
  @Nonnull
  private final AbstractItemManager featureManager;
  @Nonnull
  private final AbstractItemManager tagOrMarkerOrComponentManager;
  @Nonnull
  private final AbstractItemManager productionManager;
  @Nonnull
  private final ComponentFilterManager filterManager;
  @Nonnull
  private final SchedulableManager schedulableManager;

  public Scheduler(@Nonnull ReflectionManager... reflextionManagers) {
    this(new CompositeReflectionManager(reflextionManagers));
  }

  public Scheduler() {
    this(ReflectionFactory.getManager());
  }

  public Scheduler(@Nonnull ReflectionManager reflectionManager) {
    this.featureManager = AbstractItemManager.createItemManager(reflectionManager, Feature.class);
    this.tagOrMarkerOrComponentManager =
        AbstractItemManager.createItemManager(reflectionManager, TagOrMarkerOrComponent.class);
    this.productionManager =
        AbstractItemManager.createItemManager(reflectionManager, Production.class);
    this.filterManager = ComponentFilterManager.createComponentFilterManager(reflectionManager);
    this.schedulableManager = SchedulableManager.getSchedulableManager(this, reflectionManager);

    ManagedDataListenerFactory.getManagedDataListener().notifyNoMoreItemManager();
  }

  @Nonnull
  public SchedulableSet createSchedulableSet() {
    return new SchedulableSet(schedulableManager);
  }

  @Nonnull
  public Request createScheduleRequest (@Nonnull SchedulableSet set) {
    Request sr;

    sr = new Request(this);
    sr.addSchedulables(set);

    return sr;
  }

  @Nonnull
  public Request createScheduleRequest () {
    return new Request(this);
  }

  @Nonnull
  public SchedulableSet getAllSchedulable() {
    return schedulableManager.getAllSchedulable(schedulableManager);
  }

  @Nonnull
  public FeatureSet createFeatureSet () {
    return new FeatureSet(featureManager);
  }

  @Nonnull
  public TagOrMarkerOrComponentSet createTagOrMarkerOrComponentSet() {
    return new TagOrMarkerOrComponentSet(tagOrMarkerOrComponentManager);
  }

  @Nonnull
  public ProductionSet createProductionSet() {
    return new ProductionSet(productionManager);
  }

  @Nonnull
  public ComponentFilterSet createComponentFilterSet() {
    return new ComponentFilterSet(filterManager);
  }

  @Nonnull
  public AbstractItemManager getFeatureManager() {
    return featureManager;
  }

  @Nonnull
  public AbstractItemManager getTagOrMarkerOrComponentManager() {
    return tagOrMarkerOrComponentManager;
  }

  @Nonnull
  public AbstractItemManager getProductionManager() {
    return productionManager;
  }

  @Nonnull
  public ComponentFilterManager getFilterManager() {
    return filterManager;
  }

  @Nonnull
  public SchedulableManager getSchedulableManager() {
    return schedulableManager;
  }
}
