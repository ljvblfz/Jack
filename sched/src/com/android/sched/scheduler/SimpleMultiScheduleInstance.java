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
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Sample;
import com.android.sched.util.log.stats.SampleImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Allows to run a {@link Plan} using a pool of threads (see {@link ThreadPoolExecutor}). This
 * class use thread to synchronize sub-plan. This leads to a huge number of threads. This is to be
 * considered as a first step, and have to be deprecated in the future.
 *
 * @param <T> the root <i>data</i> type
 */
@ImplementationName(iface = ScheduleInstance.class, name = "multi-threaded-deprecated")
public class SimpleMultiScheduleInstance<T extends Component> extends MultipleScheduleInstance<T> {
  @CheckForNull
  private static ThreadPoolExecutor globalPool;
  @Nonnegative
  private static int                globalPoolSize;

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private static final StatisticId<Sample> THREAD_POOL = new StatisticId<Sample>(
      "sched.simplemultithread.threads", "Threads in the pool",
      SampleImpl.class, Sample.class);

  @Nonnull
  private static AtomicInteger activeTasks = new AtomicInteger(0);
  @Nonnull
  private static final StatisticId<Sample> ACTIVE_TASKS = new StatisticId<Sample>(
      "sched.simplemultithread.tasks.active", "Simultaneous active tasks",
      SampleImpl.class, Sample.class);

  @Nonnull
  private static AtomicInteger totalTasks = new AtomicInteger(0);
  @Nonnull
  private static final StatisticId<Sample> TOTAL_TASKS = new StatisticId<Sample>(
      "sched.simplemultithread.tasks", "Simultaneous active or pending tasks",
      SampleImpl.class, Sample.class);

  public SimpleMultiScheduleInstance(@Nonnull Plan<T> plan) throws Exception {
    super (plan);

    synchronized (SimpleMultiScheduleInstance.class) {
      if (globalPool == null) {
        globalPoolSize = getThreadPoolSize();

        globalPool = new ThreadPoolExecutor(
            globalPoolSize, globalPoolSize, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
      }
    }
  }

  private <T extends Component> void adapt(
      @Nonnull final ScheduleInstance<T> instance, @Nonnull Iterator<T> iter) throws Exception {

    synchronized (SimpleMultiScheduleInstance.class) {
      globalPoolSize++;
      tracer.getStatistic(THREAD_POOL).add(globalPoolSize, null);
      assert globalPool != null;
      globalPool.setMaximumPoolSize(globalPoolSize);
      globalPool.setCorePoolSize(globalPoolSize);
    }

    int taskCount = 0;
    CompletionService<Boolean> localPool = new ExecutorCompletionService<Boolean>(globalPool);

    @Nonnull
    final Config config = ThreadConfig.getConfig();

    while (iter.hasNext()) {
      final T element = iter.next();
      taskCount++;
      if (tracer.isTracing()) {
        tracer.getStatistic(TOTAL_TASKS).add(totalTasks.incrementAndGet(), null);
      }

      localPool.submit(new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          if (tracer.isTracing()) {
            tracer.getStatistic(ACTIVE_TASKS).add(activeTasks.incrementAndGet(), null);
          }

          // Save and set config
          @Nonnull
          Config old = ThreadConfig.getConfig();
          ThreadConfig.setConfig(config);

          // Run the runnable
          instance.process(element);

          // Restore config
          if (old != config) {
            ThreadConfig.setConfig(old);
          }

          if (tracer.isTracing()) {
            activeTasks.decrementAndGet();
          }

          return Boolean.TRUE;
        }});
    }

    // Wait until tasks termination
    while (taskCount-- > 0) {
      try {
        if (!localPool.take().get().booleanValue()) {
          throw new AssertionError();
        }

        if (tracer.isTracing()) {
          totalTasks.decrementAndGet();
        }
      } catch (ExecutionException e) {
        throw new AssertionError(e);
      }
    }

    synchronized (SimpleMultiScheduleInstance.class) {
      globalPoolSize--;
      assert globalPool != null;
      globalPool.setCorePoolSize(globalPoolSize);
      globalPool.setMaximumPoolSize(globalPoolSize);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <X extends VisitorSchedulable<T>, U extends Component> void process(@Nonnull T t)
      throws Exception {
    for (SchedStep step : steps) {
      Schedulable instance = step.getInstance();

      ManagedSchedulable managedSchedulable =
          schedulableManager.getManagedSchedulable(instance.getClass());

      if (instance instanceof AdapterSchedulable) {
        SimpleMultiScheduleInstance<U> subSchedInstance =
            (SimpleMultiScheduleInstance<U>) step.getSubSchedInstance();

        assert subSchedInstance != null;
        Iterator<U> iterData = adaptWithLog((AdapterSchedulable<T, U>) instance, t);
        subSchedInstance.adapt(subSchedInstance, iterData);
      } else if (instance instanceof RunnableSchedulable) {
        runWithLog((RunnableSchedulable) instance, t);
      } else if (instance instanceof VisitorSchedulable) {
        visitWithLog((VisitorSchedulable) instance, t);
      }
    }
  }
}
