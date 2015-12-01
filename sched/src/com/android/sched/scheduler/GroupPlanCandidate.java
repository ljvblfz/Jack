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
import com.android.sched.scheduler.State.ThreeState;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Class which analyzes a plan and diagnose quantitatively problem on group of transformations.
 */
public class GroupPlanCandidate<T extends Component> extends FitnessPlanCandidate<T>
    implements Iterable<ManagedRunnable> {
  @CheckForNull
  private List<Integer> unsatisfiedConstraints;
  @CheckForNull
  private List<Integer> satisfiedConstraints;
  @CheckForNull
  private List<List<Integer>> unsatisfiedGroups;
  @CheckForNull
  private List<List<Integer>> satisfiedGroups;
  @CheckForNull
  private List<Integer> currentGroup;
  @CheckForNull
  private ThreeState currentGroupState;

  public GroupPlanCandidate(@Nonnull FitnessPlanCandidate<T> analyzer,
      @Nonnull List<ManagedRunnable> plan) {
    super(analyzer, plan);
  }

  public GroupPlanCandidate(
      @Nonnull Request request, @Nonnull Class<T> rootRunOn, @Nonnull List<ManagedRunnable> plan) {
    super(request, rootRunOn, plan);
  }

  @Override
  protected void update(@Nonnull State currentState, int index) {
    super.update(currentState, index);

    ensureAllocated();
    assert unsatisfiedConstraints != null;
    assert satisfiedConstraints != null;
    assert unsatisfiedGroups != null;
    assert satisfiedGroups != null;
    assert currentGroup != null;
    assert currentGroupState != null;

    if (currentState.isSatisfied()) {
      satisfiedConstraints.add(Integer.valueOf(index));
      if (currentGroupState != ThreeState.SATISFIED) {
        currentGroupState = ThreeState.SATISFIED;
        satisfiedGroups.add(currentGroup);
        currentGroup = new ArrayList<Integer>();
      }
    } else {
      unsatisfiedConstraints.add(Integer.valueOf(index));
      if (currentGroupState != ThreeState.UNSATISFIED) {
        currentGroupState = ThreeState.UNSATISFIED;
        unsatisfiedGroups.add(currentGroup);
        currentGroup = new ArrayList<Integer>();
      }
    }

    currentGroup.add(Integer.valueOf(index));
  }

  private void ensureAllocated() {
    unsatisfiedConstraints = new ArrayList<Integer>();
    satisfiedConstraints = new ArrayList<Integer>();
    unsatisfiedGroups = new ArrayList<List<Integer>>();
    satisfiedGroups = new ArrayList<List<Integer>>();
    currentGroup = new ArrayList<Integer>();
    currentGroupState = ThreeState.UNDEFINED;
  }

  @Nonnegative
  public int getTotalGroupCount() {
    assert unsatisfiedGroups != null;
    assert satisfiedGroups != null;

    return satisfiedGroups.size() + unsatisfiedGroups.size();
  }

  @Nonnegative
  public int getSatisfiedGroupCount() {
    assert satisfiedGroups != null;

    return satisfiedGroups.size();
  }

  @Nonnegative
  public int getUnsatisfiedGroupCount() {
    assert unsatisfiedGroups != null;

    return unsatisfiedGroups.size();
  }

  @Nonnegative
  public int getIndexFromUnsatisfiedIndex(@Nonnegative int index) {
    assert unsatisfiedConstraints != null;

    return unsatisfiedConstraints.get(index).intValue();
  }

  @Nonnegative
  public int getIndexFromSatisfiedIndex(@Nonnegative int index) {
    assert satisfiedConstraints != null;

    return satisfiedConstraints.get(index).intValue();
  }
}
