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
import com.android.sched.item.Production;
import com.android.sched.scheduler.GroupPlanCandidate;
import com.android.sched.scheduler.ManagedRunnable;
import com.android.sched.scheduler.ManagedSchedulable;
import com.android.sched.scheduler.Request;

import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

class PlanFactory<T extends Component> extends AbstractCandidateFactory<GroupPlanCandidate<T>> {

  @Nonnull
  private final Request request;
  @Nonnull
  private final Class<T> rootRunOn;
  @Nonnull
  private final List<ManagedRunnable> runners = new ArrayList<ManagedRunnable>();

  PlanFactory (@Nonnull Request request, @Nonnull Class<T> rootRunOn) {
    this.request = request;
    this.rootRunOn = rootRunOn;

      for (Class<? extends Production> production : request.getTargetProductions()) {
        for (ManagedSchedulable schedulable : request.getRunners()) {
          if (schedulable.isRunnable()) {
            if (((ManagedRunnable) schedulable).getProductions().contains(production)) {
              runners.add((ManagedRunnable) schedulable);
            }
          }
        }
      }
  }

  @Override
  @Nonnull
  public GroupPlanCandidate<T> generateRandomCandidate(Random rng) {
    List<ManagedRunnable> initial = new ArrayList<ManagedRunnable>(runners);

    return new GroupPlanCandidate<T>(request, rootRunOn, initial);
  }

}
