/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.sched.filter.NoFilter;
import com.android.sched.item.Component;
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Schedulable;
import com.android.sched.schedulable.VisitorSchedulable;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.ThreadWithTracer;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Allows to run a {@link Plan} using the caller thread.
 *
 * @param <T> the root <i>data</i> type
 */
@ImplementationName(iface = ScheduleInstance.class, name = "single-threaded")
public class SingleScheduleInstance<T extends Component> extends ScheduleInstance<T> {
  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  protected SingleScheduleInstance(Plan<T> plan) throws Exception {
    super(plan);
  }

  /**
   * Runs all the {@link Schedulable}s of the {@link Plan} in the defined order.
   *
   * @param t the root <i>data</i> instance
   * @throws ProcessException if an Exception is thrown by a {@code Schedulable}
   */
  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <X extends VisitorSchedulable<T>, U extends Component> void process(@Nonnull T component)
      throws ProcessException {
    Worker worker = new Worker(this, component);
    Thread thread =
        new ThreadWithTracer(null, worker, ThreadConfig.getConfig().getName() + "-worker",
            ThreadConfig.get(ScheduleInstance.DEFAULT_STACK_SIZE).longValue());
    thread.setDaemon(true);
    thread.start();

    try {
      thread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    worker.throwIfNecessary();
  }

  private class Worker<U extends Component> implements Runnable {
    @Nonnull
    private final U component;
    @Nonnull
    private final SingleScheduleInstance<U> schedule;
    @CheckForNull
    private ProcessException exception;

    public Worker(@Nonnull SingleScheduleInstance<U> schedule, @Nonnull U component) {
      this.component = component;
      this.schedule = schedule;
    }

    @Override
    public void run() {
      try {
        ComponentFilterSet filters = scheduler.createComponentFilterSet();
        filters.add(NoFilter.class);
        process(schedule, component, filters);
      } catch (ProcessException e) {
        exception = e;
      }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <U extends Component> void process(@Nonnull SingleScheduleInstance<U> schedule,
        @Nonnull U component, @Nonnull ComponentFilterSet parentFilters) throws ProcessException {
      ComponentFilterSet currentFilters = schedule.applyFilters(parentFilters, component);

      for (ScheduleInstance<U>.SchedStep<U> step : schedule.steps) {
        Schedulable schedulable = step.getInstance();
        if (!step.isSkippable(currentFilters)) {
          if (step instanceof ScheduleInstance.AdapterSchedStep) {
            ScheduleInstance<? extends Component> subSchedule =
                ((ScheduleInstance.AdapterSchedStep) step).getSubSchedInstance();

            Iterator<U> componentIter =
                schedule.adaptWithLog((AdapterSchedulable) schedulable, component);
            while (componentIter.hasNext()) {
              process((SingleScheduleInstance<U>) subSchedule, componentIter.next(),
                  currentFilters);
            }
          } else {
            if (schedulable instanceof RunnableSchedulable) {
              schedule.runWithLog((RunnableSchedulable) schedulable, component);
            } else if (schedulable instanceof VisitorSchedulable) {
              schedule.visitWithLog((VisitorSchedulable) schedulable, component);
            }
          }
        } else if (logger.isLoggable(Level.FINER)) {
          logger.log(Level.FINER,
              "Skipping {0} ''{1}'' on ''{2}'' because requiring {3} but having {4}",
              new Object[] {
                  (step instanceof ScheduleInstance.RunnableSchedStep) ? "runner" : "adapter",
                  step.getName(),
                  component,
                  step.getRequiredFilters(),
                  currentFilters});
        }
      }
    }

    public void throwIfNecessary() throws ProcessException {
      if (exception != null) {
        throw exception;
      }
    }
  }
}
