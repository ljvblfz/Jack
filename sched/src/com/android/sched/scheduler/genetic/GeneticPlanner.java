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

package com.android.sched.scheduler.genetic;

import com.android.sched.item.Component;
import com.android.sched.scheduler.GroupPlanCandidate;
import com.android.sched.scheduler.IllegalRequestException;
import com.android.sched.scheduler.ManagedRunnable;
import com.android.sched.scheduler.Plan;
import com.android.sched.scheduler.PlanError;
import com.android.sched.scheduler.PlanNotFoundException;
import com.android.sched.scheduler.Planner;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.TagOrMarkerOrComponentSet;
import com.android.sched.util.log.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * @param <T> the root <i>data</i> type
 */
public abstract class GeneticPlanner<T extends Component> implements Planner<T> {
  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  @Override
  public Plan<T> buildPlan(@Nonnull Request request, @Nonnull Class<T> rootRunOn)
      throws PlanNotFoundException, IllegalRequestException {
    GroupPlanCandidate<T> planCandidate = buildPlanCandidate(request, rootRunOn);

    if (planCandidate.isValid()) {
      try {
        @SuppressWarnings("cast")
        Plan<T> plan = (Plan<T>) (planCandidate.getPlanBuilder().getPlan());

        return plan;
      } catch (PlanError e) {
        throw new AssertionError("Invalid plan returned by the Genetic Palnner");
      }
    } else {
      logger.log(Level.FINE, "Unsastisfied runners:");
      for (int idx = 0; idx < planCandidate.getUnsatisfiedConstraintCount(); idx++) {
        int idxRunnable = planCandidate.getIndexFromUnsatisfiedIndex(idx);
        ManagedRunnable runnable = planCandidate.getRunnables().get(idxRunnable);
        TagOrMarkerOrComponentSet tags = planCandidate.getBeforeTags(idxRunnable);

        logger.log(Level.FINE, "  #{0}: {1} can not have {2} can not avoid {3}", new Object[]{
          Integer.toString(idx),
          runnable,
          runnable.getMissingTags(request.getFeatures(), tags),
          runnable.getForbiddenTags(request.getFeatures(), tags)});
        logger.log(Level.FINE, "Best candidate plan: {0}", planCandidate.getDetailedDescription());
      }

      throw new PlanNotFoundException();
    }
  }

  protected abstract GroupPlanCandidate<T> buildPlanCandidate(
      @Nonnull Request request, @Nonnull Class<T> rootRunOn)
      throws PlanNotFoundException, IllegalRequestException;
}
