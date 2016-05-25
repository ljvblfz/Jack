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

package com.android.sched.scheduler;

import com.google.common.base.CaseFormat;

import com.android.sched.item.Component;
import com.android.sched.scheduler.genetic.stats.RunnerPercent;
import com.android.sched.scheduler.genetic.stats.RunnerPercentImpl;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.SchedEventType;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.StatisticId;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A really simple {@link PlanAmender} which tries to insert schedulables in order.
 */
public class EvenSimplerPlanAmender<T extends Component> implements PlanAmender<T> {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public boolean amendPlan(@Nonnull Request request, @Nonnull Class<T> on,
      @Nonnull List<ManagedRunnable> runners, @Nonnull PlanConstructor<T> ctor) {
    Event event = tracer.start(SchedEventType.AMENDER);
    try {
      List<ManagedRunnable> next = new LinkedList<ManagedRunnable>(runners);
      for (ManagedRunnable runner : runners) {
        ProductionSet productions = runner.getProductions();
        if (!productions.isEmpty() && !request.getTargetProductions().containsAll(productions)) {
          // Productions not allowed
          productions.removeAll(request.getTargetProductions());
          logger.log(Level.FINE,
              "Runner ''{0}'' not selected because produces {1} that is not allowed",
              new Object[] {runner.getName(), productions});
          next.remove(runner);
          continue;
        }

        FeatureSet features = runner.getSupportedFeatures();
        if (!features.isEmpty() && !request.getFeatures().containsAll(features)) {
          // Support not needed
          features.removeAll(request.getFeatures());
          logger.log(Level.FINE,
              "Runner ''{0}'' not selected because supports {1} that is not needed",
              new Object[] {runner.getName(), features});
          next.remove(runner);
          continue;
        }

        if (!productions.isEmpty() || !features.isEmpty()) {
          // Productions and Features are mandatory
          if (logger.isLoggable(Level.FINE)) {
            if (features.isEmpty()) {
              logger.log(Level.FINE, "Runner ''{0}'' is selected because produces {1}",
                  new Object[] {runner.getName(), productions});
            } else if (productions.isEmpty()) {
              logger.log(Level.FINE, "Runner ''{0}'' is selected because supports {1}",
                  new Object[] {runner.getName(), features});
            } else {
              logger.log(Level.FINE,
                  "Runner ''{0}'' is selected because supports {1} and produces {2}",
                  new Object[] {runner.getName(), features, productions});
            }
          }
        } else {
          logger.log(Level.FINE, "Runner ''{0}'' is selected", runner.getName());
        }
      }

      return amendPlan(request, on, next, ctor, 0);
    } finally {
      event.end();
    }
  }

  private boolean amendPlan(@Nonnull Request request, @Nonnull Class<T> on,
      @Nonnull List<ManagedRunnable> runners, @Nonnull PlanConstructor<T> ctor, int index) {
    if (runners.size() > 0) {
      // Some runners to insert, get the first one, and remove it
      List<ManagedRunnable> next = new LinkedList<ManagedRunnable>(runners);
      ManagedRunnable runner = runners.get(0);
      next.remove(0);
      if (ctor.isProductionValid(runner)) {
        // Consider inserting at each place until the end
        for (int idx = index; idx < ctor.getSize() + 1; idx++) {
          if (ctor.isConstraintValid(idx, runner)) {
            // If  constraints are valid, insert it
            if (tracer.isTracing()) {
              tracer.getStatistic(getRunnerSatisfaction(runner)).addTrue();
            }
            logger.log(Level.FINER, "Considering inserting runner ''{0}'' @{1}",
                new Object[] {runner.getName(), Integer.valueOf(idx)});
            ctor.insert(idx, runner);
            // And try to insert the next
            if (amendPlan(request, on, next, ctor, idx + 1)) {
              return true;
            }
            // If not possible, go back ...
            ctor.remove(idx);

            // ... and check that the original runner is still valid
            if (idx < ctor.getSize()) {
              boolean valid = ctor.isConstraintValid(idx);
              if (tracer.isTracing()) {
                tracer.getStatistic(getRunnerSatisfaction(ctor.getRunnerAt(idx))).add(valid);
              }

              if (!valid) {
                logger.log(Level.FINEST, "Aborting because ''{0}'' @{1} is not valid anymore",
                    new Object[] {ctor.getRunnerAt(idx).getName(), Integer.valueOf(idx)});
                return false;
              }
            }
          } else {
            // If the constraints are not valid, try at the next place
            if (tracer.isTracing()) {
              tracer.getStatistic(getRunnerSatisfaction(runner)).addFalse();
            }
            logger.log(Level.FINEST, "Rejecting inserting runner ''{0}'' @{1}",
                new Object[] {runner.getName(), Integer.valueOf(idx)});
          }
        }

        return false;
      } else {
        // If productions are invalid, skip it and try to insert the next runner
        if (tracer.isTracing()) {
          tracer.getStatistic(getRunnerSuperfluous(runner)).incValue();
        }
        if (logger.isLoggable(Level.FINER)) {
          logger.log(Level.FINER, "Rejecting runner ''{0}'' because produce superfluous {1}",
              new Object[] {runner.getName(), ctor.getSuperfluousProductions(runner)});
        }

        return amendPlan(request, on, next, ctor, index);
      }
    } else {
      // No more runner to insert, check the end of the plan
      for (int idx = index; idx < ctor.getSize(); idx++) {
        boolean valid = ctor.isConstraintValid(idx);

        if (tracer.isTracing()) {
          tracer.getStatistic(getRunnerSatisfaction(ctor.getRunnerAt(idx))).add(valid);
        }

        if (!valid) {
          logger.log(Level.FINEST, "Aborting because ''{0}'' @{1} is not valid anymore",
              new Object[] {ctor.getRunnerAt(idx).getName(), Integer.valueOf(idx)});
          return false;
        }
      }

      return true;
    }
  }

  @CheckForNull
  private static Map<ManagedRunnable, StatisticId<Percent>> runnerSatisfaction;

  @Nonnull
  private StatisticId<Percent> getRunnerSatisfaction(@Nonnull ManagedRunnable runner) {
    if (runnerSatisfaction == null) {
      runnerSatisfaction = new HashMap<ManagedRunnable, StatisticId<Percent>>();
    }

    assert runnerSatisfaction != null;
    StatisticId<Percent> id = runnerSatisfaction.get(runner);
    if (id == null) {
      String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, runner.getName());

      id = new StatisticId<Percent>("sched.amender.even-simpler." + name + ".constraint.satisfied",
          "Number of times '" + name + "' has all its constraints satisfied",
          RunnerPercentImpl.class, RunnerPercent.class);
      runnerSatisfaction.put(runner, id);
    }

    return id;
  }

  @CheckForNull
  private static Map<ManagedRunnable, StatisticId<Counter>> runnerSuperfluous;

  @Nonnull
  private StatisticId<Counter> getRunnerSuperfluous(@Nonnull ManagedRunnable runner) {
    if (runnerSuperfluous == null) {
      runnerSuperfluous = new HashMap<ManagedRunnable, StatisticId<Counter>>();
    }

    assert runnerSuperfluous != null;
    StatisticId<Counter> id = runnerSuperfluous.get(runner);
    if (id == null) {
      String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, runner.getName());

      id = new StatisticId<Counter>(
          "sched.amender.even-simpler." + name + ".production.superfluous",
          "Number of times '" + name + "' has produced productions already produced",
          CounterImpl.class, Counter.class);
      runnerSuperfluous.put(runner, id);
    }

    return id;
  }
}
