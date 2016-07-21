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

import com.android.sched.util.log.tracer.probe.EventCountProbe;
import com.android.sched.util.log.tracer.probe.GcCountProbe;
import com.android.sched.util.log.tracer.probe.GcDurationProbe;
import com.android.sched.util.log.tracer.probe.HeapAllocationCountProbe;
import com.android.sched.util.log.tracer.probe.HeapAllocationSizeProbe;
import com.android.sched.util.log.tracer.probe.HeapMemoryProbe;
import com.android.sched.util.log.tracer.probe.Probe;
import com.android.sched.util.log.tracer.probe.ThreadContentionCountProbe;
import com.android.sched.util.log.tracer.probe.ThreadContentionDurationProbe;
import com.android.sched.util.log.tracer.probe.ThreadTimeProbe;
import com.android.sched.util.log.tracer.probe.WallClockProbe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Templates definition.
 */
public enum TemplateFtl {
  TIME_WC("Wall Clock time", "time.html.ftl", "time-wc.html",
      new MappingProbe[]{new MappingProbe(WallClockProbe.class, "time")}, null),
  TIME_TT("Thread time", "time.html.ftl", "time-tt.html",
      new MappingProbe[]{new MappingProbe(ThreadTimeProbe.class, "time")}, null),
  COUNT("Event count", "count.html.ftl", "count-ec.html",
      new MappingProbe[]{new MappingProbe(EventCountProbe.class, "count")}, null),
  HEAP_MEMORY(
      "Heap memory usage", "memory.html.ftl", "memory-heap.html",
      new MappingProbe[]{new MappingProbe(HeapMemoryProbe.class, "memory")}, null),
  HEAP_ALLOCATION_SIZE(
          "Heap allocation size usage", "memory.html.ftl", "memory-alloc-size.html",
          new MappingProbe[]{new MappingProbe(HeapAllocationSizeProbe.class, "memory")}, null),
  HEAP_ALLOCATION_COUNT("Heap allocation count usage", "count.html.ftl", "heap-alloc-count.html",
          new MappingProbe[]{new MappingProbe(HeapAllocationCountProbe.class, "count")}, null),
  GC_TIME("Garbage Collector duration", "time.html.ftl", "time-gc.html",
      new MappingProbe[]{new MappingProbe(GcDurationProbe.class, "time")}, null),
  GC_COUNT("Garbage Collector count", "count.html.ftl", "count-gc.html",
      new MappingProbe[]{new MappingProbe(GcCountProbe.class, "count")}, null),
  CONTENTION_DURATION("Contention duration", "time.html.ftl", "time-contention.html",
      new MappingProbe[]{new MappingProbe(ThreadContentionDurationProbe.class, "time")}, null),
  CONTENTION_COUNT("Contention count", "count.html.ftl", "count-contention.html",
      new MappingProbe[]{new MappingProbe(ThreadContentionCountProbe.class, "count")}, null),
  OVERVIEW("Overview", "overview.html.ftl", "index.html",
      null, null);

  @Nonnull
  private String name;
  @Nonnull
  private String templateName;
  @Nonnull
  private String targetName;
  @Nonnull
  private final List<Class<? extends Probe>> mandatoryProbes =
      new ArrayList<Class<? extends Probe>>();
  @Nonnull
  private final List<Class<? extends Probe>> optionalProbes =
      new ArrayList<Class<? extends Probe>>();
  @Nonnull
  private final Map<Class<? extends Probe>, String> labels =
      new HashMap<Class<? extends Probe>, String>();


  /**
   * @param name Human readable name of the report
   * @param templateName file name of the template
   * @param targetName file name of the target file
   * @param mandatoryProbes array of mandatory probes and labels for this template
   * @param optionalProbes array of optional probes and labels for this template
   */
  private TemplateFtl(@Nonnull String name,
      @Nonnull String templateName,
      @Nonnull String targetName,
      @CheckForNull MappingProbe[] mandatoryProbes,
      @CheckForNull MappingProbe[] optionalProbes) {
    this.name = name;
    this.templateName = templateName;
    this.targetName = targetName;

    if (mandatoryProbes != null) {
      for (MappingProbe ms : mandatoryProbes) {
        this.mandatoryProbes.add(ms.getProbe());
        this.labels.put(ms.getProbe(), ms.getLabel());
      }
    }

    if (optionalProbes != null) {
      for (MappingProbe ms : optionalProbes) {
        this.optionalProbes.add(ms.getProbe());
        this.labels.put(ms.getProbe(), ms.getLabel());
      }
    }
  }

  @Nonnull
  public String getTemplateName() {
    return templateName;
  }

  @Nonnull
  public String getTargetName() {
    return targetName;
  }

  @Nonnull
  public List<Class<? extends Probe>> getMandatoryProbes() {
    return mandatoryProbes;
  }

  @Nonnull
  public List<Class<? extends Probe>> getOptionalProbes() {
    return optionalProbes;
  }

  @Nonnull
  public Object getName() {
    return name;
  }

  @Nonnull
  public String getLabel(@Nonnull Class<? extends Probe> probe) {
    String label = labels.get(probe);

    if (label == null) {
      throw new IllegalArgumentException();
    }

    return labels.get(probe);
  }

  private static class MappingProbe {
    @Nonnull
    private final Class<? extends Probe> probe;
    @Nonnull
    private final String                 label;

    private MappingProbe(@Nonnull Class<? extends Probe> probe, @Nonnull String label) {
      this.probe = probe;
      this.label = label;
    }

    @Nonnull
    private Class<? extends Probe> getProbe() {
      return probe;
    }

    @Nonnull
    private String getLabel() {
      return label;
    }
  }
}
