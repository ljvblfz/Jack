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
   * @throws Exception if an Exception is thrown by a {@code Schedulable}
   */
  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <X extends VisitorSchedulable<T>, U extends Component> void process(@Nonnull T t)
      throws ProcessException {
    Worker worker = new Worker(t);
    Thread thread =
        new Thread(null, worker, "worker",
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

  private class Worker<X extends VisitorSchedulable<T>, U extends Component> implements Runnable {
    @Nonnull
    private final T t;
    @CheckForNull
    private ProcessException exception;

    public Worker(@Nonnull T t) {
      this.t = t;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void run() {
      try {
        for (SchedStep step : steps) {
          Schedulable instance = step.getInstance();

          if (instance instanceof AdapterSchedulable) {
            ScheduleInstance<U> subSchedInstance = (ScheduleInstance<U>) step.getSubSchedInstance();
            assert subSchedInstance != null;

            Iterator<U> iterData = adaptWithLog((AdapterSchedulable<T, U>) instance, t);
            while (iterData.hasNext()) {
              subSchedInstance.process(iterData.next());
            }
          } else if (instance instanceof RunnableSchedulable) {
            runWithLog((RunnableSchedulable) instance, t);
          } else if (instance instanceof VisitorSchedulable) {
            visitWithLog((VisitorSchedulable) instance, t);
          }
        }
      } catch (ProcessException e) {
       exception = e;
      }
    }

    public void throwIfNecessary() throws ProcessException {
      if (exception != null) {
        throw exception;
      }
    }
  }
}
