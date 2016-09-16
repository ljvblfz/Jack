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
import com.android.sched.util.log.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A coherent ordered structure of {@link com.android.sched.schedulable.Schedulable}s that can be
 * applied on <i>data</i>. It can be built through {@link Request} or manually using
 * {@code append}.
 *
 * @param <T> the type of the root <i>data</i>
 */
public class Plan<T extends Component> implements Iterable<PlanStep>  {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Class<? extends Component> runOn;
  @Nonnull
  private final List<PlanStep> plan = new ArrayList<PlanStep>();
  @Nonnull
  private final Map<PlanStep, TagOrMarkerOrComponentSet> minimalMarkers =
      new HashMap<PlanStep, TagOrMarkerOrComponentSet>();

  @CheckForNull
  private FeatureSet features;
  @Nonnull
  private final Scheduler scheduler;

  public Plan(@Nonnull Scheduler scheduler, @Nonnull Class<? extends Component> runOn) {
    this.runOn = runOn;
    this.scheduler = scheduler;
  }

  @Nonnull
  public ScheduleInstance<T> getScheduleInstance() {
    return ScheduleInstance.<T> createScheduleInstance(this);
  }

  @Nonnull
  public Class<? extends Component> getRunOn() {
    return runOn;
  }

  void initPlan(@Nonnull Request request, @Nonnull PlanBuilder<T> builder) throws PlanError {
    TagOrMarkerOrComponentSet minimal =
        new TagOrMarkerOrComponentSet(request.getTargetIncludeTags());
    computeMinimal(request.getFeatures(), minimal);

    TagOrMarkerOrComponentSet tags = new TagOrMarkerOrComponentSet(request.getInitialTags());
    ProductionSet productions = new ProductionSet(request.getTargetProductions());
    productions.clear();

    logger.log(Level.FINER, "Verify plan");

    completeAndVerifyPlan(request, productions, tags, builder.getRunOn());

    logger.log(Level.FINER, "Final tags: {0}", tags.toString());
    logger.log(Level.FINER, "Final productions: {0}", productions.toString());

    if (!tags.containsAll(request.getTargetIncludeTags())) {
      throw new PlanError("Final state expect to contain "
          + request.getTargetIncludeTags().toString() + " but contains " + tags.toString());
    }

    if (tags.containsOne(request.getTargetExcludeTags())) {
      throw new PlanError("Final state expect to not contain "
          + request.getTargetExcludeTags().toString() + " but contains " + tags.toString());
    }

    if (!productions.equals(request.getTargetProductions())) {
      throw new PlanError("Plan expect to produce " + request.getTargetProductions().toString()
          + " but produce " + productions.toString());
    }
  }

  private void computeMinimal(
      @Nonnull FeatureSet features, @Nonnull TagOrMarkerOrComponentSet minimal) {
    ListIterator<PlanStep> iter = plan.listIterator(plan.size());
    while (iter.hasPrevious()) {
      PlanStep step = iter.previous();

      if (step.isRunner()) {
        minimal.addAll(step.getManagedRunner().getNeededTags(features));
        minimalMarkers.put(step, new TagOrMarkerOrComponentSet(minimal)); // Copy
      } else {
        step.getSubPlan().computeMinimal(features, minimal);
      }
    }
  }

  private void completeAndVerifyPlan(@Nonnull Request request, @Nonnull ProductionSet productions,
      @Nonnull TagOrMarkerOrComponentSet currentTags, @Nonnull Class<? extends Component> runOn)
      throws PlanError {
    // Propagate features in sub plan
    features = request.getFeatures();

    ListIterator<PlanStep> iter = plan.listIterator();
    while (iter.hasNext()) {
      PlanStep step = iter.next();

      // FINDBUGS
      assert features != null;

      logger.log(Level.FINER, "Current tags: {0}", currentTags);
      logger.log(Level.FINER, "Current productions: {0}", productions);

      if (!step.isVisitor()) {
        if (!currentTags.containsAll(
            step.getManagedRunner().getNeededTags(features))) {
          TagOrMarkerOrComponentSet missing =
              new TagOrMarkerOrComponentSet(step.getManagedRunner().getNeededTags(features));
          missing.removeAll(currentTags);

          throw new PlanError("'" + step.getManagedRunner().getName() + "' need "
              + step.getManagedRunner().getNeededTags(features).toString() + " but does not have "
              + missing.toString() + " in plan " + this.toString());
        }

        if (currentTags.containsOne(step.getManagedRunner().getUnsupportedTags(features))) {
          throw new PlanError("'" + step.getManagedRunner().getName() + "' not support "
              + step.getManagedRunner().getUnsupportedTags(features) + " but has "
              + step.getManagedRunner().getUnsupportedTags(features).getIntersection(currentTags)
              + " in plan " + this.toString());
        }

        logger.log(Level.FINER, "Runnable ''{0}'' adds {1}", new Object[] {
            step.getManagedRunner().toString(), step.getManagedRunner().getAddedTags().toString()});
        logger.log(Level.FINER, "Runnable ''{0}'' removes {1}", new Object[] {
            step.getManagedRunner().toString(),
            step.getManagedRunner().getRemovedTags().toString()});
        logger.log(Level.FINER, "Runnable ''{0}'' produces {1}", new Object[] {
            step.getManagedRunner().toString(),
            step.getManagedRunner().getProductions().toString()});

        currentTags.addAll(step.getManagedRunner().getAddedTags());
        currentTags.removeAll(step.getManagedRunner().getRemovedTags());

        productions.addAll(step.getManagedRunner().getProductions());
      } else {
        step.getSubPlan().completeAndVerifyPlan(
            request, productions, currentTags, step.getManagedVisitor().getRunOnAfter());
      }
    }
  }

  void appendStep(@Nonnull PlanStep step) {
    plan.add(step);
  }

  @Nonnull
  @Override
  public Iterator<PlanStep> iterator() {
    return plan.iterator();
  }

  public int size() {
    return plan.size();
  }

  @Nonnull
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;

    sb.append('[');
    for (PlanStep element : plan) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }

      sb.append(element.getManagedSchedulable().getName());

      if (element.isVisitor()) {
        sb.append(": ");
        sb.append(element.getSubPlan());
      }
    }
    sb.append(']');

    return new String(sb);
  }

  @Nonnull
  public String getDescription() {
    StringBuilder sb = new StringBuilder();
    getDescription(sb, "", false /* detailed */);
    return sb.toString();
  }

  @Nonnull
  public String getDetailedDescription() {
    StringBuilder sb = new StringBuilder();
    getDescription(sb, "", true /* detailed */);
    return sb.toString();
  }

  private void getDescription(@Nonnull StringBuilder sb, @Nonnull String prefix,
      boolean detailed) {
    for (PlanStep element : plan) {
      ManagedSchedulable schedulable = element.getManagedSchedulable();
      sb.append(prefix);
      sb.append(schedulable.getName());

      if (detailed && features != null) {
        sb.append(" -");

        if (schedulable.isRunnable()) {
          ManagedRunnable runnable = (ManagedRunnable) schedulable;

          if (!runnable.getNeededTags(features).isEmpty()) {
            sb.append(" need: ");
            sb.append(runnable.getNeededTags(features).toString());
          }

          if (!runnable.getUnsupportedTags(features).isEmpty()) {
            sb.append(" unsupport ");
            sb.append(runnable.getUnsupportedTags(features).toString());
          }

          if (!runnable.getAddedTags().isEmpty()) {
            sb.append(" add ");
            sb.append(runnable.getAddedTags().toString());
          }

          if (!runnable.getRemovedTags().isEmpty()) {
            sb.append(" remove ");
            sb.append(runnable.getRemovedTags().toString());
          }

          if (!runnable.getModifiedTags().isEmpty()) {
            sb.append(" modify ");
            sb.append(runnable.getModifiedTags().toString());
          }
        } else if (schedulable.isVisitor()) {
          ManagedVisitor visitor = (ManagedVisitor) schedulable;

          sb.append(" from ");
          sb.append(visitor.getRunOn().getSimpleName());
          sb.append(" to ");
          sb.append(visitor.getRunOnAfter().getSimpleName());
        }
      }

      sb.append('\n');

      if (element.isVisitor()) {
        element.getSubPlan().getDescription(sb, prefix + "  ", detailed);
      }
    }
  }

  @Nonnull
  public TagOrMarkerOrComponentSet computeFinalTagsOrMarkers(
      @Nonnull TagOrMarkerOrComponentSet initialTags) {
    TagOrMarkerOrComponentSet tags = new TagOrMarkerOrComponentSet(initialTags);

    modifyTagsOrMarkers(tags);

    return tags;
  }

  private void modifyTagsOrMarkers(@Nonnull TagOrMarkerOrComponentSet tags) {
    for (PlanStep element : plan) {
      if (!element.isVisitor()) {
        tags.addAll(element.getManagedRunner().getAddedTags());
        tags.removeAll(element.getManagedRunner().getRemovedTags());
      } else {
        element.getSubPlan().modifyTagsOrMarkers(tags);
      }
    }
  }

  @Nonnull
  public FeatureSet getFeatures() {
    assert features != null;

    return features;
  }

  @Nonnull
  public Scheduler getScheduler() {
    return scheduler;
  }
}
