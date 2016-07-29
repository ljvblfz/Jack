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

import com.android.sched.util.codec.ImplementationName;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Probe which take the time of the Garbage Collector activation.
 */
@ImplementationName(iface = Probe.class, name = "gc-duration")
public class GcDurationProbe extends TimeNanosProbe {
  @Nonnull
  private final List<GarbageCollectorMXBean> gcs;

  public GcDurationProbe() {
    super("Garbage Collector duration", MIN_PRIORITY - 2);

    gcs = ManagementFactory.getGarbageCollectorMXBeans();
  }

  @Override
  @Nonnegative
  public long read() {
    long count = 0;

    for (GarbageCollectorMXBean gc : gcs) {
      count += gc.getCollectionTime();
    }

    // From millis to nanos
    return count * 1000 * 1000;
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }
}
