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

import com.android.sched.item.Feature;
import com.android.sched.item.ItemManager;
import com.android.sched.item.Production;
import com.android.sched.item.TagOrMarkerOrComponent;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.sched.ManagedDataListenerFactory;

import javax.annotation.Nonnull;

/**
 * Entry point of the {@code SchedLib}.
 */
@HasKeyId
public class Scheduler {
  @Nonnull
  private static final ObjectId<Scheduler> SCHEDULER =
      new ObjectId<Scheduler>("schedlib.scheduler", Scheduler.class);

  @Nonnull
  public static Scheduler getScheduler() {
    return ThreadConfig.get(SCHEDULER);
  }

  @Nonnull
  private final ItemManager featureManager;
  @Nonnull
  private final ItemManager tagOrMarkerOrCompnentManager;
  @Nonnull
  private final ItemManager productionManager;
  @Nonnull
  private final SchedulableManager schedulableManager = SchedulableManager.getSchedulableManager();

  private Scheduler() {
    this.featureManager = ItemManager.createItemManager(Feature.class);
    this.tagOrMarkerOrCompnentManager = ItemManager.createItemManager(TagOrMarkerOrComponent.class);
    this.productionManager = ItemManager.createItemManager(Production.class);

    ManagedDataListenerFactory.getManagedDataListener().notifyNoMoreItemManager();
  }

  @Nonnull
  public SchedulableSet createSchedulableSet() {
    return new SchedulableSet();
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
    return schedulableManager.getAllSchedulable();
  }

  @Nonnull
  public FeatureSet createFeatureSet () {
    return new FeatureSet(featureManager);
  }

  @Nonnull
  public TagOrMarkerOrComponentSet createTagOrMarkerOrComponentSet() {
    return new TagOrMarkerOrComponentSet(tagOrMarkerOrCompnentManager);
  }

  @Nonnull
  public ProductionSet createProductionSet() {
    return new ProductionSet(productionManager);
  }
}
