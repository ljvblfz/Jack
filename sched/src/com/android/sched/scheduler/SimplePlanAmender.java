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

import com.android.sched.item.Component;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.SchedEventType;
import com.android.sched.util.log.TracerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A simple {@link PlanAmender} which tries to insert schedulables.
 */
public class SimplePlanAmender<T extends Component> implements PlanAmender<T> {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Override
  public boolean amendPlan(Request request, Class<T> on, List<ManagedRunnable> runners,
      PlanConstructor<T> ctor) {
    return false;
  }

  // @Override
  @Nonnull
  public FitnessPlanCandidate<T> amendPlan(@Nonnull Request request, @Nonnull Class<T> on,
      @Nonnull RunnerSet runners, @Nonnull FitnessPlanCandidate<T> current)
          throws PlanNotFoundException {
    // STOPSHIP Adapt this to the new ammendPlan method
    Event event = TracerFactory.getTracer().start(SchedEventType.AMENDER);
    try {
      RunnerSet mandatoryRunners = new RunnerSet(request.getScheduler().getSchedulableManager());
      RunnerSet optionalRunners = new RunnerSet(request.getScheduler().getSchedulableManager());

      for (ManagedRunnable runner : runners) {
        ProductionSet productions = runner.getProductions();
        if (!productions.isEmpty() && !request.getTargetProductions().containsAll(productions)) {
          // Productions not allowed
          productions.removeAll(request.getTargetProductions());
          logger.log(Level.FINE, "Runner ''{0}'' not selected because produces {1} not needed",
              new Object[] {runner.getName(), productions});
          break;
        }

        FeatureSet features = runner.getSupportedFeatures();
        if (!features.isEmpty() && !request.getFeatures().containsAll(features)) {
          // Support not needed
          features.removeAll(request.getFeatures());
          logger.log(Level.FINE, "Runner ''{0}'' not selected because supports {1} not needed",
              new Object[] {runner.getName(), features});
          break;
        }

        if (!productions.isEmpty() || !features.isEmpty()) {
          // Productions and Features are mandatory
          mandatoryRunners.add(runner);

          if (logger.isLoggable(Level.SEVERE)) {
            if (features.isEmpty()) {
              logger.log(Level.FINE, "Runner ''{0}'' is mandatory because produces {1}",
                  new Object[] {runner.getName(), productions});
            } else if (productions.isEmpty()) {
              logger.log(Level.FINE, "Runner ''{0}'' is mandatory because supports {1}",
                  new Object[] {runner.getName(), features});
            } else {
              logger.log(Level.FINE,
                  "Runner ''{0}'' is mandatory because supports {1} and produces {2}",
                  new Object[] {runner.getName(), features, productions});
            }
          }
        } else {
          optionalRunners.add(runner);
          logger.log(Level.FINE, "Runner ''{0}'' is optional", runner.getName());
        }
      }

      double maxFitness = 0;
      FitnessPlanCandidate<T> bestCandidate = current;
      for (ManagedRunnable runner : mandatoryRunners) {
        for (int idx = 0; idx < current.getRunnables().size() + 1; idx++) {
          List<ManagedRunnable> newRunners = new ArrayList<ManagedRunnable>(current.getRunnables());
          newRunners.add(idx, runner);
          FitnessPlanCandidate<T> candidate = new FitnessPlanCandidate<T>(request, on, newRunners);
          if (candidate.getFitness() > maxFitness) {
            maxFitness = candidate.getFitness();
            bestCandidate = candidate;
          }
        }

        current = bestCandidate;
      }

      if (current.isValid()) {
        return current;
      }

      logger.log(Level.FINE, "Best fitness: {0}", Double.valueOf(maxFitness));

      for (ManagedRunnable runner : optionalRunners) {
        for (int idx = 0; idx < current.getRunnables().size() + 1; idx++) {
          List<ManagedRunnable> newRunners = new ArrayList<ManagedRunnable>(current.getRunnables());
          newRunners.add(idx, runner);
          FitnessPlanCandidate<T> candidate = new FitnessPlanCandidate<T>(request, on, newRunners);
          if (candidate.isValid()) {
            return candidate;
          }

          if (candidate.getFitness() > maxFitness) {
            maxFitness = candidate.getFitness();
            bestCandidate = candidate;
          }
        }

        current = bestCandidate;
      }

      throw new PlanNotFoundException();
    } finally {
      event.end();
    }
  }
}
