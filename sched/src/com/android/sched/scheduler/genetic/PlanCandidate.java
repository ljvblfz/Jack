/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched.scheduler.genetic;

import com.android.sched.item.Component;
import com.android.sched.item.Items;
import com.android.sched.item.TagOrMarkerOrComponent;
import com.android.sched.scheduler.FeatureSet;
import com.android.sched.scheduler.IllegalRequestException;
import com.android.sched.scheduler.ManagedRunnable;
import com.android.sched.scheduler.ManagedVisitor;
import com.android.sched.scheduler.PlanBuilder;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.SubPlanBuilder;
import com.android.sched.scheduler.TagOrMarkerOrComponentSet;
import com.android.sched.scheduler.genetic.State.ThreeState;
import com.android.sched.scheduler.genetic.stats.RunnerPercent;
import com.android.sched.scheduler.genetic.stats.RunnerPercentImpl;
import com.android.sched.scheduler.genetic.stats.TagPercent;
import com.android.sched.scheduler.genetic.stats.TagPercentImpl;
import com.android.sched.util.codec.PercentFormatter;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.StatisticId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

class PlanCandidate<T extends Component> implements Iterable<ManagedRunnable> {
  @Nonnull
  private static final Map<ManagedRunnable, StatisticId<Percent>> runnerSatisfaction =
      new HashMap<ManagedRunnable, StatisticId<Percent>>();
  @Nonnull
  private static final
      Map<Class<? extends TagOrMarkerOrComponent>, StatisticId<Percent>> needSatisfaction =
          new HashMap<Class<? extends TagOrMarkerOrComponent>, StatisticId<Percent>>();
  @Nonnull
  private static final Map<Class<? extends TagOrMarkerOrComponent>, StatisticId<Percent>>
      noSatisfaction = new HashMap<Class<? extends TagOrMarkerOrComponent>, StatisticId<Percent>>();

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final List<ManagedRunnable> plan;
  @Nonnull
  private final List<TagOrMarkerOrComponentSet> beforeTags;
  @Nonnull
  private final List<Integer> unsatisfiedConstraints;
  @Nonnull
  private final List<Integer> satisfiedConstraints;
  @Nonnull
  private final List<List<Integer>> unsatisfiedGroups;
  @Nonnull
  private final List<List<Integer>> satisfiedGroups;

  @Nonnull
  private final Request request;
  @Nonnull
  private final Class<T> rootRunOn;

  @Nonnegative
  private long unsatisfiedConstraint = 0;
  @Nonnegative
  private long satisfiedConstraint = 0;
  @Nonnegative
  private int adapterCount = 0;

  public PlanCandidate(@Nonnull PlanCandidate<T> analyzer, @Nonnull List<ManagedRunnable> plan) {
    this(analyzer.request, analyzer.rootRunOn, plan);
  }

  PlanCandidate(
      @Nonnull Request request, @Nonnull Class<T> rootRunOn, @Nonnull List<ManagedRunnable> plan) {
    Event event = tracer.start(GeneticEventType.ANALYZER);

    try {
      this.request = request;
      this.rootRunOn = rootRunOn;

      this.plan = new ArrayList<ManagedRunnable>(plan);
      this.unsatisfiedConstraints = new ArrayList<Integer>();
      this.satisfiedConstraints = new ArrayList<Integer>();
      this.unsatisfiedGroups = new ArrayList<List<Integer>>();
      this.satisfiedGroups = new ArrayList<List<Integer>>();
      this.beforeTags = new ArrayList<TagOrMarkerOrComponentSet>();

      FeatureSet features = request.getFeatures();
      Stack<Class<? extends Component>> runOn = new Stack<Class<? extends Component>>();
      List<Integer> currentGroup = new ArrayList<Integer>();
      State.ThreeState currentGroupState = ThreeState.UNDEFINED;
      TagOrMarkerOrComponentSet currentTags =
          new TagOrMarkerOrComponentSet(request.getInitialTags());
      beforeTags.add(new TagOrMarkerOrComponentSet(currentTags));

      runOn.push(rootRunOn);
      for (int idx = 0; idx < plan.size(); idx++) {
        ManagedRunnable runner = plan.get(idx);
        State current = new State();

        while (!runOn.isEmpty()) {
          if (runOn.contains(runner.getRunOn())) {
            satisfiedConstraint++;
            current.setSatisfied();

            while (runOn.peek() != runner.getRunOn()) {
              runOn.pop();
            }

            break;
          }

          if (request.getVisitors().containsAdapters(runOn.peek(), runner.getRunOn())) {
            satisfiedConstraint++;
            current.setSatisfied();

            for (ManagedVisitor visitor :
                request.getVisitors().getAdapter(runOn.peek(), runner.getRunOn())) {
              runOn.push(visitor.getRunOnAfter());
              adapterCount++;
            }

            break;
          }

          runOn.pop();
        }

        if (runOn.isEmpty()) {
          System.err.println("Impossible to go from " + runOn.peek().getSimpleName() + " to "
              + runner.getRunOn().getSimpleName());
          unsatisfiedConstraint++;
          current.setUnsatisfied();
          runOn.push(rootRunOn);
        }

        if (tracer.isTracing()) {
          TagOrMarkerOrComponentSet needed = runner.getNeededTags(features);
          for (Class<? extends TagOrMarkerOrComponent> tag :
              runner.getMissingTags(features, currentTags)) {
            tracer.getStatistic(getNeedSatisfaction(tag)).addFalse();
            needed.remove(tag);
          }

          for (Class<? extends TagOrMarkerOrComponent> tag : needed) {
            tracer.getStatistic(getNeedSatisfaction(tag)).addTrue();
          }

          TagOrMarkerOrComponentSet unsupported = runner.getUnsupportedTags(features);
          for (Class<? extends TagOrMarkerOrComponent> tag :
              runner.getForbiddenTags(features, currentTags)) {
            tracer.getStatistic(getNoSatisfaction(tag)).addFalse();
            unsupported.remove(tag);
          }

          for (Class<? extends TagOrMarkerOrComponent> tag : unsupported) {
            tracer.getStatistic(getNoSatisfaction(tag)).addTrue();
          }
        }

        if (runner.isCompatible(features, currentTags)) {
          assert runner.getUnsatisfiedConstraintCount(features, currentTags) == 0;
          satisfiedConstraint += runner.getConstraintCount(features);
          current.setSatisfied();
        } else {
          assert runner.getUnsatisfiedConstraintCount(features, currentTags) > 0;
          unsatisfiedConstraint += runner.getUnsatisfiedConstraintCount(features, currentTags);
          current.setUnsatisfied();
        }

        if (tracer.isTracing()) {
          tracer.getStatistic(getRunnerSatisfaction(runner)).add(current.isStatisfied());
        }

        if (current.isStatisfied()) {
          satisfiedConstraints.add(Integer.valueOf(idx));
          if (currentGroupState != ThreeState.SATISFIED) {
            currentGroupState = ThreeState.SATISFIED;
            satisfiedGroups.add(currentGroup);
            currentGroup = new ArrayList<Integer>();
          }
        } else {
          unsatisfiedConstraints.add(Integer.valueOf(idx));
          if (currentGroupState != ThreeState.UNSATISFIED) {
            currentGroupState = ThreeState.UNSATISFIED;
            unsatisfiedGroups.add(currentGroup);
            currentGroup = new ArrayList<Integer>();
          }
        }

        currentGroup.add(Integer.valueOf(idx));
        currentTags = runner.getAfterTags(currentTags);
        beforeTags.add(currentTags);
      }
    } finally {
      event.end();
    }
  }

  @Nonnull
  private StatisticId<Percent> getNeedSatisfaction(
      @Nonnull Class<? extends TagOrMarkerOrComponent> tag) {
    StatisticId<Percent> id = needSatisfaction.get(tag);
    if (id == null) {
      String name = Items.getName(tag);

      id = new StatisticId<Percent>("sched.tag." + name + ".need.satisfied",
          "Number of time 'need " + name + "' is satisfied", TagPercentImpl.class,
          TagPercent.class);
      needSatisfaction.put(tag, id);
    }

    return id;
  }

  @Nonnull
  private StatisticId<Percent> getNoSatisfaction(
      @Nonnull Class<? extends TagOrMarkerOrComponent> tag) {
    StatisticId<Percent> id = noSatisfaction.get(tag);
    if (id == null) {
      String name = Items.getName(tag);

      id = new StatisticId<Percent>("sched.tag." + name + ".no.satisfied",
          "Number of time 'no " + name + "' is satisfied", TagPercentImpl.class, TagPercent.class);
      noSatisfaction.put(tag, id);
    }

    return id;
  }

  @Nonnull
  private StatisticId<Percent> getRunnerSatisfaction(@Nonnull ManagedRunnable runner) {
    StatisticId<Percent> id = runnerSatisfaction.get(runner);
    if (id == null) {
      String name = runner.getName();

      id = new StatisticId<Percent>("sched.runner." + name + ".constraint.satisfied",
          "Number of time '" + name + "' has all their constraints satisfied",
          RunnerPercentImpl.class, RunnerPercent.class);
      runnerSatisfaction.put(runner, id);
    }

    return id;
  }

  double getFitness() {
    if (satisfiedConstraint > 0 || unsatisfiedConstraint > 0) {
      if (unsatisfiedConstraint > 0) {
        return (double) satisfiedConstraint
            / (double) (satisfiedConstraint + unsatisfiedConstraint);
      } else {
        return 1.0 + (1 / (double) ((10 * adapterCount) + plan.size()));
      }
    } else {
      return 1.0;
    }
  }

  boolean isValid() {
    return unsatisfiedConstraint == 0;
  }

  @Nonnegative
  int getUnsatisfiedRunnerCount() {
    return unsatisfiedConstraints.size();
  }

  @Nonnegative
  long getSatisfiedConstraintCount() {
    return satisfiedConstraint;
  }

  @Nonnegative
  long getUnsatisfiedConstraintCount() {
    return unsatisfiedConstraint;
  }

  @Nonnegative
  int getSatisfiedRunnerCount() {
    return satisfiedConstraints.size();
  }

  @Nonnegative
  int getTotalGroupCount() {
    return satisfiedGroups.size() + unsatisfiedGroups.size();
  }

  @Nonnegative
  int getSatisfiedGroupCount() {
    return satisfiedGroups.size();
  }

  @Nonnegative
  int getUnsatisfiedGroupCount() {
    return unsatisfiedGroups.size();
  }

  @Nonnegative
  int getIndexFromUnsatisfiedIndex(@Nonnegative int index) {
    return unsatisfiedConstraints.get(index).intValue();
  }

  @Nonnegative
  int getIndexFromSatisfiedIndex(@Nonnegative int index) {
    return satisfiedConstraints.get(index).intValue();
  }

  @Nonnull
  TagOrMarkerOrComponentSet getBeforeTags(@Nonnegative int index) {
    return beforeTags.get(index);
  }

  @Nonnull
  Class<? extends Component> getRunOnBefore(@Nonnegative int index) {
    if (index == 0) {
      return rootRunOn;
    } else {
      return plan.get(index - 1).getRunOn();
    }
  }

  @Nonnegative
  int getAdapterCount () {
    return adapterCount;
  }

  @Nonnull
  String getDescription() {
    try {
      return getPlanBuilder().getDescription();
    } catch (IllegalRequestException e) {
      return "Unknown";
    }
  }

  @Nonnull
  String getDetailedDescription() {
    try {
      return getPlanBuilder().getDetailedDescription();
    } catch (IllegalRequestException e) {
      return "Unknown";
    }
  }

  @Nonnull
  PlanBuilder<T> getPlanBuilder()
      throws IllegalRequestException {
    Event event = tracer.start(GeneticEventType.BUILDER);

    try {
      Stack<Class<? extends Component>> runOn = new Stack<Class<? extends Component>>();
      Stack<SubPlanBuilder<? extends Component>> adapters =
          new Stack<SubPlanBuilder<? extends Component>>();

      runOn.push(rootRunOn);
      adapters.push(request.getPlanBuilder(rootRunOn));

      for (ManagedRunnable runner : plan) {
        while (!runOn.isEmpty()) {
          if (runOn.contains(runner.getRunOn())) {
            while (runOn.peek() != runner.getRunOn()) {
              runOn.pop();
              adapters.pop();
            }

            break;
          }

          if (request.getVisitors().containsAdapters(runOn.peek(), runner.getRunOn())) {
            for (ManagedVisitor visitor :
                request.getVisitors().getAdapter(runOn.peek(), runner.getRunOn())) {
              runOn.push(visitor.getRunOnAfter());
              adapters.push(adapters.peek().appendSubPlan(visitor));
            }

            break;
          }

          runOn.pop();
          adapters.pop();
        }

        adapters.peek().append(runner);
      }

      while (runOn.peek() != rootRunOn) {
        adapters.pop();
        runOn.pop();
      }

      @SuppressWarnings("unchecked")
      PlanBuilder<T> pb = (PlanBuilder<T>) adapters.pop();
      return pb;
    } finally {
      event.end();
    }
  }

  @Override
  @Nonnull
  public String toString () {
    StringBuilder sb = new StringBuilder();

    sb.append("fitness: ");
    sb.append(getFitness());
    sb.append(", runners: ");
    sb.append(plan.size());
    sb.append(", adapters: ");
    sb.append(getAdapterCount());
    sb.append(", constraints: ");
    sb.append(satisfiedConstraint + unsatisfiedConstraint);
    sb.append(", satisfied: ");
    sb.append(satisfiedConstraint);
    sb.append(" (");
    sb.append(toPercent(satisfiedConstraint, satisfiedConstraint + unsatisfiedConstraint));
    sb.append(") ");
    sb.append(", unsatisfied: ");
    sb.append(unsatisfiedConstraint);
    sb.append(" (");
    sb.append(toPercent(unsatisfiedConstraint, satisfiedConstraint + unsatisfiedConstraint));
    sb.append(")");

    return sb.toString();
  }


  //
  // Formatter
  //

  @Nonnull
  private static PercentFormatter formatter = new PercentFormatter();
  @Nonnull
  private static String toPercent (long val, long total) {
    if (total != 0) {
      return formatter.formatValue(Double.valueOf((float) val / (float) total));
    } else {
      return formatter.formatValue(Double.valueOf(Double.NaN));
    }
  }

  @Override
  @Nonnull
  public Iterator<ManagedRunnable> iterator() {
    return plan.iterator();
  }

  @Nonnull
  public List<ManagedRunnable> getRunnables() {
    return plan;
  }

  @Nonnegative
  public int getSize() {
    return plan.size();
  }
}
