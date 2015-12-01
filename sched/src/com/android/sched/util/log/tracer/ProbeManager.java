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

import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.log.EventType;
import com.android.sched.util.log.tracer.filter.EventFilter;
import com.android.sched.util.log.tracer.probe.HeapAllocationProbe;
import com.android.sched.util.log.tracer.probe.Probe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Class which manage a collection of {@link Probe}.
 */
@HasKeyId
public class ProbeManager {
  @Nonnull
  private static final PropertyId<ProbeManager> PROBE_MANAGER = PropertyId.create(
      "sched.tracer.probes", "Define which probes use for tracing",
      new ProbeManagerCodec()).addDefaultValue(
      "event-count,gc-count,gc-duration,wall-clock");

  @Nonnull
  public static final ThreadLocal<Boolean> enable = new ThreadLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return Boolean.FALSE;
    }
  };

  @Nonnull
  public static ProbeManager getProbeManager() {
    return ThreadConfig.get(PROBE_MANAGER);
  }

  @Nonnull
  private final EventFilter[] filters;
  @Nonnull
  private final Probe[] probes;
  @Nonnull
  private final List<Probe> listProbes = new ArrayList<Probe>();
  @Nonnegative
  private final int nb;

  ProbeManager(@Nonnull ProbeManagerBuilder builder) {
    Map<Probe, EventFilter> map = builder.getProbes();
    nb = map.size();
    probes = new Probe[nb];
    filters = new EventFilter[nb];

    listProbes.addAll(map.keySet());
    // Order from high to low priority
    Collections.sort(listProbes);

    int idx = 0;
    for (Probe probe : listProbes) {
      probes[idx] = probe;
      filters[idx] = map.get(probe);
      idx++;
    }
  }

  // Reverse order, low priority to high priority
  @Nonnull
  long[] readAndStart(@Nonnull EventType type) {
    long[] values = new long[nb];

    for (int i = (nb - 1); i >= 0; i--) {
      EventFilter filter = filters[i];
      if (filter == null || filter.isEnabled(type)) {
        values[i] = probes[i].read();
      }
    }

    start();

    return values;
  }

  // Reverse order, low priority to high priority
  void start() {
    // WIP here
    HeapAllocationProbe.ensureInstall();

    for (int i = 0; i < nb; i++) {
      probes[i].start();
    }

    enable.set(Boolean.TRUE);
  }

  // Normal order, high priority to low priority
  @Nonnull
  long[] stopAndRead(@Nonnull EventType type) {
    stop();

    long[] values = new long[nb];

    for (int i = 0; i < nb; i++) {
      EventFilter filter = filters[i];
      if (filter == null || filter.isEnabled(type)) {
        values[i] = probes[i].read();
      }
    }

    return values;
  }

  // Normal order, high priority to low priority
  void stop() {
    enable.set(Boolean.FALSE);

    for (int i = 0; i < nb; i++) {
      probes[i].stop();
    }
  }

  // Reverse order, low priority to high priority
  @Nonnull
  long[] read(@Nonnull EventType type) {
    long[] values = new long[nb];

    for (int i = (nb - 1); i >= 0; i--) {
      EventFilter filter = filters[i];
      if (filter == null || filter.isEnabled(type)) {
        values[i] = probes[i].read();
      }
    }

    return values;
  }

  @Nonnegative
  int getIndex(@Nonnull Probe probe) {
    int index = listProbes.indexOf(probe);

    if (index < 0) {
      throw new IllegalArgumentException();
    }

    return index;
  }

  boolean hasFilter(@Nonnull Probe probe) {
    return filters[getIndex(probe)] != null;
  }

  @CheckForNull
  EventFilter getFilter(@Nonnull Probe probe) {
    return filters[getIndex(probe)];
  }

  @Nonnull
  List<Probe> getProbes() {
    return listProbes;
  }

  public boolean isStarted () {
    return enable.get().booleanValue();
  }
}
