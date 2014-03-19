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

package com.android.sched.util.log.tracer;

import com.android.sched.util.log.tracer.filter.EventFilter;
import com.android.sched.util.log.tracer.probe.Probe;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Helper class to build a {@link ProbeManager}.
 */
public class ProbeManagerBuilder {
  @Nonnull
  private final Map<Probe, EventFilter> probes = new HashMap<Probe, EventFilter>();

  public ProbeManagerBuilder() {
  }

  public void add(@Nonnull Probe probe) {
    add(probe, null);
  }

  public void add(@Nonnull Probe probe, @CheckForNull EventFilter filter) {
    probes.put(probe, filter);
  }

  @Nonnull
  public Map<Probe, EventFilter> getProbes() {
    return probes;
  }

  @Nonnull
  public ProbeManager build() {
    return new ProbeManager(this);
  }
}
