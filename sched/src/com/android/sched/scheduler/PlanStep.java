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

package com.android.sched.scheduler;

import com.android.sched.item.Component;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A step of a {@link Plan} corresponding to a {@link ManagedSchedulable}.
 */
public class PlanStep {
  @Nonnull
  private final ManagedSchedulable schedulable;
  @CheckForNull
  private final Plan<? extends Component> subPlan;

  /**
   * Creates a new instance of {@link PlanStep} that encapsulates the given {@link ManagedVisitor}.
   * @param visitor the {@code ManagedVisitor}
   * @param subPlan the subPlan that derives from the {@code ManagedVisitor}
   */
  PlanStep(@Nonnull ManagedVisitor visitor, @Nonnull Plan<? extends Component> subPlan) {
    this.schedulable = visitor;
    this.subPlan = subPlan;
  }

  /**
   * Creates a new instance of {@link PlanStep} that encapsulates the given {@link ManagedRunnable}.
   */
  PlanStep(@Nonnull ManagedRunnable runner) {
    this.schedulable = runner;
    this.subPlan = null;
  }

  public boolean isVisitor() {
    return subPlan != null;
  }

  public boolean isRunner() {
    return subPlan == null;
  }

  @Nonnull
  public ManagedSchedulable getManagedSchedulable() {
    return schedulable;
  }

  @Nonnull
  public ManagedRunnable getManagedRunner() {
    return (ManagedRunnable) schedulable;
  }

  @Nonnull
  public ManagedVisitor getManagedVisitor() {
    return (ManagedVisitor) schedulable;
  }

  /**
   * Returns the subplan derived from the contained {@link ManagedVisitor}.
   * @throws IllegalStateException if the contained {@link ManagedSchedulable} is a
   * {@link ManagedRunnable}
   */
  @Nonnull
  public Plan<? extends Component> getSubPlan() {
    if (subPlan == null) {
      throw new IllegalStateException();
    }

    return subPlan;
  }
}
