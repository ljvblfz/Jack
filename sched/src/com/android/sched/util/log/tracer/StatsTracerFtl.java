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

import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;

import com.android.sched.util.codec.DirectoryCodec;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.EventType;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Statistic;
import com.android.sched.util.log.stats.StatisticId;
import com.android.sched.util.log.tracer.filter.EventFilter;
import com.android.sched.util.log.tracer.probe.MemoryBytesProbe;
import com.android.sched.util.log.tracer.probe.Probe;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Logging performance metrics for internal development purposes with a FreeMarker template.
 */
@ImplementationName(iface = Tracer.class, name = "html")
@HasKeyId
public class StatsTracerFtl extends AbstractTracer {
  @Nonnull
  public static final PropertyId<Directory> TRACER_DIR = PropertyId.create(
      "sched.tracer.dir", "Define in which directory the tracer generates files",
      new DirectoryCodec(Existence.MUST_EXIST, Permission.READ | Permission.WRITE))
      .requiredIf(TracerFactory.TRACER.getClazz().isImplementedBy(StatsTracerFtl.class));

  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Directory dir = ThreadConfig.get(TRACER_DIR);

  @Nonnull
  private final Map<EventType, Total> map = new HashMap<EventType, Total>();

  private class Total {
    @Nonnull
    EventType type;
    @Nonnull
    SimpleStat[] withChildrenValue = new SimpleStat[getProbeManager().getProbes().size()];
    @Nonnull
    SimpleStat[] withoutChildrenValue = new SimpleStat[getProbeManager().getProbes().size()];

    @Nonnull
    Set<EventType> children = new HashSet<EventType>();

    Total(@Nonnull EventType type) {
      this.type = type;

      for (int idx = 0; idx < getProbeManager().getProbes().size(); idx++) {
        withChildrenValue[idx] = new SimpleStat();
        withoutChildrenValue[idx] = new SimpleStat();
      }
    }

    @Override
    @Nonnull
    public String toString() {
      return type.getName();
    }
  }

  @Override
  public void stopTracer() {
    /*
     * Remove overhead
     */

    map.remove(TracerEventType.OVERHEAD);

    /*
     * Alloc data model
     */

    Map<String, Object> modelRoot = new HashMap<String, Object>();
    List<Map<String, Object>> modelStats = new ArrayList<Map<String, Object>>();
    modelRoot.put("stats", modelStats);
    Map<String, Map<String, Object>> modelParams = new HashMap<String, Map<String, Object>>();
    modelRoot.put("probes", modelParams);
    List<Map<String, Object>> modelTemps = new ArrayList<Map<String, Object>>();
    modelRoot.put("templates", modelTemps);
    Map<String, Object> modelSysts = new HashMap<String, Object>();
    modelRoot.put("systems", modelSysts);

    /*
     * Put systems in data model
     */

    {
      Date date = new Date(System.currentTimeMillis());
      modelSysts.put("date", date.toString());

      modelSysts.put("config", getConfigFileName());

      OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
      modelSysts.put("os_arch", os.getArch());
      modelSysts.put("os_proc_nb", Integer.valueOf(os.getAvailableProcessors()));
      modelSysts.put("os_name", os.getName());
      modelSysts.put("os_version", os.getVersion());

      RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
      modelSysts.put("vm_name", runtime.getVmName());
      modelSysts.put("vm_vendor", runtime.getVmVendor());
      modelSysts.put("vm_version", runtime.getVmVersion());
      modelSysts.put("vm_options", Joiner.on(' ').skipNulls().join(runtime.getInputArguments()));
      modelSysts.put(
          "vm_memory_max", MemoryBytesProbe.formatBytes(Runtime.getRuntime().maxMemory()));

      boolean first = true;
      StringBuilder gcs = new StringBuilder();
      for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
        if (!first) {
          gcs.append(", ");
        } else {
          first = false;
        }
        gcs.append(gc.getName());
      }
      modelSysts.put("vm_collectors", gcs);

      try {
        modelSysts.put("host_name",  InetAddress.getLocalHost().getHostName());
      } catch (UnknownHostException e1) {
        // Do our best
      }

      Method method;
      try {
        method = os.getClass().getMethod("getTotalPhysicalMemorySize");
        method.setAccessible(true);
        modelSysts.put("os_memory_physical",
            MemoryBytesProbe.formatBytes(((Long) method.invoke(os)).longValue()));
      } catch (Throwable t) {
        // Do our best
      }

      try {
        method = os.getClass().getMethod("getTotalSwapSpaceSize");
        method.setAccessible(true);
        modelSysts.put(
            "os_memory_swap", MemoryBytesProbe.formatBytes(((Long) method.invoke(os)).longValue()));
      } catch (Throwable t) {
        // Do our best
      }

      try {
        method = os.getClass().getMethod("getCommittedVirtualMemorySize");
        method.setAccessible(true);
        modelSysts.put("os_memory_committed",
            MemoryBytesProbe.formatBytes(((Long) method.invoke(os)).longValue()));
      } catch (Throwable t) {
        // Do our best
      }
    }

    /*
     * Put templates in data model
     */

    List<Class<? extends Probe>> classProbes = new ArrayList<Class<? extends Probe>>();
    for (Probe probe : probeManager.getProbes()) {
      classProbes.add(probe.getClass());
    }

    List<TemplateFtl> templates = new ArrayList<TemplateFtl>();
    for (TemplateFtl template : TemplateFtl.values()) {
      if (// All mandatory probes are present, or
          (template.getMandatoryProbes().size() > 0
              && classProbes.containsAll(template.getMandatoryProbes())) ||
          // At leat one optional probe is present, or
          (template.getOptionalProbes().size() > 0
              && !Collections.disjoint(template.getOptionalProbes(), classProbes)) ||
          // No probe is requiered, it is a general template
          (template.getMandatoryProbes().size() == 0
              && template.getOptionalProbes().size() == 0)) {

        Map<String, Object> elt = new HashMap<String, Object>();
        elt.put("file", template.getTargetName());
        elt.put("name", template.getName());

        modelTemps.add(elt);

        templates.add(template);
      }
    }

    ListIterator<TemplateFtl> iterTemplate = templates.listIterator();
    while (iterTemplate.hasNext()) {
      int templateIdx = iterTemplate.nextIndex();
      TemplateFtl template = iterTemplate.next();
      StringBuilder summary = new StringBuilder();
      Configuration config;
      Writer writer;

      /*
       * Complete data model
       */

      boolean first = true;
      boolean hasFilter = false;
      modelStats.clear();
      modelParams.clear();
      ListIterator<? extends Probe> iterProbe = probeManager.getProbes().listIterator();
      while (iterProbe.hasNext()) {
        int probeIndex = iterProbe.nextIndex();
        Probe probe = iterProbe.next();

        if (template.getMandatoryProbes().contains(probe.getClass()) ||
            template.getOptionalProbes().contains(probe.getClass())) {
          String label = template.getLabel(probe.getClass());
          long total = 0;

          for (Total c : map.values()) {
            if (c.withChildrenValue[probeIndex].getTotal() != 0 ||
                c.withoutChildrenValue[probeIndex].getTotal() != 0) {
              Map<String, Object> elt = new HashMap<String, Object>();
              elt.put("name", c.type.getName());
              elt.put("file", getEventFileName(c.type));
              // Following convention:
              // - Begin with a v_ for value, and f_ for formatting value in string
              // - End with _with for cumulative with children, _without for exclusive
              elt.put("v_" + label + "_with",
                  Long.valueOf((long) c.withChildrenValue[probeIndex].getTotal()));
              elt.put("f_" + label + "_with",
                  probe.formatValue((long) c.withChildrenValue[probeIndex].getTotal()));
              elt.put("v_" + label + "_without",
                  Long.valueOf((long) c.withoutChildrenValue[probeIndex].getTotal()));
              elt.put("f_" + label + "_without",
                  probe.formatValue((long) c.withoutChildrenValue[probeIndex].getTotal()));
              modelStats.add(elt);
              // Compute total
              total += c.withoutChildrenValue[probeIndex].getTotal();
            }
          }

          Map<String, Object> elt = new HashMap<String, Object>();
          elt.put("name", probe.getDescription());
          elt.put("v_Total", Long.valueOf(total));
          elt.put("f_Total", probe.formatValue(total));

          EventFilter filter = probeManager.getFilter(probe);
          if (filter != null) {
            hasFilter = true;
            elt.put("filter", filter.getDescription());
          }

          modelParams.put(label, elt);

          if (!first) {
            summary.append(", ");
          } else {
            first = false;
          }

          summary.append(probe.getDescription());
          summary.append(": ");
          summary.append(probe.formatValue(total));
        }
      }

      if (summary.length() > 0) {
        modelTemps.get(templateIdx).put("summary", summary.toString());
      }

      modelTemps.get(templateIdx).put("filter", Boolean.valueOf(hasFilter).toString());

      /*
       * Process
       */

      config = new Configuration();
      config.setClassForTemplateLoading(StatsTracerFtl.class, "templates/");
      config.setObjectWrapper(new DefaultObjectWrapper());
      config.setNumberFormat("0.######");

      File file = new File(dir.getFile(), template.getTargetName());
      try {
        writer = new BufferedWriter(new FileWriter(file), 102400);
      } catch (IOException e) {
        logger.log(
            Level.SEVERE, "Unable to open tracer file '" + file.getAbsolutePath() + "'", e);
        return;
      }

      logger.log(Level.FINER, "Produce template ''{0}'' in file ''{1}''",
          new Object[] {template.getTemplateName(), file.getAbsolutePath()});


      /* Merge data-model with template */
      Template tmp;
      try {
        tmp = config.getTemplate(template.getTemplateName());
        try {
          tmp.process(modelRoot, writer);
        } catch (IOException e) {
          logger.log(
              Level.SEVERE, "Unable to write tracer file '" + file.getAbsolutePath() + "'", e);
            return;
        } catch (TemplateException e) {
          logger.log(
              Level.SEVERE, "Unable to use template '" + template.getTemplateName() + "'", e);
          continue;
        }
      } catch (IOException e) {
        logger.log(
            Level.SEVERE, "Unable to read template '" + template.getTemplateName() + "'", e);
        continue;
      } finally {
        try {
          writer.flush();
          writer.close();
        } catch (IOException e) {
          logger.log(
              Level.SEVERE, "Unable to close tracer file '" + file.getAbsolutePath() + "'", e);
        }
      }
    }

    for (Total total : map.values()) {
      generateEventReport(total);
    }

    for (StatisticId<? extends Statistic> id : getStatisticsIds()) {
      generateStatReport(id);
    }

    generateConfigReport();
  }

  @CheckForNull
  private String getFileName(@Nonnull Object object) {
    if (object instanceof EventType) {
      return getEventFileName((EventType) object);
    } else if (object instanceof StatisticId) {
      return getStatFileName((StatisticId<?>) object);
    } else {
      return null;
    }
  }

  @Nonnull
  private String getEventFileName(@Nonnull EventType type) {
    return "event-"
        + type.getName().replace(' ', '_').replace('\'', '_').replace('"', '_').replace(':', '_')
        + ".html";
  }

  @Nonnull
  private String getStatFileName(@Nonnull StatisticId<? extends Statistic> id) {
    return "stat-"
        + id.getName().replace(' ', '_').replace('\'', '_').replace('"', '_').replace(':', '_')
        + ".html";
  }

  @Nonnull
  private String getConfigFileName() {
    return "config.html";
  }

  private void generateEventReport(@Nonnull Total total) {
    String template = "event.html.ftl";

    Map<String, Object> dmRoot = new HashMap<String, Object>();
    dmRoot.put("name", "Event " + total.type.getName());

    //
    // Tables
    //

    List<Map<String, Object>> dmTables = new ArrayList<Map<String, Object>>();
    dmRoot.put("tables", dmTables);


    //
    // Children
    //

    if (!total.children.isEmpty()) {
      Map<String, Object> dmTable = new HashMap<String, Object>();
      dmTables.add(dmTable);
      dmTable.put("name", "Children");
      List<String> dmHeader = new ArrayList<String>();
      dmTable.put("header", dmHeader);
      List<List<Object>> dmDatas = new ArrayList<List<Object>>();
      dmTable.put("data", dmDatas);

      // Add name column
      dmHeader.add("Name");
      dmHeader.add("string");
      for (EventType type : Ordering.usingToString().immutableSortedCopy(total.children)) {
        List<Object> dmData = new ArrayList<Object>();
        dmDatas.add(dmData);
        dmData.add(type.getName().replace("'", "\\'"));
        dmData.add(getEventFileName(type));
      }
    }

    //
    // Probes
    //

    {
      Map<String, Object> dmTable = new HashMap<String, Object>();
      dmTables.add(dmTable);
      if (total.children.isEmpty()) {
        dmTable.put("name", "Probes");
      } else {
        dmTable.put("name", "Probes without children");
      }
      List<String> dmHeader = new ArrayList<String>();
      dmTable.put("header", dmHeader);
      List<List<Object>> dmDatas = new ArrayList<List<Object>>();
      dmTable.put("data", dmDatas);

      // Add name column
      dmHeader.add("Probe");
      dmHeader.add("string");
      dmHeader.add("Count");
      dmHeader.add("number");
      dmHeader.add("Total");
      dmHeader.add("number");
      dmHeader.add("Min");
      dmHeader.add("number");
      dmHeader.add("Average");
      dmHeader.add("number");
      dmHeader.add("Max");
      dmHeader.add("number");

      ListIterator<Probe> iter = probeManager.getProbes().listIterator();
      while (iter.hasNext()) {
        int idx = iter.nextIndex();
        Probe probe = iter.next();

        List<Object> dmData = new ArrayList<Object>();
        dmDatas.add(dmData);

        dmData.add(probe.getDescription().replace("'", "\\'"));
        dmData.add(""); // No HRef

        SimpleStat stat = total.withoutChildrenValue[idx];
        addProbe(dmData, stat.getCount(),   null);
        addProbe(dmData, stat.getTotal(),   probe);
        addProbe(dmData, stat.getMin(),     probe);
        addProbe(dmData, stat.getAverage(), probe);
        addProbe(dmData, stat.getMax(),     probe);
      }
    }

    if (!total.children.isEmpty()) {
      Map<String, Object> dmTable = new HashMap<String, Object>();
      dmTables.add(dmTable);
      dmTable.put("name", "Probes with children");
      List<String> dmHeader = new ArrayList<String>();
      dmTable.put("header", dmHeader);
      List<List<Object>> dmDatas = new ArrayList<List<Object>>();
      dmTable.put("data", dmDatas);

      // Add name column
      dmHeader.add("Probe");
      dmHeader.add("string");
      dmHeader.add("Count");
      dmHeader.add("number");
      dmHeader.add("Total");
      dmHeader.add("number");
      dmHeader.add("Min");
      dmHeader.add("number");
      dmHeader.add("Average");
      dmHeader.add("number");
      dmHeader.add("Max");
      dmHeader.add("number");

      ListIterator<Probe> iter = probeManager.getProbes().listIterator();
      while (iter.hasNext()) {
        int idx = iter.nextIndex();
        Probe probe = iter.next();

        List<Object> dmData = new ArrayList<Object>();
        dmDatas.add(dmData);

        dmData.add(probe.getDescription().replace("'", "\\'"));
        dmData.add(""); // No HRef

        SimpleStat stat = total.withChildrenValue[idx];
        addProbe(dmData, stat.getCount(),   null);
        addProbe(dmData, stat.getTotal(),   probe);
        addProbe(dmData, stat.getMin(),     probe);
        addProbe(dmData, stat.getAverage(), probe);
        addProbe(dmData, stat.getMax(),     probe);
      }
    }

    //
    // Statistics
    //

    List<? extends Statistic> dummies =
        Ordering.usingToString().immutableSortedCopy(StatisticId.getDummies());

    //
    // Statistics without Children
    //

    for (Statistic dummy : dummies) {
      Map<String, Object> dmTable = new HashMap<String, Object>();
      dmTables.add(dmTable);
      dmTable.put("name", ("Statistics " + (total.children.isEmpty() ? "(" : "without Children ("))
          + dummy.getDescription() + ")");
      List<String> dmHeader = new ArrayList<String>();
      dmTable.put("header", dmHeader);
      List<List<Object>> dmDatas = new ArrayList<List<Object>>();
      dmTable.put("data", dmDatas);

      // Add name column
      dmHeader.add("Statistic");
      dmHeader.add("string");

      // Add value columns
      for (int idx = 0; idx < dummy.getDataView().getDataCount(); idx++) {
        dmHeader.add(dummy.getDescription(idx));
        dmHeader.add(dummy.getType(idx));
      }

      Map<StatisticId<? extends Statistic>, Statistic>[] rawStat = globalStatistics.get(total.type);
      if (rawStat != null) {
        List<StatisticId<? extends Statistic>> statIds = Ordering.usingToString()
            .immutableSortedCopy(rawStat[Children.WITHOUT.ordinal()].keySet());

        for (StatisticId<? extends Statistic> statId : statIds) {
          Statistic woStat = rawStat[Children.WITHOUT.ordinal()].get(statId);
          if (woStat != null) {
            if (StatisticId.getRegularClass(dummy.getClass()) == woStat.getClass()) {
              List<Object> dmData = new ArrayList<Object>();
              dmDatas.add(dmData);

              dmData.add(statId.getName().replace("'", "\\'"));
              dmData.add(getStatFileName(statId));
              for (int idx = 0; idx < dummy.getDataView().getDataCount(); idx++) {
                assert woStat.getHumanReadableValue(idx) != null;

                if (dummy.getType(idx).equals("string")) {
                  dmData.add(woStat.getHumanReadableValue(idx).replace("'", "\\'"));
                  dmData.add(getFileName(woStat.getValue(idx)));
                } else {
                  assert woStat.getValue(idx) != null;

                  dmData.add(woStat.getValue(idx));
                  dmData.add(woStat.getHumanReadableValue(idx));
                }
              }
            }
          }
        }
      }
    }

    //
    // Statistics with Children
    //

    if (!total.children.isEmpty()) {
      for (Statistic dummy : dummies) {
        Map<String, Object> dmStat = new HashMap<String, Object>();
        dmTables.add(dmStat);
        dmStat.put("name", "Statistics with Children (" + dummy.getDescription() + ")");
        List<String> dmHeader = new ArrayList<String>();
        dmStat.put("header", dmHeader);
        List<List<Object>> dmDatas = new ArrayList<List<Object>>();
        dmStat.put("data", dmDatas);

        // Add name column
        dmHeader.add("Statistic");
        dmHeader.add("string");

        // Add value columns
        for (int idx = 0; idx < dummy.getDataView().getDataCount(); idx++) {
          dmHeader.add(dummy.getDescription(idx));
          dmHeader.add(dummy.getType(idx));
        }

        Map<StatisticId<? extends Statistic>, Statistic>[] rawStat =
            globalStatistics.get(total.type);
        if (rawStat != null) {
          List<StatisticId<? extends Statistic>> statIds = Ordering.usingToString()
              .immutableSortedCopy(rawStat[Children.WITH.ordinal()].keySet());

          for (StatisticId<? extends Statistic> statId : statIds) {
            Statistic wStat = rawStat[Children.WITH.ordinal()].get(statId);
            if (wStat != null) {
              if (StatisticId.getRegularClass(dummy.getClass()) == wStat.getClass()) {
                List<Object> dmData = new ArrayList<Object>();
                dmDatas.add(dmData);

                dmData.add(statId.getName().replace("'", "\\'"));
                dmData.add(getStatFileName(statId));
                for (int idx = 0; idx < dummy.getDataView().getDataCount(); idx++) {
                  assert wStat.getHumanReadableValue(idx) != null;

                  if (dummy.getType(idx).equals("string")) {
                    dmData.add(wStat.getHumanReadableValue(idx).replace("'", "\\'"));
                    dmData.add(getFileName(wStat.getValue(idx)));
                  } else {
                    assert wStat.getValue(idx) != null;

                    dmData.add(wStat.getValue(idx));
                    dmData.add(wStat.getHumanReadableValue(idx));
                  }
                }
              }
            }
          }
        }
      }
    }

    //
    // Template
    //

    Configuration config = new Configuration();
    config.setClassForTemplateLoading(StatsTracerFtl.class, "templates/");
    config.setObjectWrapper(new DefaultObjectWrapper());
    config.setNumberFormat("0.######");

    File file = new File(dir.getFile(), getEventFileName(total.type));
    Writer writer;
    try {
      writer = new BufferedWriter(new FileWriter(file), 102400);
    } catch (IOException e) {
      logger.log(
          Level.SEVERE, "Unable to open tracer file '" + file.getAbsolutePath() + "'", e);
      return;
    }

    logger.log(Level.FINER, "Produce template ''{0}'' in file ''{1}''",
        new Object[] {template, file.getAbsolutePath()});

    /* Merge data-model with template */
    Template tmp;
    try {
      tmp = config.getTemplate(template);
      try {
        tmp.process(dmRoot, writer);
      } catch (IOException e) {
        logger.log(
            Level.SEVERE, "Unable to write tracer file '" + file.getAbsolutePath() + "'", e);
          return;
      } catch (TemplateException e) {
        logger.log(
            Level.SEVERE, "Unable to use template '" + template + "'", e);
      }
    } catch (IOException e) {
      logger.log(
          Level.SEVERE, "Unable to read template '" + template + "'", e);
    } finally {
      try {
        writer.flush();
        writer.close();
      } catch (IOException e) {
        logger.log(
            Level.SEVERE, "Unable to close tracer file '" + file.getAbsolutePath() + "'", e);
      }
    }
  }

  private void generateStatReport(@Nonnull StatisticId<? extends Statistic> id) {
    String template = "event.html.ftl";

    Map<String, Object> dmRoot = new HashMap<String, Object>();
    dmRoot.put("name", id.getName());

    //
    // Tables
    //

    List<Map<String, Object>> dmTables = new ArrayList<Map<String, Object>>();
    dmRoot.put("tables", dmTables);

    List<Total> totals = Ordering.usingToString().immutableSortedCopy(map.values());

    //
    // Statistics without Children
    //

    Map<String, Object> dmStat = new HashMap<String, Object>();
    dmTables.add(dmStat);
    dmStat.put("name", "Without Children");
    List<String> dmHeader = new ArrayList<String>();
    dmStat.put("header", dmHeader);
    List<List<Object>> dmDatas = new ArrayList<List<Object>>();
    dmStat.put("data", dmDatas);

    // Add name column
    dmHeader.add("Event");
    dmHeader.add("string");

    // Add value columns
    Statistic dummy = id.getDummyInstance();
    for (int idx = 0; idx < dummy.getDataView().getDataCount(); idx++) {
      dmHeader.add(dummy.getDescription(idx));
      dmHeader.add(dummy.getType(idx));
    }

    for (Total total : totals) {
      Map<StatisticId<? extends Statistic>, Statistic>[] rawStat = globalStatistics.get(total.type);
      if (rawStat != null) {
        Statistic woStat = rawStat[Children.WITHOUT.ordinal()].get(id);
        if (woStat != null) {
          List<Object> dmData = new ArrayList<Object>();
          dmDatas.add(dmData);

          dmData.add(total.type.getName().replace("'", "\\'"));
          dmData.add(getEventFileName(total.type));
          for (int idx = 0; idx < dummy.getDataView().getDataCount(); idx++) {
            dmData.add(woStat.getValue(idx));
            dmData.add(woStat.getHumanReadableValue(idx));
          }
        }
      }
    }

    dmStat = new HashMap<String, Object>();
    dmTables.add(dmStat);
    dmStat.put("name", "With Children");
    dmHeader = new ArrayList<String>();
    dmStat.put("header", dmHeader);
    dmDatas = new ArrayList<List<Object>>();
    dmStat.put("data", dmDatas);

    // Add name column
    dmHeader.add("Event");
    dmHeader.add("string");

    // Add value columns
    dummy = id.getDummyInstance();
    for (int idx = 0; idx < dummy.getDataView().getDataCount(); idx++) {
      dmHeader.add(dummy.getDescription(idx));
      dmHeader.add(dummy.getType(idx));
    }

    for (Total total : totals) {
      Map<StatisticId<? extends Statistic>, Statistic>[] rawStat = globalStatistics.get(total.type);
      if (rawStat != null) {
        Statistic wStat = rawStat[Children.WITH.ordinal()].get(id);
        if (wStat != null) {
          List<Object> dmData = new ArrayList<Object>();
          dmDatas.add(dmData);

          dmData.add(total.type.getName().replace("'", "\\'"));
          dmData.add(getEventFileName(total.type));
          for (int idx = 0; idx < dummy.getDataView().getDataCount(); idx++) {
            dmData.add(wStat.getValue(idx));
            dmData.add(wStat.getHumanReadableValue(idx));
          }
        }
      }
    }

    //
    // Template
    //

    Configuration config = new Configuration();
    config.setClassForTemplateLoading(StatsTracerFtl.class, "templates/");
    config.setObjectWrapper(new DefaultObjectWrapper());
    config.setNumberFormat("0.######");

    File file = new File(dir.getFile(), getStatFileName(id));
    Writer writer;
    try {
      writer = new BufferedWriter(new FileWriter(file), 102400);
    } catch (IOException e) {
      logger.log(
          Level.SEVERE, "Unable to open tracer file '" + file.getAbsolutePath() + "'", e);
      return;
    }

    logger.log(Level.FINER, "Produce template ''{0}'' in file ''{1}''",
        new Object[] {template, file.getAbsolutePath()});

    /* Merge data-model with template */
    Template tmp;
    try {
      tmp = config.getTemplate(template);
      try {
        tmp.process(dmRoot, writer);
      } catch (IOException e) {
        logger.log(
            Level.SEVERE, "Unable to write tracer file '" + file.getAbsolutePath() + "'", e);
          return;
      } catch (TemplateException e) {
        logger.log(
            Level.SEVERE, "Unable to use template '" + template + "'", e);
      }
    } catch (IOException e) {
      logger.log(
          Level.SEVERE, "Unable to read template '" + template + "'", e);
    } finally {
      try {
        writer.flush();
        writer.close();
      } catch (IOException e) {
        logger.log(
            Level.SEVERE, "Unable to close tracer file '" + file.getAbsolutePath() + "'", e);
      }
    }
  }

  private void generateConfigReport() {
    String template = "event.html.ftl";

    Map<String, Object> dmRoot = new HashMap<String, Object>();
    dmRoot.put("name", "Configuration");

    //
    // Tables
    //

    List<Map<String, Object>> dmTables = new ArrayList<Map<String, Object>>();
    dmRoot.put("tables", dmTables);

    //
    // Statistics without Children
    //

    Map<String, Object> dmStat = new HashMap<String, Object>();
    dmTables.add(dmStat);
    dmStat.put("name", "Application properties");
    List<String> dmHeader = new ArrayList<String>();
    dmStat.put("header", dmHeader);
    List<List<Object>> dmDatas = new ArrayList<List<Object>>();
    dmStat.put("data", dmDatas);

    // Add name column
    dmHeader.add("Name");
    dmHeader.add("string");
    dmHeader.add("Value");
    dmHeader.add("string");
    dmHeader.add("Description");
    dmHeader.add("string");

    // Print properties
    Config config = ThreadConfig.getConfig();
    for (PropertyId<?> property : config.getPropertyIds()) {
      List<Object> dmData = new ArrayList<Object>();
      dmDatas.add(dmData);

      dmData.add(property.getName().replace("'", "\\'"));
      dmData.add(""); // No HRef
      dmData.add(config.getAsString(property).replace("'", "\\'"));
      dmData.add(""); // No HRef
      dmData.add(property.getDescription().replace("'", "\\'"));
      dmData.add(""); // No HRef
    }

    //
    // Template
    //

    Configuration configuration = new Configuration();
    configuration.setClassForTemplateLoading(StatsTracerFtl.class, "templates/");
    configuration.setObjectWrapper(new DefaultObjectWrapper());
    configuration.setNumberFormat("0.######");

    File file = new File(dir.getFile(), getConfigFileName());
    Writer writer;
    try {
      writer = new BufferedWriter(new FileWriter(file), 102400);
    } catch (IOException e) {
      logger.log(
          Level.SEVERE, "Unable to open tracer file '" + file.getAbsolutePath() + "'", e);
      return;
    }

    logger.log(Level.FINER, "Produce template ''{0}'' in file ''{1}''",
        new Object[] {template, file.getAbsolutePath()});

    /* Merge data-model with template */
    Template tmp;
    try {
      tmp = configuration.getTemplate(template);
      try {
        tmp.process(dmRoot, writer);
      } catch (IOException e) {
        logger.log(
            Level.SEVERE, "Unable to write tracer file '" + file.getAbsolutePath() + "'", e);
          return;
      } catch (TemplateException e) {
        logger.log(
            Level.SEVERE, "Unable to use template '" + template + "'", e);
      }
    } catch (IOException e) {
      logger.log(
          Level.SEVERE, "Unable to read template '" + template + "'", e);
    } finally {
      try {
        writer.flush();
        writer.close();
      } catch (IOException e) {
        logger.log(
            Level.SEVERE, "Unable to close tracer file '" + file.getAbsolutePath() + "'", e);
      }
    }
  }

  private void addProbe(@Nonnull List<Object> model, double value, @CheckForNull Probe probe) {
    model.add(Long.valueOf((long) value));
    if (probe != null) {
      model.add(probe.formatValue((long) value));
    } else {
      model.add(Long.toString((long) value));
    }
  }

  @Override
  public void processEvent(@Nonnull Event event) {
    processEventWithOverhead(event, new Stack<EventType>());
  }

  @Nonnull
  private long[] processEventWithOverhead(@Nonnull Event event, @Nonnull Stack<EventType> stack) {
    EventType eventType = event.getType();

    // Accumulate overhead
    long[] overhead = new long[probeManager.getProbes().size()];
    stack.push(eventType);
    for (Event child : event.getChildren()) {
      if (child.getType() != TracerEventType.OVERHEAD) {
        long[] childOverhead = processEventWithOverhead(child, stack);

        for (int idx = 0; idx < overhead.length; idx++) {
          overhead[idx] += childOverhead[idx];
        }
      } else { // child.getType() == TracerEventType.OVERHEAD
        ListIterator<? extends Probe> iter = probeManager.getProbes().listIterator();
        while (iter.hasNext()) {
          int idx = iter.nextIndex();
          Probe probe = iter.next();
          overhead[idx] += child.getElapsedValue(probe);
        }
      }
    }
    stack.pop();

    // Remove overhead from current
    ListIterator<? extends Probe> iter = probeManager.getProbes().listIterator();
    while (iter.hasNext()) {
      int idx = iter.nextIndex();
      Probe probe = iter.next();
      event.adjustElapsedValue(probe, -overhead[idx]);
    }

    long[] withChildrenValue = new long[getProbeManager().getProbes().size()];
    long[] withoutChildrenValue = new long[getProbeManager().getProbes().size()];

    // Accumulate with children
    iter = probeManager.getProbes().listIterator();
    while (iter.hasNext()) {
      int idx = iter.nextIndex();
      Probe probe = iter.next();
      long duration = event.getElapsedValue(probe);

      withoutChildrenValue[idx] += duration;
      if (!stack.contains(eventType)) {
        withChildrenValue[idx] += duration;
      }
    }

    // Remove children
    for (Event child : event.getChildren()) {
      if (child.getType() != TracerEventType.OVERHEAD) {
        iter = probeManager.getProbes().listIterator();
        while (iter.hasNext()) {
          int idx = iter.nextIndex();
          Probe probe = iter.next();
          withoutChildrenValue[idx] -= child.getElapsedValue(probe);
        }
      }
    }

    // Get total object
    Total total = map.get(eventType);
    if (total == null) {
      total = new Total(eventType);
      map.put(eventType, total);
    }

    // Add sample
    iter = probeManager.getProbes().listIterator();
    while (iter.hasNext()) {
      int idx = iter.nextIndex();
      Probe probe = iter.next();
      total.withChildrenValue[idx].add(withChildrenValue[idx], null);
      total.withoutChildrenValue[idx].add(withoutChildrenValue[idx], null);
    }

    // Add children
    for (Event child : event.getChildren()) {
      if (child.getType() != TracerEventType.OVERHEAD) {
        total.children.add(child.getType());
      }
    }

    return overhead;
  }

  @Override
  public void flush() {
  }
}
