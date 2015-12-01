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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A really simple {@link PlanAmender} which tries to insert schedulables in order.
 */
public class EvenSimplerPlanAmender<T extends Component> implements PlanAmender<T> {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @Override
  public boolean amendPlan(@Nonnull Request request, @Nonnull Class<T> on,
      @Nonnull List<ManagedRunnable> runners, @Nonnull PlanConstructor<T> ctor) {
    List<ManagedRunnable> next = new LinkedList<ManagedRunnable>(runners);
    for (ManagedRunnable runner : runners) {
      ProductionSet productions = runner.getProductions();
      if (!productions.isEmpty() && !request.getTargetProductions().containsAll(productions)) {
        // Productions not allowed
        productions.removeAll(request.getTargetProductions());
        logger.log(Level.FINE,
            "Runner ''{0}'' not selected because produces {1} that is not not allowed",
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
  }

  private boolean amendPlan(@Nonnull Request request, @Nonnull Class<T> on,
      @Nonnull List<ManagedRunnable> runners, @Nonnull PlanConstructor<T> ctor, int index) {
    Event event = TracerFactory.getTracer().start(SchedEventType.AMENDER);
    try {
      List<ManagedRunnable> next = new LinkedList<ManagedRunnable>(runners);
      for (ManagedRunnable runner : runners) {
        next.remove(runner);
        for (int idx = index; idx < ctor.getSize() + 1; idx++) {
          if (ctor.isConstraintValid(idx, runner)) {
            logger.log(Level.FINER, "Considering inserting runner ''{0}'' @{1}",
                new Object[] {runner.getName(), Integer.valueOf(idx)});
            ctor.insert(idx, runner);
            if (amendPlan(request, on, next, ctor, idx + 1)) {
              return true;
            }
            ctor.remove(idx);
          } else {
            logger.log(Level.FINER, "Rejecting inserting runner ''{0}'' @{1}",
                new Object[] {runner.getName(), Integer.valueOf(idx)});
          }
        }

        return false;
      }

      return true;
    } finally {
      event.end();
    }
  }
}
