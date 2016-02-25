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

import com.android.sched.item.Component;
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Schedulable;
import com.android.sched.schedulable.VisitorSchedulable;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.ThreadWithTracer;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Allows to run a {@link Plan} using the caller thread.
 *
 * @param <T> the root <i>data</i> type
 */
@ImplementationName(iface = ScheduleInstance.class, name = "single-threaded")
public class SingleScheduleInstance<T extends Component> extends ScheduleInstance<T> {

  public SingleScheduleInstance(Plan<T> plan) throws Exception {
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

  private static class Worker<U extends Component> implements Runnable {
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
        process(schedule, component);
      } catch (ProcessException e) {
        exception = e;
      }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <U extends Component> void process(@Nonnull SingleScheduleInstance<U> schedule,
        @Nonnull U component) throws ProcessException {
      for (SchedStep step : schedule.steps) {
        Schedulable schedulable = step.getInstance();

        if (schedulable instanceof AdapterSchedulable) {
          ScheduleInstance<? extends Component> subSchedule = step.getSubSchedInstance();
          assert subSchedule != null;

          Iterator<U> componentIter =
              schedule.adaptWithLog((AdapterSchedulable) schedulable, component);
          while (componentIter.hasNext()) {
            process((SingleScheduleInstance<U>) subSchedule, componentIter.next());
          }
        } else if (schedulable instanceof RunnableSchedulable) {
          schedule.runWithLog((RunnableSchedulable) schedulable, component);
        } else if (schedulable instanceof VisitorSchedulable) {
          schedule.visitWithLog((VisitorSchedulable) schedulable, component);
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
