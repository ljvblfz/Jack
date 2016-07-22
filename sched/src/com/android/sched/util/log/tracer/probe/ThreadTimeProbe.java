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

package com.android.sched.util.log.tracer.probe;

import com.android.sched.util.codec.ImplementationFilter;
import com.android.sched.util.codec.ImplementationName;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Probe which take the usage of per thread CPU time.
 */
@ImplementationName(iface = Probe.class,
    name = "thread-cpu-time",
    filter = ThreadTimeProbe.Filter.class)
public class ThreadTimeProbe extends TimeNanosProbe {
  static class Filter implements ImplementationFilter {
    @Override
    public boolean isValid() {
      try {
        ThreadMXBean threadManager = ManagementFactory.getThreadMXBean();
        return threadManager.isCurrentThreadCpuTimeSupported();
      } catch (Throwable e) {
        return false;
      }
    }
  }

  @Nonnull
  private final ThreadMXBean threadMXBean;

  public ThreadTimeProbe() {
    super("Per Thread CPU time", MAX_PRIORITY + 1);

    threadMXBean = ManagementFactory.getThreadMXBean();
    threadMXBean.setThreadCpuTimeEnabled(true);
  }

  @Override
  @Nonnegative
  public long read() {
    return threadMXBean.getCurrentThreadCpuTime();
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }
}