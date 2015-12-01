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
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.schedulable.ProcessorSchedulable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A structure that allows to manually build a {@link Plan} that can be a subplan of another plan.
 * @param <T> the root <i>data</i> type of the {@code Plan}
 */
public class SubPlanBuilder<T extends Component> {
  @Nonnull
  private final Scheduler scheduler;

  @Nonnull
  private final Class<T> runOn;
  @Nonnull
  protected Plan<T> plan;

  protected SubPlanBuilder(@Nonnull Scheduler scheduler, @Nonnull Class<T> runOn) {
    this.runOn = runOn;
    this.scheduler = scheduler;

    plan = new Plan<T>(scheduler, runOn);
  }

  /**
   * Adds a {@link ProcessorSchedulable} at the end of this {@code Plan}.
   * <p>
   * The <i>data</i> type of the {@code RunnableSchedulable} must be compatible with this
   * {@code Plan}.
   */
  public void append(@Nonnull Class<? extends ProcessorSchedulable<T>> runner) {
    ManagedRunnable ir =
        (ManagedRunnable) (scheduler.getSchedulableManager().getManagedSchedulable(runner));
    if (ir == null) {
      throw new SchedulableNotRegisteredError(runner);
    }

    if (!ir.getRunOn().equals(runOn)) {
      throw new PlanError("'" + ir.getName() + "' expect to be applied on '"
          + ir.getRunOn().toString() + "' but was on '" + runOn.toString() + "'");
    }

    append(ir);
  }

  public void append(@Nonnull ManagedRunnable runner) {
    assert runner != null;
    assert runner.getRunOn().equals(
        runOn) : "Expect '" + runner.getRunOn().toString() + "', have '" + runOn.toString() + "'";

    plan.appendStep(new PlanStep(runner));
  }

  /**
   * Adds a {@link AdapterSchedulable} at the end of this {@code Plan} thus creating a
   * subplan which <i>data</i> type is the output <i>data</i> type of the {@code VisitorSchedulable}
   * .
   * <p>
   * The input <i>data</i> type of the {@code VisitorSchedulable} must be compatible with this
   * {@code Plan}.
   *
   * @return the new subplan
   */
  @Nonnull
  public <U extends Component> SubPlanBuilder<U> appendSubPlan(
      @Nonnull Class<? extends AdapterSchedulable<T, U>> visitor) {
    ManagedVisitor ia =
        (ManagedVisitor) (scheduler.getSchedulableManager().getManagedSchedulable(visitor));
    if (ia == null) {
      throw new SchedulableNotRegisteredError(visitor);
    }

    if (!ia.getRunOn().equals(runOn)) {
      throw new PlanError("'" + ia.getName() + "' expect to be applied on '"
          + ia.getRunOn().toString() + "' but was on '" + runOn.toString() + "'");
    }

    return appendSubPlan(ia);
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public <U extends Component> SubPlanBuilder<U> appendSubPlan(@Nonnull ManagedVisitor visitor) {
    assert visitor != null;
    assert visitor.getRunOn().equals(
        runOn) : "Expect '" + visitor.getRunOn().toString() + "', have '" + runOn.toString() + "'";

    SubPlanBuilder<U> subPlanBuilder =
        new SubPlanBuilder<U>(scheduler, (Class<U>) visitor.getRunOnAfter());
    plan.appendStep(new PlanStep(visitor, subPlanBuilder.plan));

    return subPlanBuilder;
  }

  /**
   * Returns the root <i>data</i> type of this {@code Plan}.
   */
  @Nonnull
  public Class<T> getRunOn() {
    return runOn;
  }

  // STOPSHIP Remove this
  @Nonnull
  @Deprecated
  public List<ManagedRunnable> getRunners() {
    List<ManagedRunnable> list = new ArrayList<ManagedRunnable>();
    getRunners(plan, list);

    return list;
  }

  @Nonnull
  private static List<ManagedRunnable> getRunners(@Nonnull Plan<?> plan,
      @Nonnull List<ManagedRunnable> list) {
    Iterator<PlanStep> iter = plan.iterator();

    while (iter.hasNext()) {
      PlanStep step = iter.next();
      ManagedSchedulable schedulable = step.getManagedSchedulable();

      if (step.isRunner()) {
        list.add((ManagedRunnable) schedulable);
      } else {
        getRunners(step.getSubPlan(), list);
      }
    }

    return list;
  }

  @Nonnull
  @Override
  public String toString() {
    return plan.toString();
  }

  @Nonnull
  public String getDescription() {
    return plan.getDescription();
  }

  @Nonnull
  public String getDetailedDescription() {
    return plan.getDetailedDescription();
  }
}
