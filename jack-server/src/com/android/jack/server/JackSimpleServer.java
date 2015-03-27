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

package com.android.jack.server;

import com.google.common.base.Joiner;

import com.android.sched.util.config.cli.TokenIterator;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.location.NoLocation;
import com.android.sched.util.log.tracer.probe.MemoryBytesProbe;
import com.android.sched.util.log.tracer.probe.TimeNanosProbe;

import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.http.parse.PathParser;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Server controlling the number of Jack compilations that are executed simultaneously.
 */
public class JackSimpleServer {
  private static int port;

  @Nonnull
  private static final ServerTask serviceTest = new ServerTask() {
    @Nonnull
    private final Random rnd = new Random();

    @Override
    public int run(@Nonnull PrintStream out, @Nonnull PrintStream err, @Nonnull File workingDir,
        @Nonnull TokenIterator args) {
      String cmd = null;
      try {
        args.hasNext();
        cmd = args.next();
      } catch (Throwable e) {
        e.printStackTrace();
      }

      out.println("Pre-test stdout for '" + workingDir.getPath() + "' from " + port);
      err.println("Pre-test stderr for '" + cmd + "' from " + port);

      try {
        Thread.sleep(rnd.nextInt(3000));
      } catch (InterruptedException e) {
        // Doesn't matter
      }

      out.println("Post-test stdout for '" + workingDir.getPath() + "' from " + port);
      err.println("Post-test stderr for '" + cmd  + "' from " + port);

      return rnd.nextInt(30);
    }
  };

  @Nonnull
  private static ServerTask service = new ServerTaskInsideVm();

  @Nonnull
  private static Logger logger = Logger.getLogger(JackSimpleServer.class.getSimpleName());

  private static final int CMD_IDX_CMD = 0;
  private static final int CMD_IDX_OUT = 1;
  private static final int CMD_IDX_ERR = 2;
  private static final int CMD_IDX_CLI = 3;
  private static final int CMD_IDX_END = 4;

  private static final int CLI_IDX_PORT    = 0;
  private static final int CLI_IDX_COUNT   = 1;
  private static final int CLI_IDX_MAX     = 2;
  private static final int CLI_IDX_TIEMOUT = 3;
  private static final int CLI_IDX_END     = 4;

  @CheckForNull
  private static Connection connection;
  @CheckForNull
  private static Timer timer;
  @Nonnull
  private static Lock lock = new ReentrantLock();

  private static int  timeout;

  private static int  currentLocal = 0;
  private static long totalLocal = 0;
  private static int  maxLocal = 0;

  private static int  currentForward = 0;
  private static long totalForward = 0;
  private static int  maxForward = 0;

  public static void main(String[] args) {
    if (args.length != CLI_IDX_END) {
      logger.log(Level.SEVERE,
          "Usage: <port-nb> <server-count> <max-compile> <timeout-s>");
      abort();
    }

    port = 0;
    try {
      port = Integer.parseInt(args[CLI_IDX_PORT]);
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Cannot parse port number '" + args[CLI_IDX_PORT] + "'");
      abort();
    }

    logger = Logger.getLogger(JackSimpleServer.class.getSimpleName()  + "." + port);

    int count = 0;
    try {
      count = Integer.parseInt(args[CLI_IDX_COUNT]);
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Cannot parse server count '" + args[CLI_IDX_COUNT] + "'");
      abort();
    }

    int nbInstance = 0;
    try {
      nbInstance = Integer.parseInt(args[CLI_IDX_MAX]);
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Cannot parse instance count '" + args[CLI_IDX_MAX] + "'");
      abort();
    }

    try {
      timeout = Integer.parseInt(args[CLI_IDX_TIEMOUT]) * 1000;
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Cannot parse timeout '" + args[CLI_IDX_TIEMOUT] + "'");
      abort();
    }

    InetSocketAddress socket = new InetSocketAddress("127.0.0.1", port);

    logger.log(Level.INFO, "Starting simple server on " + socket);
    try {
      JackRouter router = new JackRouter();
      router.addContainer(new PathParser("/jack"), new JackRun());
      router.addContainer(new PathParser("/gc"), new JackGc());
      router.addContainer(new PathParser("/stat"), new JackStat());

      ContainerSocketProcessor processor =
          new ContainerSocketProcessor(router, nbInstance);
      connection = new SocketConnection(processor);
      assert connection != null;
      connection.connect(socket);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Problem during connection ", e);
      abort();
    }
  }

  private static class JackRouter implements Container {
    @Nonnull
    private final Map<String, Container> registry = new HashMap<String, Container>();
    @Nonnull
    private final Container primary;

    public JackRouter() {
      primary = new Container() {
        @Override
        public void handle(@Nonnull Request request, @Nonnull Response response) {
          logger.log(Level.INFO, "Unknown request for '" + request.getPath().getPath() + "'");
          response.setStatus(Status.NOT_FOUND);
        }
      };
    }

    public JackRouter(@Nonnull Container primary) {
      this.primary = primary;
    }

    public void addContainer(@Nonnull Path path, @Nonnull Container container) {
      registry.put(path.getPath(), container);
    }

    @Override
    public void handle(@Nonnull Request request, @Nonnull Response response) {
      String normalizedPath = request.getPath().getPath();

      logger.log(Level.INFO, "Route request from '" + normalizedPath + "'");

      Container container = registry.get(normalizedPath);
      if (container != null) {
        container.handle(request, response);
      } else {
        primary.handle(request, response);
      }
    }
  }

  private static class JackRun implements Container {
    @Override
    public void handle(@Nonnull Request request, @Nonnull Response response) {
      try {
        String line;

        try {
          line = request.getContent();
        } catch (IOException e1) {
          logger.log(Level.SEVERE, "Command read command");
          response.setStatus(Status.BAD_REQUEST);
          return;
        }

        if (line == null) {
          logger.log(Level.SEVERE, "Command error: nothing to read");
          response.setStatus(Status.BAD_REQUEST);
          return;
        }

        String[] command = line.split(" ");

        if (!command[CMD_IDX_CMD].equals("+")) {
          logger.log(Level.SEVERE, "Command error '" + line + "'");
          response.setStatus(Status.BAD_REQUEST);
          return;
        }

        logger.log(Level.INFO, "Read command '" + line + "'");

        PrintStream out = null;
        PrintStream err = null;

        long id;

        lock.lock();
        try {
          id = totalLocal;
          totalLocal++;
          if (currentLocal == 0) {
            cancelTimer();
          }
          currentLocal++;
          logger.log(Level.INFO, "Number of concurrent compilations: " + currentLocal);
          if (currentLocal > maxLocal) {
            maxLocal = currentLocal;
          }
        } finally {
          lock.unlock();
        }

        try {
          if (command.length != CMD_IDX_END) {
            logger.log(Level.SEVERE, "Command format error '" + line + "'");
            response.setStatus(Status.BAD_REQUEST);
            return;
          }

          logger.log(Level.INFO, "Open standard output '" + command[CMD_IDX_OUT] + "'");
          try {
            out = new OutputStreamFile(command[CMD_IDX_OUT]).getPrintStream();
          } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            response.setStatus(Status.BAD_REQUEST);
            return;
          }

          logger.log(Level.INFO, "Open standard error '" + command[CMD_IDX_ERR] + "'");
          try {
            err = new OutputStreamFile(command[CMD_IDX_ERR]).getPrintStream();
          } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            response.setStatus(Status.BAD_REQUEST);
            return;
          }

          logger.log(Level.INFO, "Parse command line");
          TokenIterator args = new TokenIterator(new NoLocation(), "@" + command[CMD_IDX_CLI]);
          args.allowFileReferenceInFile();
          if (!args.hasNext()) {
            logger.log(Level.SEVERE, "Cli format error");
            response.setStatus(Status.BAD_REQUEST);
            return;
          }

          String workingDir;
          try {
            workingDir = args.next();
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Cli format error");
            response.setStatus(Status.BAD_REQUEST);
            return;
          }

          int code = -1;
          long start = System.currentTimeMillis();
          try {
            logger.log(Level.INFO, "Run Compilation #" + id);
            start = System.currentTimeMillis();
            code = service.run(out, err, new File(workingDir), args);
          } finally {
            long stop = System.currentTimeMillis();
            logger.log(Level.INFO, "Compilation #" + id + " return exit code " + code);
            logger.log(Level.INFO, "Compilation #" + id + " run in " + (stop - start) + " ms");

            response.setStatus(Status.OK);

            PrintStream printer;
            try {
              printer = response.getPrintStream();
              printer.println(code);
              printer.close();
            } catch (IOException e) {
              logger.log(Level.SEVERE, "Problem to send exit code for compilation #" + id);
              response.setStatus(Status.BAD_REQUEST);
              return;
            }
          }
        } finally {
          if (out != null) {
            out.close();
          } else {
            unblock(command[CMD_IDX_OUT]);
          }

          if (err != null) {
            err.close();
          } else {
            unblock(command[CMD_IDX_ERR]);
          }

          lock.lock();
          try {
            currentLocal--;
            if (currentLocal == 0) {
              startTimer();
            }
          } finally {
            lock.unlock();
          }
        }
      } finally {
        try {
          response.close();
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Exception during close: ", e);
        }
      }

      return;
    }
  }

  private static class JackStat implements Container {
    @Override
    public void handle(@Nonnull Request request, @Nonnull Response response) {
      try {
        response.setStatus(Status.OK);
        PrintStream printer = response.getPrintStream();

        long time = System.currentTimeMillis();
        Date date = new Date(time);
        printer.println("date: " + time + " [" + date + "]");

        try {
          lock.lock();
          try {
            printer.println("server.compilation: " + totalLocal);
            printer.println("server.compilation.max: " + maxLocal);
            printer.println("server.compilation.current: " + currentLocal);
            printer.println("server.forward: " + totalForward);
            printer.println("server.forward.max: " + maxForward);
            printer.println("server.forward.current: " + currentForward);
          } finally {
            lock.unlock();
          }

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
          logger.log(Level.SEVERE, "Unexpected exception: ", e);
        }
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Exception during IO: ", e);
      } finally {
        try {
          response.close();
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Exception during close: ", e);
        }
      }
    }
  }

  static void printMemoryUsage(@Nonnull PrintStream printer, @Nonnull String suffix,
      @CheckForNull MemoryUsage usage) {
    if (usage != null) {
      printer.println(suffix + "commited: " + formatQuatity(usage.getCommitted()));
      printer.println(suffix + "itnit: " + formatQuatity(usage.getInit()));
      printer.println(suffix + "max: " + formatQuatity(usage.getMax()));
      printer.println(suffix + "used: " + formatQuatity(usage.getUsed()));
    }
  }

  @Nonnull
  static String formatDuration(@Nonnull long duration, @Nonnull TimeUnit unit) {
    String str = Long.toString(duration);

    str += " [";
    str += TimeNanosProbe.formatDuration(unit.toNanos(duration));
    str += "]";

    return str;
  }

  @Nonnull
  static String formatQuatity(@Nonnull long quantity) {
    String str = Long.toString(quantity);

    str += " [";
    str += MemoryBytesProbe.formatBytes(quantity);
    str += "]";

    return str;
  }

  @Nonnull
  static String tranformString(@Nonnull String string) {
    return string.toLowerCase().replace(' ', '-');
  }

  @SuppressFBWarnings("DM_GC")
  private static class JackGc implements Container {
    @Override
    public void handle(@Nonnull Request request, @Nonnull Response response) {
      logger.log(Level.INFO, "Force GC");
      System.gc();
      response.setStatus(Status.OK);
      try {
        response.close();
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Exception during close: ", e);
      }
    }
  }

  private static void abort() {
    logger.log(Level.SEVERE, "Abort sever");
    System.exit(1);
  }

  private static void unblock(@Nonnull String name) {
    logger.log(Level.INFO, "Trying to unblock '" + name + "'");
    PrintStream out = null;
    try {
      out = new OutputStreamFile(name).getPrintStream();
    } catch (IOException e) {
      // Best effort
    }

    if (out != null) {
      out.close();
    }
  }

  private static void startTimer() {
    lock.lock();
    try {
      if (timer != null) {
        cancelTimer();
      }

      logger.log(Level.INFO, "Start timer");

      timer = new Timer("jack-server-timeout");
      assert timer != null;
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          cancelTimer();

          Connection conn = connection;
          assert conn != null;

          logger.log(Level.INFO, "Shutdowning server");
          logger.log(Level.INFO, "# max of concurrent compilations: " + maxLocal);
          logger.log(Level.INFO, "# total of compilations: " + totalLocal);
          logger.log(Level.INFO, "# max of concurrent forward compilations: " + maxForward);
          logger.log(Level.INFO, "# total of forward compilations: " + totalForward);
          try {
            conn.close();
            logger.log(Level.INFO, "Done");
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot shutdown the server: ", e);
          }
        }
      }, timeout);
    } finally {
      lock.unlock();
    }
  }

  private static void cancelTimer() {
    lock.lock();
    try {
      if (timer != null) {
        logger.log(Level.INFO, "Cancel timer");

        timer.cancel();
        timer.purge();
        timer = null;
      }
    } finally {
      lock.unlock();
    }
  }
}
