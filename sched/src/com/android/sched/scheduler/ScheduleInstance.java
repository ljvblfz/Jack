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

import com.android.sched.filter.ManagedComponentFilter;
import com.android.sched.filter.NoFilter;
import com.android.sched.item.Component;
import com.android.sched.item.ManagedItem;
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.schedulable.ComponentFilter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Schedulable;
import com.android.sched.schedulable.SchedulerVisitable;
import com.android.sched.schedulable.VisitorSchedulable;
import com.android.sched.transform.TransformRequest;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.LongPropertyId;
import com.android.sched.util.config.id.ReflectFactoryPropertyId;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.SchedEventType;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.util.ArrayList;
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
  public static final ReflectFactoryPropertyId<ScheduleInstance> DEFAULT_RUNNER =
      ReflectFactoryPropertyId
          .create("sched.runner", "Kind of runner for runnable", ScheduleInstance.class)
          .addArgType(Plan.class).addDefaultValue("multi-threaded");

  @Nonnull
  public static final BooleanPropertyId SKIP_ADAPTER =
      BooleanPropertyId
          .create("sched.filter.skip-adapter", "Skip adapter as soon as possible")
          .addDefaultValue(true);
  public boolean skipAdapter = ThreadConfig.get(SKIP_ADAPTER).booleanValue();

  @Nonnull
  public static final LongPropertyId DEFAULT_STACK_SIZE =
      LongPropertyId.create("sched.runner.stack-size", "Size of Worker stack in bytes").withMin(0)
          .addDefaultValue(1024 * 1024 * 2);

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  protected final Scheduler scheduler;

  @Nonnull
  protected final SchedStep<T>[] steps;
  @CheckForNull
  private final FeatureSet features;

  @Nonnull
  private final FilterInstance<T>[] filterInstances;
  @CheckForNull
  private ScheduleInstance<?> parent;
  @Nonnull
  private ComponentFilterSet filtersNeeded;

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
  @SuppressWarnings("unchecked")
  public ScheduleInstance(@Nonnull Plan<T> plan) throws Exception {
    scheduler = plan.getScheduler();
    this.features = plan.getFeatures();

    Event eventGlobal = tracer.start(SchedEventType.INSTANCIER);
    try {
      steps = new SchedStep[plan.size()];
      int idx = 0;
      filtersNeeded = scheduler.createComponentFilterSet();
      for (PlanStep step : plan) {
        SchedStep<T> instance = null;

        try {
          Event event = tracer.start(SchedEventType.INSTANCIER);
          try {
            if (step.isVisitor()) {
              ScheduleInstance<? extends Component> subInstance =
                  step.getSubPlan().getScheduleInstance();
              subInstance.parent = this;
              instance = new AdapterSchedStep<T>((ManagedVisitor) step.getManagedSchedulable(),
                  subInstance);
            } else {
              instance = new RunnableSchedStep<T>((ManagedRunnable) step.getManagedSchedulable());
            }
          } finally {
            event.end();
          }
        } catch (Exception e) {
          logger.log(Level.SEVERE,
              "Cannot instantiate schedulable '" + step.getManagedSchedulable().getName() + "'",
              e);
          throw e;
        }

        filtersNeeded.addAll(instance.runnableFilters);
        steps[idx++] = instance;
      }

      ArrayList<FilterInstance<T>> tmp = new ArrayList<FilterInstance<T>>(filtersNeeded.getSize());
      Iterator<ManagedItem> iter = filtersNeeded.managedIterator();
      idx = 0;
      while (iter.hasNext()) {
        ManagedComponentFilter mcf = (ManagedComponentFilter) iter.next();
        Class<? extends ComponentFilter<T>> filter =
            (Class<? extends ComponentFilter<T>>) mcf.getItem();
        if (mcf.getFilterOn().isAssignableFrom(plan.getRunOn())) {
          tmp.add(new FilterInstance<T>(filter, mcf));
        } else {
          filtersNeeded.remove(mcf);
        }
      }

      filterInstances = tmp.toArray(new FilterInstance[tmp.size()]);
    } finally {
      eventGlobal.end();
    }
  }

  /**
   * Runs all the {@link Schedulable}s of the {@link Plan} in the defined order.
   *
   * @param data the root <i>data</i> instance
   * @throws ProcessException if an Exception is thrown by a {@code Schedulable}
   */
  public abstract <X extends VisitorSchedulable<T>, U extends Component> void process(
      @Nonnull T data) throws ProcessException;

  //
  // Methods to log and assert
  //

  protected <U extends Component> void runWithLog(@Nonnull RunnableSchedulable<U> runner,
      @Nonnull U data) throws RunnerProcessException {
    ManagedSchedulable managedSchedulable =
        scheduler.getSchedulableManager().getManagedSchedulable(runner.getClass());
    Stack<ElementStack> visitStack = tlsVisitStack.get();

    visitStack.push(new ElementStack(features, managedSchedulable));

    Event event = logAndTrace(runner, data);
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
        scheduler.getSchedulableManager().getManagedSchedulable(visitor.getClass());
    Stack<ElementStack> visitStack = tlsVisitStack.get();

    visitStack.push(new ElementStack(features, managedSchedulable));

    Event event = logAndTrace(visitor, data);
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
    Event event = logAndTrace(adapter, data);
    try {
      return adapter.adapt(data);
    } catch (Throwable e) {
      ManagedSchedulable managedSchedulable =
          scheduler.getSchedulableManager().getManagedSchedulable(adapter.getClass());

      throw new AdapterProcessException(adapter, managedSchedulable, data, e);
    } finally {
      event.end();
    }
  }

  @Nonnull
  private <U extends Component> Event logAndTrace(@Nonnull Schedulable schedulable,
      @Nonnull U data) {
    if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "Run {0} ''{1}'' on ''{2}''",
          new Object[] {(schedulable instanceof AdapterSchedulable) ? "adapter" : "runner",
              getSchedulableName(schedulable.getClass()), data});
    }

    if (tracer.isTracing()) {
      return tracer.start(getSchedulableName(schedulable.getClass()));
    } else {
      return tracer.start("<no-name>");
    }
  }

  /**
   * Return the current {@link ManagedSchedulable}. The current is the one currently running for the
   * calling thread.
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
  protected abstract class SchedStep<T> {
    @Nonnull
    private Schedulable instance;
    @Nonnull
    protected final ComponentFilterSet runnableFilters;

    protected SchedStep(@Nonnull ManagedSchedulable managed) throws Exception {
      try {
        this.instance = managed.getSchedulable().newInstance();
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Cannot instantiate schedulable '" + managed.getName() + "'", e);
        throw e;
      }

      runnableFilters = scheduler.createComponentFilterSet();
    }

    @Nonnull
    public Schedulable getInstance() {
      return instance;
    }

    public abstract boolean isSkippable(@Nonnull ComponentFilterSet current);
    @Nonnull
    public abstract ComponentFilterSet getRequiredFilters();

    @Nonnull
    public String getName() {
      return getSchedulableName(instance.getClass());
    }
  }

  /**
   * A {@link SchedStep} dedicated to {@link ManagedRunnable}
   */
  protected class RunnableSchedStep<T> extends SchedStep<T> {

    protected RunnableSchedStep(@Nonnull ManagedRunnable managed) throws Exception {
      super(managed);
      runnableFilters.addAll(managed.getFilters());
    }

    @Override
    public boolean isSkippable(@Nonnull ComponentFilterSet current) {
      return !current.containsAll(runnableFilters);
    }

    @Override
    @Nonnull
    public ComponentFilterSet getRequiredFilters() {
      return runnableFilters.clone();
    }
  }

  /**
   * A {@link SchedStep} dedicated to {@link ManagedVisitor}
   */
  protected class AdapterSchedStep<T> extends SchedStep<T> {
    @Nonnull
    private final ScheduleInstance<? extends Component> subSchedInstance;
    @Nonnull
    protected final ComponentFilterSet adapterFilters;

    @SuppressWarnings({"rawtypes"})
    protected AdapterSchedStep(@Nonnull ManagedVisitor managed,
        @Nonnull ScheduleInstance<? extends Component> subSchedInstance) throws Exception {
      super(managed);

      adapterFilters = scheduler.createComponentFilterSet();
      this.subSchedInstance = subSchedInstance;
      for (ScheduleInstance<? extends Component>.SchedStep<? extends Component> step :
          subSchedInstance.steps) {
        runnableFilters.addAll(step.runnableFilters);
        if (step instanceof AdapterSchedStep) {
          adapterFilters.addAll(((AdapterSchedStep) step).adapterFilters);
        } else {
          adapterFilters.addAll(step.runnableFilters);
        }
      }

      Iterator<ManagedItem> iter = adapterFilters.managedIterator();
      while (iter.hasNext()) {
        ManagedComponentFilter mcf = (ManagedComponentFilter) iter.next();
        if (mcf.getFilterOn().isAssignableFrom(managed.getRunOnAfter())) {
          adapterFilters.remove(mcf);
        }
      }

      if (adapterFilters.isEmpty()) {
        adapterFilters.add(NoFilter.class);
      }
    }

    @Nonnull
    public ScheduleInstance<? extends Component> getSubSchedInstance() {
      return subSchedInstance;
    }

    @Override
    public boolean isSkippable(@Nonnull ComponentFilterSet current) {
      return skipAdapter && !current.containsOne(adapterFilters);
    }

    @Override
    @Nonnull
    public ComponentFilterSet getRequiredFilters() {
      return adapterFilters.clone();
    }
  }

  @Nonnull
  protected String getSchedulableName(@Nonnull Class<? extends Schedulable> schedulable) {
    SchedulableManager manager = scheduler.getSchedulableManager();
    ManagedSchedulable managed = manager.getManagedSchedulable(schedulable);
    String name = (managed != null)
            ? managed.getName()
            : ("<" + schedulable.getSimpleName() + ">");

    return name;
  }

  private static class ElementStack {
    @CheckForNull
    private final FeatureSet features;
    @CheckForNull
    private final ManagedSchedulable schedulable;

    ElementStack(@CheckForNull FeatureSet features, @CheckForNull ManagedSchedulable schedulable) {
      this.features = features;
      this.schedulable = schedulable;
    }
  }

  private static class FilterInstance<T extends Component> {
    @Nonnull
    public final ComponentFilter<T> filter;
    @Nonnull
    public final ManagedComponentFilter filterItem;

    public FilterInstance(@Nonnull Class<? extends ComponentFilter<T>> cl,
        @Nonnull ManagedComponentFilter item) {
      try {
        this.filter = cl.newInstance();
      } catch (InstantiationException e) {
        throw new AssertionError();
      } catch (IllegalAccessException e) {
        throw new AssertionError();
      }
      this.filterItem = item;
    }

    @Override
    public final boolean equals(@CheckForNull Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof FilterInstance)) {
        return false;
      }

      FilterInstance<?> other = (FilterInstance<?>) obj;
      return filterItem.equals(other.filterItem);
    }

    @Override
    public final int hashCode() {
      return filterItem.hashCode();
    }
  }

  @Nonnull
  protected ComponentFilterSet applyFilters(@Nonnull ComponentFilterSet parentFilters,
      @Nonnull T component) {
    ComponentFilterSet currentFilters = parentFilters.clone();
    for (FilterInstance<T> configFilter : filterInstances) {
      if (parent != null && parent.filtersNeeded.contains(configFilter.filterItem)) {
        // If the filter was already applied in a parent, and it is true, just check that the filter
        // is true also on the current component. Remove the filter if it is not the case.
        if (currentFilters.contains(configFilter.filterItem)
            && !configFilter.filter.accept(component)) {
          currentFilters.remove(configFilter.filterItem);
        }
      } else {
        if (configFilter.filter.accept(component)) {
          currentFilters.add(configFilter.filterItem);
        }
      }
    }
    return currentFilters;
  }
}
