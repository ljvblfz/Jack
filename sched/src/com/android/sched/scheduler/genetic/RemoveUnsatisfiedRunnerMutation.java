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
import com.android.sched.scheduler.ManagedRunnable;

import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

class RemoveUnsatisfiedRunnerMutation<T extends Component>
    implements EvolutionaryOperator<PlanCandidate<T>> {
  @Nonnull
  private final NumberGenerator<Probability> removeProbability;


  public RemoveUnsatisfiedRunnerMutation(@Nonnull NumberGenerator<Probability> removeProbability) {
    this.removeProbability = removeProbability;
  }

  @Override
  @Nonnull
  public List<PlanCandidate<T>> apply(List<PlanCandidate<T>> selectedCandidates, Random rng) {
    List<PlanCandidate<T>> mutatedCandidates =
        new ArrayList<PlanCandidate<T>>(selectedCandidates.size());

    for (PlanCandidate<T> candidate : selectedCandidates) {
      if (candidate.getSize() != 0 && removeProbability.nextValue().nextEvent(rng)) {
        List<ManagedRunnable> newRunners = new ArrayList<ManagedRunnable>(candidate.getRunnables());

        if (candidate.getUnsatisfiedRunnerCount() > 0) {
          int idx = rng.nextInt(candidate.getUnsatisfiedRunnerCount());
          idx = candidate.getIndexFromUnsatisfiedIndex(idx);
          if (newRunners.get(idx).getProductions().isEmpty()) {
            newRunners.remove(idx);
          }
        }

        mutatedCandidates.add(new PlanCandidate<T>(candidate, newRunners));
      } else {
        mutatedCandidates.add(candidate);
      }
    }

    return mutatedCandidates;
  }
}
