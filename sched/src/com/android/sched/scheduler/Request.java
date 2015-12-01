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

import com.android.sched.item.Component;
import com.android.sched.item.Feature;
import com.android.sched.item.Production;
import com.android.sched.item.TagOrMarkerOrComponent;
import com.android.sched.schedulable.Schedulable;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Allows to formulate a request for a {@link Plan} from {@link Schedulable}s.
 * <p>The plan will in the end be able to produce {@link Production}s, support a number of
 * {@link Feature}s, and enforce constraints represented by {@link TagOrMarkerOrComponent}s.
 */
public class Request {
  @Nonnull
  private final Scheduler scheduler;

  @Nonnull
  protected TagOrMarkerOrComponentSet initialTags;
  @Nonnull
  protected TagOrMarkerOrComponentSet targetIncludeTags;
  @Nonnull
  protected TagOrMarkerOrComponentSet targetExcludeTags;
  @Nonnull
  protected ProductionSet targetProductions;
  @Nonnull
  protected FeatureSet features;

  @Nonnull
  protected RunnerSet runners;
  @CheckForNull
  protected RunnerSet candidateRunners = null;
  @Nonnull
  protected AdapterSet visitors = new AdapterSet();

  Request(@Nonnull Scheduler scheduler) {
    this.scheduler = scheduler;

    initialTags        = scheduler.createTagOrMarkerOrComponentSet();
    targetIncludeTags  = scheduler.createTagOrMarkerOrComponentSet();
    targetExcludeTags  = scheduler.createTagOrMarkerOrComponentSet();
    targetProductions  = scheduler.createProductionSet();
    features           = scheduler.createFeatureSet();
    runners            = new RunnerSet(scheduler.getSchedulableManager());
  }

  //
  // Build request methods
  //

  /**
   * Adds a {@link TagOrMarkerOrComponent} class that will be required at the end of the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addTargetIncludeTagOrMarker(@Nonnull Class<? extends TagOrMarkerOrComponent> tag) {
    targetIncludeTags.add(tag);

    return this;
  }

  /**
   * Adds a {@link TagOrMarkerOrComponentSet} whose elements will be required at the end of the
   * plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addTargetIncludeTagsOrMarkers(@Nonnull TagOrMarkerOrComponentSet set) {
    targetIncludeTags.addAll(set);

    return this;
  }

  /**
   * Sets a {@link TagOrMarkerOrComponentSet} whose elements will be required at the end of the
   * plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request setTargetIncludeTagsOrMarkers(@Nonnull TagOrMarkerOrComponentSet set) {
    targetIncludeTags = set.clone();

    return this;
  }

  /**
   * Adds a {@link TagOrMarkerOrComponent} class that will be forbidden at the end of the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addTargetExcludeTagOrMarker(@Nonnull Class<? extends TagOrMarkerOrComponent> tag) {
    targetExcludeTags.add(tag);

    return this;
  }

  /**
   * Adds a {@link TagOrMarkerOrComponentSet} whose elements will be forbidden at the end of the
   * plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addTargetExcludeTagsOrMarkers(@Nonnull TagOrMarkerOrComponentSet set) {
    targetExcludeTags.addAll(set);

    return this;
  }

  /**
   * Sets a {@link TagOrMarkerOrComponentSet} whose elements will be forbidden at the end of the
   * plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request setTargetExcludeTagsOrMarkers(@Nonnull TagOrMarkerOrComponentSet set) {
    targetExcludeTags = set.clone();

    return this;
  }

  /**
   * Adds a {@link Production} class that will be produced during the execution of the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addProduction(@Nonnull Class<? extends Production> production) {
    targetProductions.add(production);

    return this;
  }

  /**
   * Adds a {@link ProductionSet} whose elements will be produced during the execution of the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addProductions(@Nonnull ProductionSet set) {
    targetProductions.addAll(set);

    return this;
  }

  /**
   * Sets a {@link ProductionSet} whose elements will be produced during the execution of the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request setProductions(@Nonnull ProductionSet set) {
    targetProductions = set.clone();

    return this;
  }

  /**
   * Adds a {@link Feature} class that must be supported at the end of the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addFeature(@Nonnull Class<? extends Feature> feature) {
    features.add(feature);
    // Invalidate candidateRunners
    candidateRunners = null;

    return this;
  }

  /**
   * Adds a {@link FeatureSet} whose elements must be supported at the end of the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addFeatures(@Nonnull FeatureSet set) {
    features.addAll(set);

    return this;
  }

  /**
   * Sets a {@link FeatureSet} whose elements must be supported at the end of the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request setFeatures(@Nonnull FeatureSet set) {
    features = set.clone();

    return this;
  }

  /**
   * Adds a {@link TagOrMarkerOrComponent} class that will be considered present at the beginning of
   * the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addInitialTagOrMarker(@Nonnull Class<? extends TagOrMarkerOrComponent> tag) {
    initialTags.add(tag);

    return this;
  }

  /**
   * Adds a {@link TagOrMarkerOrComponentSet} whose elements will be considered present at the
   * beginning of the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addInitialTagsOrMarkers(@Nonnull TagOrMarkerOrComponentSet set) {
    initialTags.addAll(set);

    return this;
  }

  /**
   * Sets a {@link TagOrMarkerOrComponentSet} whose elements will be considered present at the
   * beginning of the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request setInitialTagsOrMarkers(@Nonnull TagOrMarkerOrComponentSet set) {
    initialTags = set.clone();

    return this;
  }

  /**
   * Adds the {@link Schedulable} to the set of {@code Schedulables} used to build the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addSchedulable(@Nonnull Class<? extends Schedulable> sched) {
    ManagedSchedulable schedulable = scheduler.getSchedulableManager().getManagedSchedulable(sched);
    if (schedulable == null) {
      throw new SchedulableNotRegisteredError(sched);
    }

    if (schedulable instanceof ManagedRunnable) {
      ManagedRunnable runner = (ManagedRunnable) schedulable;

      runners.add(runner);
      // Invalidate candidateRunners
      candidateRunners = null;
    } else if (schedulable instanceof ManagedVisitor) {
      visitors.add((ManagedVisitor) schedulable);
    } else {
      throw new AssertionError();
    }

    return this;
  }

  /**
   * Adds all the elements of the {@link SchedulableSet} to the set of {@code Schedulables} used to
   * build the plan.
   *
   * @return this {@code Request}
   */
  @Nonnull
  public Request addSchedulables(@Nonnull SchedulableSet set) {
    for (ManagedSchedulable sched : set.getAll()) {
      if (sched.isRunnable()) {
        ManagedRunnable runner = (ManagedRunnable) sched;

        runners.add(runner);
        // Invalidate candidateRunners
        candidateRunners = null;
      } else if (sched.isVisitor()) {
        visitors.add((ManagedVisitor) sched);
      } else {
        assert false;
      }
    }

    return this;
  }

  //
  // Getters
  //

  /**
   * Returns a {@link TagOrMarkerOrComponentSet} copy composed of the elements that will be required
   * at the beginning of the plan.
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getInitialTags() {
    return initialTags.clone();
  }

  /**
   * Returns a {@link TagOrMarkerOrComponentSet} copy composed of the elements that will be required
   * at the end of the plan.
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getTargetIncludeTags() {
    return targetIncludeTags.clone();
  }

  /**
   * Returns a {@link TagOrMarkerOrComponentSet} copy composed of the elements that will be
   * forbidden at the end of the plan.
   */
  @Nonnull
  public TagOrMarkerOrComponentSet getTargetExcludeTags() {
    return targetExcludeTags.clone();
  }

  /**
   * Returns a {@link ProductionSet} copy composed of the elements that will be produced at the end
   * of the plan.
   */
  @Nonnull
  public ProductionSet getTargetProductions() {
    return targetProductions.clone();
  }

  /**
   * Returns a {@link FeatureSet} copy composed of the elements that must be supported.
   */
  @Nonnull
  public FeatureSet getFeatures() {
    return features.clone();
  }

  @Nonnull
  public RunnerSet getRunners() {
    if (candidateRunners == null) {
      candidateRunners = new RunnerSet(runners);

      for (ManagedRunnable runner : runners) {
        if (!features.containsAll(runner.getSupportedFeatures())) {
          candidateRunners.remove(runner);
        }
      }
    }

    return candidateRunners;
  }

  @Nonnull
  public AdapterSet getVisitors() {
    return visitors;
  }

  //
  // Return Plan
  //

  /**
   * Automatically builds and returns the {@link Plan}.
   *
   * @param on the type class of the root <i>data</i>
   * @throws PlanNotFoundException if no valid {@code Plan} can be built
   * @throws IllegalRequestException if the {@code Request} is not valid
   */
  @Nonnull
  @SuppressWarnings("unchecked")
  public <T extends Component> Plan<T> buildPlan(@Nonnull Class<T> on)
      throws PlanNotFoundException, IllegalRequestException {
    return ((Planner<T>) (PlannerFactory.createPlanner())).buildPlan(this, on);
  }

  /**
   * Returns a {@link PlanBuilder}.
   *
   * @param runOn the type class of the root <i>data</i>
   * @throws IllegalRequestException if the {@code Request} is not valid
   */
  @Nonnull
  public <T extends Component> PlanBuilder<T> getPlanBuilder(@Nonnull Class<T> runOn)
      throws IllegalRequestException {
    return new PlanBuilder<T>(this, runOn);
  }

  @Nonnull
  public Scheduler getScheduler() {
    return scheduler;
  }
}
