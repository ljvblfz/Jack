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
import com.android.sched.scheduler.Request;
import com.android.sched.scheduler.TagOrMarkerOrComponentSet;

import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

class MoveRunnerMutation<T extends Component>
    implements EvolutionaryOperator<GroupPlanCandidate<T>> {
  @Nonnull
  private final NumberGenerator<Probability> moveProbability;
  @Nonnull
  private final Request request;

  public MoveRunnerMutation(@Nonnull NumberGenerator<Probability> moveProbability,
      @Nonnull Request request) {
    this.moveProbability = moveProbability;
    this.request = request;
  }

  @Override
  @Nonnull
  public List<GroupPlanCandidate<T>> apply(
      List<GroupPlanCandidate<T>> selectedCandidates, Random rng) {
    List<GroupPlanCandidate<T>> mutatedCandidates =
        new ArrayList<GroupPlanCandidate<T>>(selectedCandidates.size());

    for (GroupPlanCandidate<T> candidate : selectedCandidates) {
      if (moveProbability.nextValue().nextEvent(rng)) {
        List<ManagedRunnable> newRunners = new ArrayList<ManagedRunnable>(candidate.getRunnables());

        if (candidate.getSatisfiedRunnerCount() > 0) {
          int idx = rng.nextInt(candidate.getSatisfiedRunnerCount());
          idx = candidate.getIndexFromSatisfiedIndex(idx);
          ManagedRunnable runner = newRunners.get(idx);

          int forward;
          for (forward = 1; forward <= idx; forward++) {
            int newIdx = idx - forward;

            // Runner at the new pos must continue to be satisfied
            TagOrMarkerOrComponentSet beforeTags = candidate.getBeforeTags(newIdx);
            if (runner.getUnsatisfiedConstraintCount(request.getFeatures(), beforeTags) > 0) {
              break;
            }

            // Runner after the runner at the new pos must continue to be satisfied
            TagOrMarkerOrComponentSet afterTags = runner.getAfterTags(beforeTags);
            if (newRunners.get(newIdx)
                .getUnsatisfiedConstraintCount(request.getFeatures(), afterTags) > 0) {
              break;
            }
          }
          forward--;

          if (forward > 0) {
            // There is room to move forward the runner
            if (forward > 1) {
              // If there is a choice, get a random forward
              forward = rng.nextInt(forward) + 1;
            }

            newRunners.remove(idx);
            newRunners.add(idx - forward, runner);
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
