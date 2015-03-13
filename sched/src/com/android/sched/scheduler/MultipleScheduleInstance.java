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
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.EnumPropertyId;
import com.android.sched.util.config.id.IntegerPropertyId;
import com.android.sched.util.log.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Based abstract class allowing to run a {@link Plan} using with threads.
 *
 * @param <T> the root <i>data</i> type
 */
@HasKeyId
public abstract class MultipleScheduleInstance<T extends Component> extends ScheduleInstance<T> {
  /**
   * Number of threads policy
   */
  @VariableName("policy")
  private enum NumThreadsPolicy {
    NUM_CORES,
    FIXED;
  }

  @Nonnull
  private static final EnumPropertyId<NumThreadsPolicy> NUM_THREADS_POLICY = EnumPropertyId
      .create("sched.runner.thread.kind", "Number of threads policy used by the scheduler",
          NumThreadsPolicy.class, NumThreadsPolicy.values())
      .addDefaultValue("num-cores")
      .ignoreCase()
      .requiredIf(
          ScheduleInstance.DEFAULT_RUNNER.getClazz().isSubClassOf(MultipleScheduleInstance.class));

  @Nonnull
  private static final IntegerPropertyId NUM_FIXED_THREADS = IntegerPropertyId.create(
      "sched.runner.thread.fixed.count", "Number of threads used by the scheduler")
      .withMin(2).requiredIf(NUM_THREADS_POLICY.is(NumThreadsPolicy.FIXED));

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  @Nonnegative
  private int threadPoolSize;

  public MultipleScheduleInstance(@Nonnull Plan<T> plan) throws Exception {
    super(plan);

    switch (ThreadConfig.get(NUM_THREADS_POLICY)) {
      case NUM_CORES:
        threadPoolSize = Runtime.getRuntime().availableProcessors();

        logger.log(Level.FINE,
            "Multi-threaded based executor with {0} threads (one by number of cores)",
            Integer.valueOf(threadPoolSize));
        break;
      case FIXED:
        threadPoolSize = ThreadConfig.get(NUM_FIXED_THREADS).intValue();

        logger.log(Level.FINE,
            "Multi-threaded based executor with {0} threads (on a system with {1} cores)",
            new Object[] {Integer.valueOf(threadPoolSize),
                Integer.valueOf(Runtime.getRuntime().availableProcessors())});
        break;
    }
  }

  @Nonnegative
  public int getThreadPoolSize() {
    return threadPoolSize;
  }
}
