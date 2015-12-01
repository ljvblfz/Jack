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
import com.android.sched.scheduler.GroupPlanCandidate;
import com.android.sched.scheduler.ManagedRunnable;
import com.android.sched.scheduler.ManagedSchedulable;
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.TagOrMarkerOrComponentSet;

import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

class AddPostRunnerMutation<T extends Component>
    implements EvolutionaryOperator<GroupPlanCandidate<T>> {
  @Nonnull
  private final NumberGenerator<Probability> addProbability;
  @Nonnull
  private final List<ManagedRunnable> runners = new ArrayList<ManagedRunnable>();
  @Nonnull
  private final Request request;

  public AddPostRunnerMutation(@Nonnull NumberGenerator<Probability> addProbability,
      @Nonnull Request request) {
    this.addProbability = addProbability;
    this.request = request;

    for (ManagedSchedulable schedulable : request.getRunners()) {
      if (schedulable.isRunnable() && ((ManagedRunnable) schedulable).getProductions().isEmpty()) {
        runners.add((ManagedRunnable) schedulable);
      }
    }
  }

  @Override
  @Nonnull
  public List<GroupPlanCandidate<T>> apply(
      List<GroupPlanCandidate<T>> selectedCandidates, Random rng) {
    List<GroupPlanCandidate<T>> mutatedCandidates =
        new ArrayList<GroupPlanCandidate<T>>(selectedCandidates.size());

    for (GroupPlanCandidate<T> candidate : selectedCandidates) {
      if (addProbability.nextValue().nextEvent(rng)) {
        List<ManagedRunnable> newRunners = new ArrayList<ManagedRunnable>(candidate.getRunnables());

        if (candidate.getSatisfiedRunnerCount() > 0) {
          int idx = rng.nextInt(candidate.getSatisfiedRunnerCount());
          idx = candidate.getIndexFromSatisfiedIndex(idx);
          ManagedRunnable runner = newRunners.get(idx);
          TagOrMarkerOrComponentSet afterTags = candidate.getBeforeTags(idx + 1);

          List<ManagedRunnable> candidates = new ArrayList<ManagedRunnable>();
          for (ManagedRunnable postRunner : runners) {
            if (postRunner.isCompatible(request.getFeatures(), afterTags)) {
              candidates.add(postRunner);
            }
          }

          if (candidates.size() > 0) {
            newRunners.add(idx + 1, candidates.get(rng.nextInt(candidates.size())));
          }
        }

        mutatedCandidates.add(new GroupPlanCandidate<T>(candidate, newRunners));
      } else {
        mutatedCandidates.add(candidate);
      }
    }

    return mutatedCandidates;
  }
}
