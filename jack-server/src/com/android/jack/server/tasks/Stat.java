/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.server.tasks;

import com.google.common.base.Joiner;

import com.android.jack.server.JackHttpServer;
import com.android.jack.server.ServerInfo;
import com.android.jack.server.type.TextPlain;
import com.android.sched.util.log.tracer.probe.MemoryBytesProbe;
import com.android.sched.util.log.tracer.probe.TimeNanosProbe;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Administrative task: Provide some statistics.
 */
public class Stat extends SynchronousAdministrativeTask {

  @Nonnull
  private static Logger logger = Logger.getLogger(Stat.class.getName());

  public Stat(@Nonnull JackHttpServer jackServer) {
    super(jackServer);
  }

  @Override
  protected void handle(long taskId, @Nonnull Request request, @Nonnull Response response) {
    try {
      response.setContentType(TextPlain.CONTENT_TYPE_NAME + "; Charset="
          + TextPlain.getPreferredTextPlainCharset(request).name());
      response.setStatus(Status.OK);
      PrintStream printer = response.getPrintStream();

      long time = System.currentTimeMillis();
      Date date = new Date(time);
      printer.println("date: " + time + " [" + date + "]");

      try {
        ServerInfo stat = jackServer.getServiceStat();
        printer.println("server.compilation: " + stat.getTotalLocal());
        printer.println("server.compilation.max: " + stat.getMaxLocal());
        printer.println("server.compilation.current: " + stat.getCurrentLocal());
        printer.println("server.forward: " + stat.getTotalForward());
        printer.println("server.forward.max: " + stat.getMaxForward());
        printer.println("server.forward.current: " + stat.getCurrentForward());

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        printer.println("os.arch: " + os.getArch());
        printer.println("os.proc.nb: " + Integer.valueOf(os.getAvailableProcessors()));
        printer.println("os.name: " + os.getName());
        printer.println("os.version: " + os.getVersion());

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        printer.println("vm.name: " + runtime.getVmName());
        printer.println("vm.vendor: " + runtime.getVmVendor());
        printer.println("vm.version: " + runtime.getVmVersion());
        printer.println("vm_options: "
            + Joiner.on(' ').skipNulls().join(runtime.getInputArguments()));
        printer.println("vm.memory.max: " + formatQuatity(Runtime.getRuntime().maxMemory()));
        printer.println("vm.memory.free: " + formatQuatity(Runtime.getRuntime().freeMemory()));
        printer.println("vm.memory.total: " + formatQuatity(Runtime.getRuntime().totalMemory()));

        try {
          CompilationMXBean compilation = ManagementFactory.getCompilationMXBean();
          printer.println("vm.jit.time: "
              + formatDuration(compilation.getTotalCompilationTime(), TimeUnit.MILLISECONDS));
        } catch (UnsupportedOperationException e) {
          // Do the best
        }

        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
          String suffix = "vm.collector." + tranformString(gc.getName()) + ".";
          printer.println(suffix + "time: "
              + formatDuration(gc.getCollectionTime(), TimeUnit.MILLISECONDS));
          printer.println(suffix + "count: " + gc.getCollectionCount());
        }

        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
          String suffix = "vm.pool." + tranformString(pool.getName()) + ".";
          printer.println(suffix + "type: " + pool.getType().name());

          printMemoryUsage(printer, suffix + "collection.", pool.getCollectionUsage());
          try {
            printer.println(suffix + "collection.threshold: "
                + formatQuatity(pool.getCollectionUsageThreshold()));
          } catch (UnsupportedOperationException e) {
            // Best effort
          }

          try {
            printer.println(suffix + "collection.threshold.count: "
                + pool.getCollectionUsageThresholdCount());
          } catch (UnsupportedOperationException e) {
            // Best effort
          }

          printMemoryUsage(printer, suffix + "peak.", pool.getPeakUsage());

          printMemoryUsage(printer, suffix + "usage.", pool.getUsage());
          try {
            printer.println(suffix + "usage.threshold: "
                + formatQuatity(pool.getUsageThreshold()));
          } catch (UnsupportedOperationException e) {
            // Best effort
          }
          try {
            printer.println(suffix + "usage.threshold.count: " + pool.getUsageThresholdCount());
          } catch (UnsupportedOperationException e) {
            // Best effort
          }
        }

        try {
          printer.println("host.name: " + InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e1) {
          // Best effort
        }

        Method method;
        try {
          method = os.getClass().getMethod("getCommittedVirtualMemorySize");
          method.setAccessible(true);
          printer.println("os.memory.virtual.committed: "
              + formatQuatity(((Long) method.invoke(os)).longValue()));
        } catch (Throwable t) {
          // Best effort
        }

        try {
          method = os.getClass().getMethod("getTotalPhysicalMemorySize");
          method.setAccessible(true);
          printer.println("os.memory.physical.total: "
              + formatQuatity(((Long) method.invoke(os)).longValue()));
        } catch (Throwable t) {
          // Best effort
        }

        try {
          method = os.getClass().getMethod("getFreePhysicalMemorySize");
          method.setAccessible(true);
          printer.println("os.memory.physical.free: "
              + formatQuatity(((Long) method.invoke(os)).longValue()));
        } catch (Throwable t) {
          // Best effort
        }

        try {
          method = os.getClass().getMethod("getTotalSwapSpaceSize");
          method.setAccessible(true);
          printer.println("os.memory.swap.total: "
              + formatQuatity(((Long) method.invoke(os)).longValue()));
        } catch (Throwable t) {
          // Best effort
        }

        try {
          method = os.getClass().getMethod("getFreeSwapSpaceSize");
          method.setAccessible(true);
          printer.println("os.memory.swap.free: "
              + formatQuatity(((Long) method.invoke(os)).longValue()));
        } catch (Throwable t) {
          // Best effort
        }

        try {
          method = os.getClass().getMethod("getOpenFileDescriptorCount");
          method.setAccessible(true);
          printer.println("os.fd.open: " + ((Long) method.invoke(os)).longValue());
        } catch (Throwable t) {
          // Best effort
        }


        try {
          method = os.getClass().getMethod("getProcessCpuLoad");
          method.setAccessible(true);
          printer.println("os.process.cpu.load: " + ((Double) method.invoke(os)).doubleValue());
        } catch (Throwable t) {
          // Best effort
        }

        try {
          method = os.getClass().getMethod("getProcessCpuTime");
          method.setAccessible(true);
          printer.println("os.process.cpu.time: "
              + formatDuration(((Long) method.invoke(os)).longValue(), TimeUnit.NANOSECONDS));
        } catch (Throwable t) {
          // Best effort
        }

        try {
          method = os.getClass().getMethod("getSystemCpuLoad");
          method.setAccessible(true);
          printer.println("os.system.cpu.load: " + ((Double) method.invoke(os)).doubleValue());
        } catch (Throwable t) {
          // Best effort
        }
      } catch (Throwable e) {
        logger.log(Level.SEVERE, "Unexpected exception", e);
        response.setStatus(Status.INTERNAL_SERVER_ERROR);
      }
    } catch (UnsupportedEncodingException e) {
      logger.log(Level.SEVERE, "Unsupported charset", e);
      response.setStatus(Status.NOT_ACCEPTABLE);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Exception during IO", e);
      response.setStatus(Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Nonnull
  private static String tranformString(@Nonnull String string) {
    return string.toLowerCase().replace(' ', '-');
  }

  @Nonnull
  private static String formatQuatity(@Nonnull long quantity) {
    String str = Long.toString(quantity);

    str += " [";
    str += MemoryBytesProbe.formatBytes(quantity);
    str += "]";

    return str;
  }

  @Nonnull
  private static String formatDuration(@Nonnull long duration, @Nonnull TimeUnit unit) {
    String str = Long.toString(duration);

    str += " [";
    str += TimeNanosProbe.formatDuration(unit.toNanos(duration));
    str += "]";

    return str;
  }

  private static void printMemoryUsage(@Nonnull PrintStream printer, @Nonnull String suffix,
      @CheckForNull MemoryUsage usage) {
    if (usage != null) {
      printer.println(suffix + "commited: " + formatQuatity(usage.getCommitted()));
      printer.println(suffix + "init: " + formatQuatity(usage.getInit()));
      printer.println(suffix + "max: " + formatQuatity(usage.getMax()));
      printer.println(suffix + "used: " + formatQuatity(usage.getUsed()));
    }
  }
}