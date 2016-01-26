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
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Schedulable;
import com.android.sched.schedulable.SchedulerVisitable;
import com.android.sched.schedulable.VisitorSchedulable;
import com.android.sched.transform.TransformRequest;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.LongPropertyId;
import com.android.sched.util.config.id.ReflectFactoryPropertyId;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.SchedEventType;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class allows to instantiate and run a {@link Plan}. The processing part is abstract and must
 * be implemented by another class.
 *
 * @param <T> the root <i>data</i> type
 */
@HasKeyId
@VariableName("runner")
public abstract class ScheduleInstance<T extends Component> {
  @SuppressWarnings({"rawtypes"})
  @Nonnull
  public static final
      ReflectFactoryPropertyId<ScheduleInstance> DEFAULT_RUNNER = ReflectFactoryPropertyId.create(
          "sched.runner", "Set kind of runner for runnable", ScheduleInstance.class)
          .addArgType(Plan.class).addDefaultValue("multi-threaded");

  @Nonnull
  public static final LongPropertyId DEFAULT_STACK_SIZE =
      LongPropertyId.create("sched.runner.stack-size", "Size of Worker stack in bytes").withMin(0)
          .addDefaultValue(1024 * 1024 * 2);

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  protected final SchedulableManager schedulableManager =
      SchedulableManager.getSchedulableManager();

  @Nonnull
  protected final SchedStep[] steps;

  @CheckForNull
  private final FeatureSet features;

  // Stack of visit by thread
  private static final ThreadLocal<Stack<ElementStack>> tlsVisitStack =
      new ThreadLocal<Stack<ElementStack>>() {
        @Override
        protected Stack<ElementStack> initialValue() {
          return new Stack<ElementStack>();
        }
      };

  @SuppressWarnings("unchecked")
  public static <T extends Component> ScheduleInstance<T> createScheduleInstance(Plan<T> plan) {
    return ThreadConfig.get(DEFAULT_RUNNER).create(plan);
  }

  /**
   * Construct the {@link Plan}.
   *
   * @param plan the {@code Plan} to instantiate
   * @throws Exception if an Exception is thrown when instantiating a {@code Schedulable}
   */
  public ScheduleInstance(@Nonnull Plan<T> plan) throws Exception {
    this.features = plan.getFeatures();

    Event eventGlobal = tracer.start(SchedEventType.INSTANCIER);
    try {
      steps = new SchedStep[plan.size()];
      int idx = 0;
      for (PlanStep step : plan) {
        SchedStep instance = null;

        try {
          Event event = tracer.start(SchedEventType.INSTANCIER);
          try {
            instance = new SchedStep(step.getManagedSchedulable().getSchedulable().newInstance());
          } finally {
            event.end();
          }
        } catch (Exception e) {
          logger.log(Level.SEVERE,
              "Can not instanciate schedulable '" + step.getManagedSchedulable().getName() + "'",
              e);
          throw e;
        }

        if (step.isVisitor()) {
          instance.setSubSchedInstance(step.getSubPlan().getScheduleInstance());
        }

        steps[idx++] = instance;
      }
    } finally {
      eventGlobal.end();
    }
  }

  /**
   * Runs all the {@link Schedulable}s of the {@link Plan} in the defined order.
   *
   * @param data the root <i>data</i> instance
   * @throws Exception if an Exception is thrown by a {@code Schedulable}
   */
  public abstract <X extends VisitorSchedulable<T>, U extends Component> void process(
      @Nonnull T data) throws ProcessException;

  //
  // Methods to log and assert
  //

  protected <U extends Component> void runWithLog(
      @Nonnull RunnableSchedulable<U> runner, @Nonnull U data) throws RunnerProcessException {
    ManagedSchedulable managedSchedulable =
        schedulableManager.getManagedSchedulable(runner.getClass());
    Stack<ElementStack> visitStack = tlsVisitStack.get();

    visitStack.push(new ElementStack(features, managedSchedulable));

    Event event = logAndTrace(runner, managedSchedulable, data);
    try {
      try {
        runner.run(data);
      } catch (Throwable e) {
        throw new RunnerProcessException(runner, managedSchedulable, data, e);
      }
    } finally {
      event.end();
    }

    visitStack.pop();
  }

  @SuppressWarnings("unchecked")
  protected <X extends VisitorSchedulable<T>, U extends Component> void visitWithLog(
      @Nonnull VisitorSchedulable<U> visitor, @Nonnull U data) throws VisitorProcessException {
    ManagedSchedulable managedSchedulable =
        schedulableManager.getManagedSchedulable(visitor.getClass());
    Stack<ElementStack> visitStack = tlsVisitStack.get();

    visitStack.push(new ElementStack(features, managedSchedulable));

    Event event = logAndTrace(visitor, managedSchedulable, data);
    try {
      assert data instanceof SchedulerVisitable<?>;
      try {
        ((SchedulerVisitable<X>) data).visit((X) visitor, new TransformRequest());
      } catch (Throwable e) {
        throw new VisitorProcessException(visitor, managedSchedulable, data, e);
      }
    } finally {
      event.end();
    }

    visitStack.pop();
  }

  @Nonnull
  protected <DST extends Component> Iterator<DST> adaptWithLog(
      @Nonnull AdapterSchedulable<T, DST> adapter, @Nonnull T data) throws AdapterProcessException {
    ManagedSchedulable managedSchedulable =
        schedulableManager.getManagedSchedulable(adapter.getClass());

    Event event = logAndTrace(adapter, managedSchedulable, data);
    try {
      return adapter.adapt(data);
    } catch (Throwable e) {
      throw new AdapterProcessException(adapter, managedSchedulable, data, e);
    } finally {
      event.end();
    }
  }

  @Nonnull
  private <U extends Component> Event logAndTrace(@Nonnull Schedulable schedulable,
      @CheckForNull ManagedSchedulable managedSchedulable, @Nonnull U data) {
    String name =
        (managedSchedulable != null) ? managedSchedulable.getName() : ("<"
            + schedulable.getClass().getSimpleName() + ">");

    if (schedulable instanceof AdapterSchedulable) {
      logger.log(Level.FINEST, "Run adapter ''{0}'' on ''{1}''", new Object[] {name, data});
    } else {
      logger.log(Level.FINEST, "Run runner ''{0}'' on ''{1}''", new Object[] {name, data});
    }

    Event event = tracer.start(name);

    return event;
  }

  /**
   * Return the current {@link ManagedSchedulable}. The current is the one currently running
   * for the calling thread.
   *
   * @return the current {@link ManagedSchedulable} or null if the info is not available
   * @throws EmptyStackException if no {@link ManagedSchedulable} is running
   */
  @CheckForNull
  public static ManagedSchedulable getCurrentSchedulable() throws EmptyStackException {
    return tlsVisitStack.get().peek().schedulable;
  }

  /**
   * Return the current {@link FeatureSet}, that is features fulfill by the {@link ScheduleInstance}
   * currently running for the calling thread.
   *
   * @return the current {@link FeatureSet} or null if the info is not available.
   * @throws EmptyStackException if no {@link ScheduleInstance} is running.
   */
  @CheckForNull
  public static FeatureSet getCurrentFeatures() throws EmptyStackException {
    return tlsVisitStack.get().peek().features;
  }

  /**
   * This object represent one step in a {@link ScheduleInstance} object.
   */
  protected static class SchedStep {
    @Nonnull
    public Schedulable instance;
    @CheckForNull
    public ScheduleInstance<? extends Component> subSchedInstance;

    public SchedStep(@Nonnull Schedulable instance) {
      this.instance = instance;
      this.subSchedInstance = null;
    }

    @Nonnull
    public Schedulable getInstance() {
      return instance;
    }

    @CheckForNull
    public ScheduleInstance<? extends Component> getSubSchedInstance() {
      return subSchedInstance;
    }

    public void setSubSchedInstance(
        @Nonnull ScheduleInstance<? extends Component> subSchedInstance) {
      this.subSchedInstance = subSchedInstance;
    }
  }

  private static class ElementStack {
    @CheckForNull
    private final FeatureSet         features;
    @CheckForNull
    private final ManagedSchedulable schedulable;

    ElementStack(@CheckForNull FeatureSet features,
        @CheckForNull ManagedSchedulable schedulable) {
      this.features = features;
      this.schedulable = schedulable;
    }
  }
}
