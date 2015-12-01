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

package com.android.sched.scheduler;

import com.android.sched.item.Component;
import com.android.sched.item.Items;
import com.android.sched.item.Production;
import com.android.sched.item.TagOrMarkerOrComponent;
import com.android.sched.scheduler.genetic.stats.RunnerPercent;
import com.android.sched.scheduler.genetic.stats.RunnerPercentImpl;
import com.android.sched.scheduler.genetic.stats.TagPercent;
import com.android.sched.scheduler.genetic.stats.TagPercentImpl;
import com.android.sched.util.codec.PercentFormatter;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.SchedEventType;
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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Class which analyzes a plan and diagnoses quantitatively problems.
 */
public class FitnessPlanCandidate<T extends Component> implements PlanCandidate<T> {
  @CheckForNull
  private static Map<ManagedRunnable, StatisticId<Percent>> runnerSatisfaction;
  @CheckForNull
  private static Map<Class<? extends TagOrMarkerOrComponent>, StatisticId<Percent>>
      needSatisfaction;
  @CheckForNull
  private static Map<Class<? extends TagOrMarkerOrComponent>, StatisticId<Percent>>
      noSatisfaction;

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final List<ManagedRunnable> plan;
  @Nonnull
  private final List<TagOrMarkerOrComponentSet> beforeTags;

  @Nonnull
  private final Request request;
  @Nonnull
  private final Class<T> rootRunOn;

  @Nonnegative
  private long unsatisfiedConstraint = 0;
  @Nonnegative
  private long satisfiedConstraint = 0;
  @Nonnegative
  private int satisfiedRunner = 0;
  @Nonnegative
  private int adapterCount = 0;
  @Nonnegative
  private int unsatisfiedProduction = 0;

  public FitnessPlanCandidate(@Nonnull Request request, @Nonnull Class<T> rootRunOn,
      @Nonnull PlanBuilder<T> builder) {
    this(request, rootRunOn, builder.getRunners());
  }

  public FitnessPlanCandidate(@Nonnull FitnessPlanCandidate<T> analyzer,
      @Nonnull List<ManagedRunnable> plan) {
    this(analyzer.request, analyzer.rootRunOn, plan);
  }

  FitnessPlanCandidate(
      @Nonnull Request request, @Nonnull Class<T> rootRunOn, @Nonnull List<ManagedRunnable> plan) {
    Event event = tracer.start(SchedEventType.ANALYZER);

    try {
      this.request = request;
      this.rootRunOn = rootRunOn;

      this.plan = new ArrayList<ManagedRunnable>(plan);
      this.beforeTags = new ArrayList<TagOrMarkerOrComponentSet>();

      FeatureSet features = request.getFeatures();
      Stack<Class<? extends Component>> runOn = new Stack<Class<? extends Component>>();
      ProductionSet toProduce = new ProductionSet(request.getTargetProductions());
      TagOrMarkerOrComponentSet currentTags =
          new TagOrMarkerOrComponentSet(request.getInitialTags());
      beforeTags.add(new TagOrMarkerOrComponentSet(currentTags));

      runOn.push(rootRunOn);
      for (int idx = 0; idx < plan.size(); idx++) {
        ManagedRunnable runner = plan.get(idx);
        State currentState = new State();

        for (Class<? extends Production> production : runner.getProductions()) {
          if (toProduce.contains(production)) {
            toProduce.remove(production);
          } else {
            unsatisfiedProduction++;
          }
        }

        while (!runOn.isEmpty()) {
          if (runOn.contains(runner.getRunOn())) {
            satisfiedConstraint++;
            currentState.setSatisfied();

            while (runOn.peek() != runner.getRunOn()) {
              runOn.pop();
            }

            break;
          }

          if (request.getVisitors().containsAdapters(runOn.peek(), runner.getRunOn())) {
            satisfiedConstraint++;
            currentState.setSatisfied();

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
          unsatisfiedConstraint++;
          currentState.setUnsatisfied();
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
          currentState.setSatisfied();
        } else {
          assert runner.getUnsatisfiedConstraintCount(features, currentTags) > 0;
          unsatisfiedConstraint += runner.getUnsatisfiedConstraintCount(features, currentTags);
          currentState.setUnsatisfied();
        }

        if (tracer.isTracing()) {
          tracer.getStatistic(getRunnerSatisfaction(runner)).add(currentState.isSatisfied());
        }

        if (currentState.isSatisfied()) {
          satisfiedRunner++;
        }

        update(currentState, idx);
        currentTags = runner.getAfterTags(currentTags);
        beforeTags.add(currentTags);
      }
    } finally {
      event.end();
    }
  }

  protected void update(@Nonnull State currentState, int index) {
  }

  @Nonnull
  private StatisticId<Percent> getNeedSatisfaction(
      @Nonnull Class<? extends TagOrMarkerOrComponent> tag) {
    if (needSatisfaction == null) {
      needSatisfaction =
          new HashMap<Class<? extends TagOrMarkerOrComponent>, StatisticId<Percent>>();
    }

    assert needSatisfaction != null;
    StatisticId<Percent> id = needSatisfaction.get(tag);
    if (id == null) {
      String name = Items.getName(tag);

      id = new StatisticId<Percent>("sched.tag." + name + ".need.satisfied",
          "Number of time 'need " + name + "' is satisfied", TagPercentImpl.class,
          TagPercent.class);

      assert needSatisfaction != null;
      needSatisfaction.put(tag, id);
    }

    return id;
  }

  @Nonnull
  private StatisticId<Percent> getNoSatisfaction(
      @Nonnull Class<? extends TagOrMarkerOrComponent> tag) {
    if (noSatisfaction == null) {
      noSatisfaction = new HashMap<Class<? extends TagOrMarkerOrComponent>, StatisticId<Percent>>();
    }

    assert noSatisfaction != null;
    StatisticId<Percent> id = noSatisfaction.get(tag);
    if (id == null) {
      String name = Items.getName(tag);

      id = new StatisticId<Percent>("sched.tag." + name + ".no.satisfied",
          "Number of time 'no " + name + "' is satisfied", TagPercentImpl.class, TagPercent.class);

      assert noSatisfaction != null;
      noSatisfaction.put(tag, id);
    }

    return id;
  }

  @Nonnull
  private StatisticId<Percent> getRunnerSatisfaction(@Nonnull ManagedRunnable runner) {
    if (runnerSatisfaction == null) {
      runnerSatisfaction = new HashMap<ManagedRunnable, StatisticId<Percent>>();
    }

    assert runnerSatisfaction != null;
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

  public double getFitness() {
    if (satisfiedConstraint > 0 || unsatisfiedConstraint > 0) {
      if (unsatisfiedConstraint > 0) {
        return (double) satisfiedConstraint
            / (double) (satisfiedConstraint + unsatisfiedConstraint + unsatisfiedProduction);
      } else {
        return 1.0 + (1 / (double) ((10 * adapterCount) + plan.size()));
      }
    } else {
      return 1.0;
    }
  }

  @Override
  public boolean isValid() {
    return unsatisfiedConstraint == 0 && unsatisfiedProduction == 0;
  }

  @Nonnegative
  public int getUnsatisfiedRunnerCount() {
    return plan.size() - satisfiedRunner;
  }

  @Nonnegative
  public int getSatisfiedRunnerCount() {
    return satisfiedRunner;
  }

  @Nonnegative
  long getSatisfiedConstraintCount() {
    return satisfiedConstraint;
  }

  @Nonnegative
  public long getUnsatisfiedConstraintCount() {
    return unsatisfiedConstraint;
  }

  @Nonnegative
  public int getUnsatisfiedProductionCount() {
    return unsatisfiedProduction;
  }

  @Nonnull
  public TagOrMarkerOrComponentSet getBeforeTags(@Nonnegative int index) {
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

  @Override
  @Nonnull
  public
  String getDescription() {
    try {
      return getPlanBuilder().getDescription();
    } catch (IllegalRequestException e) {
      return "Unknown";
    }
  }

  @Override
  @Nonnull
  public String getDetailedDescription() {
    try {
      return getPlanBuilder().getDetailedDescription();
    } catch (IllegalRequestException e) {
      return "Unknown";
    }
  }

  @Override
  @Nonnull
  public PlanBuilder<T> getPlanBuilder()
      throws IllegalRequestException {
    Event event = tracer.start(SchedEventType.PLANBUILDER);

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

  @Override
  @Nonnegative
  public int getSize() {
    return plan.size();
  }

  /**
   * State of a plan
   */
  protected static class State {
    @Nonnull
    private ThreeState state = ThreeState.UNDEFINED;

    enum ThreeState {
      SATISFIED,
      UNSATISFIED,
      UNDEFINED;
    }

    void setSatisfied() {
      if (state != ThreeState.UNSATISFIED) {
        state = ThreeState.SATISFIED;
      }
    }

    void setUnsatisfied() {
      state = ThreeState.UNSATISFIED;
    }

    boolean isSatisfied() {
      assert state != ThreeState.UNDEFINED;

      return state == ThreeState.SATISFIED;
    }
  }
}
