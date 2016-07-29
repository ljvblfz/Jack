/*
 * Copyright (C) 2016 The Android Open Source Project
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

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Probe which take the duration of JIT compiler.
 */
@ImplementationName(iface = Probe.class,
    name = "jit-duration",
    filter = JitDurationProbe.Filter.class)
public class JitDurationProbe extends TimeNanosProbe {
  static class Filter implements ImplementationFilter {
    @Override
    public boolean isValid() {
      try {
        CompilationMXBean compilerMXBean = ManagementFactory.getCompilationMXBean();
        return compilerMXBean.isCompilationTimeMonitoringSupported();
      } catch (Throwable e) {
        return false;
      }
    }
  }

  @Nonnull
  private final CompilationMXBean compilerMXBean;

  public JitDurationProbe() {
    super("JIT compiler duration", MIN_PRIORITY);

    compilerMXBean = ManagementFactory.getCompilationMXBean();
  }

  @Override
  @Nonnegative
  public long read() {
    return compilerMXBean.getTotalCompilationTime() * 1000 * 1000;
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }
}