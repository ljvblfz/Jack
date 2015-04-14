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

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.config.cli.TokenIterator;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.location.NoLocation;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.tracer.probe.MemoryBytesProbe;
import com.android.sched.util.log.tracer.probe.TimeNanosProbe;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Server controlling the number of Jack compilations that are executed simultaneously.
 */
public class JackSimpleServer {
  static {
    // It seems that loggers must be created from parents to children to have the loggers
    // correctly initialized. Thus load the initial configuration that define specific level
    // for some packages firstly. Otherwise the parent logger of JackSimpleServer is not created
    // before JackSimpleServer logger. Create the JackSimpleServer logger in first means that
    // it will not have com.android.jack.server as parent even if it is created after.
    LoggerFactory.loadLoggerConfiguration(JackSimpleServer.class, "/initial.logging.properties");
  }

  @Nonnull
  private static Logger logger = Logger.getLogger(JackSimpleServer.class.getName());

  private static int portService;
  private static int portAdmin;

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

      out.println("Pre-test stdout for '" + workingDir.getPath() + "' from " + portService);
      err.println("Pre-test stderr for '" + cmd + "' from " + portService);

      try {
        Thread.sleep(rnd.nextInt(3000));
      } catch (InterruptedException e) {
        // Doesn't matter
      }

      out.println("Post-test stdout for '" + workingDir.getPath() + "' from " + portService);
      err.println("Post-test stderr for '" + cmd + "' from " + portService);

      return rnd.nextInt(30);
    }

    @Override
    @Nonnull
    public String getVersion() {
      return "0-0";
    }
  };

  @Nonnull
  private static ServerTask service = new ServerTaskInsideVm();

  private static final int CMD_IDX_CMD = 0;
  private static final int CMD_IDX_OUT = 1;
  private static final int CMD_IDX_ERR = 2;
  private static final int CMD_IDX_CLI = 3;
  private static final int CMD_IDX_END = 4;

  private static final int CLI_IDX_PORTS   = 0;
  private static final int CLI_IDX_PORTA   = 1;
  private static final int CLI_IDX_COUNT   = 2;
  private static final int CLI_IDX_MAX     = 3;
  private static final int CLI_IDX_TIEMOUT = 4;
  private static final int CLI_IDX_END     = 5;

  @CheckForNull
  private static Connection serviceConnection;
  @CheckForNull
  private static Connection adminConnection;
  @CheckForNull
  private static Timer timer;
  @Nonnull
  private static Lock lock = new ReentrantLock();

  private static int timeout;

  private static int currentLocal = 0;
  private static long totalLocal = 0;
  private static int maxLocal = 0;

  private static int currentForward = 0;
  private static long totalForward = 0;
  private static int maxForward = 0;

  public static void main(String[] args) {
    if (args.length != CLI_IDX_END) {
      logger.log(Level.SEVERE,
          "Usage: <port-service> <port-admin> <server-count> <max-compile> <timeout-s>");
      abort();
    }

    portService = 0;
    try {
      portService = Integer.parseInt(args[CLI_IDX_PORTS]);
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Cannot parse port number '" + args[CLI_IDX_PORTS] + "'");
      abort();
    }

    portAdmin = 0;
    try {
      portAdmin = Integer.parseInt(args[CLI_IDX_PORTA]);
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Cannot parse port number '" + args[CLI_IDX_PORTA] + "'");
      abort();
    }

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

    InetSocketAddress serviceSocket = new InetSocketAddress("127.0.0.1", portService);
    InetSocketAddress adminSocket   = new InetSocketAddress("127.0.0.1", portAdmin);

    logger.log(Level.INFO, "Starting admin connection on " + adminSocket);
    try {
      JackRouter router = new JackRouter(0);
      router.addContainer("gc", new JackGc());
      router.addContainer("stat", new JackStat());
      router.addContainer("id", new JackId());
      router.addContainer("stop", new JackStop());

      ContainerSocketProcessor processor = new ContainerSocketProcessor(router, 1);
      adminConnection = new SocketConnection(processor);
      assert adminConnection != null;
      adminConnection.connect(adminSocket);
    } catch (IOException e) {
      if (e.getCause() instanceof BindException) {
        logger.log(Level.SEVERE, "Problem during service connection: " + e.getCause().getMessage());
      } else {
        logger.log(Level.SEVERE, "Problem during service connection ", e);
      }
      abort();
    }

    logger.log(Level.INFO, "Starting service connection server on " + serviceSocket);
    try {
      JackRouter jackV1Version = new JackRouter(2, Status.NOT_IMPLEMENTED);
      jackV1Version.addContainer(service.getVersion(), new JackRun());

      JackRouter protocolVersion =
          new JackRouter(1, new ErrorContainer(Status.NOT_IMPLEMENTED), new ErrorContainer(
              Status.BAD_REQUEST) {
            @Override
            public void handle(@Nonnull Request request, @Nonnull Response response) {
              logger.log(Level.WARNING,
                  "Jack version not available, try to shutdown server (jack-admin stop-server)");
              super.handle(request, response);
            }
          });
      protocolVersion.addContainer("1", jackV1Version);

      JackRouter router = new JackRouter(0);
      router.addContainer("jack", protocolVersion);

      ContainerSocketProcessor processor = new ContainerSocketProcessor(router, nbInstance);
      serviceConnection = new SocketConnection(processor);
      assert serviceConnection != null;
      serviceConnection.connect(serviceSocket);
      startTimer();
    } catch (IOException e) {
      if (e.getCause() instanceof BindException) {
        logger.log(Level.SEVERE, "Problem during service connection: " + e.getCause().getMessage());
      } else {
        logger.log(Level.SEVERE, "Problem during service connection ", e);
      }
      abort();
    }
  }

  private static class ErrorContainer implements Container {
    @Nonnull
    private final Status status;

    public ErrorContainer(@Nonnull Status status) {
      this.status = status;
    }

    @Override
    public void handle(@Nonnull Request request, @Nonnull Response response) {
      response.setStatus(status);
      try {
        response.close();
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Exception during close: ", e);
      }
    }
  }

  private static class JackRouter implements Container {
    @Nonnull
    private final Map<String, Container> registry = new HashMap<String, Container>();
    @Nonnegative
    private final int       index;
    @Nonnull
    private final Container notFound;
    @Nonnull
    private final Container noSegment;

    public JackRouter(@Nonnegative int index) {
      this(index, Status.NOT_FOUND);
    }

    public JackRouter(@Nonnegative int index, @Nonnull final Status error) {
      this(index, new ErrorContainer(error), new ErrorContainer(error));
    }

    public JackRouter(@Nonnegative int index, @Nonnull final Status notFound,
        @Nonnull final Status noSegment) {
      this(index, new ErrorContainer(notFound), new ErrorContainer(noSegment));
    }

    public JackRouter(@Nonnegative int index, @Nonnull Container notFound,
        @Nonnull Container noSegment) {
      this.index = index;
      this.notFound = notFound;
      this.noSegment = noSegment;
    }

    @Nonnull
    public JackRouter addContainer(@Nonnull String fragment, @Nonnull Container container) {
      registry.put(fragment, container);

      return this;
    }

    @Override
    public void handle(@Nonnull Request request, @Nonnull Response response) {
      String segments[] = request.getPath().getSegments();

      if (index >= segments.length) {
        logger.log(Level.WARNING, "Unknown request for missing segment #" + index + ": '"
            + request.getPath().getPath() + "'");
        noSegment.handle(request, response);
      }

      Container container = registry.get(segments[index]);
      if (container != null) {
        logger.log(Level.INFO, "Request for segment #" + index + ": '" + segments[index] + "'");
        container.handle(request, response);
      } else {
        logger.log(Level.WARNING, "Unknown request for segment #" + index + ": '" + segments[index]
            + "'");
        notFound.handle(request, response);
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

          logger.log(Level.INFO, "Check security");
          try {
            checkSecurity(command[CMD_IDX_CLI]);
          } catch (Throwable e) {
            logger.log(Level.SEVERE, e.getMessage());
            response.setStatus(Status.UNAUTHORIZED);
            return;
          }

          try {
            logger.log(Level.INFO, "Open standard output '" + command[CMD_IDX_OUT] + "'");
            try {
              out = new FifoStreamFile(command[CMD_IDX_OUT]).getPrintStream(10000);
            } catch (IOException | TimeoutException e) {
              logger.log(Level.SEVERE, e.getMessage());
              response.setStatus(Status.BAD_REQUEST);
              return;
            }

            logger.log(Level.INFO, "Open standard error '" + command[CMD_IDX_ERR] + "'");
            try {
              err = new FifoStreamFile(command[CMD_IDX_ERR]).getPrintStream(10000);
            } catch (IOException | TimeoutException e) {
              logger.log(Level.SEVERE, e.getMessage());
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
          }
        } finally {
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

  private static class JackId implements Container {
    @Override
    public void handle(@Nonnull Request request, @Nonnull Response response) {
      try {
        response.setStatus(Status.OK);
        PrintStream printer = response.getPrintStream();

        printer.println("server.version: 2");
        printer.println("service.name: " + service.getClass().getCanonicalName());
        printer.println("service.versions: " + service.getVersion());
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
      printer.println(suffix + "init: " + formatQuatity(usage.getInit()));
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

  private static class JackStop implements Container {
    @Override
    public void handle(@Nonnull Request request, @Nonnull Response response) {
      logger.log(Level.INFO, "Force shutdown");

      Thread thread = new Thread() {
        @Override
        public void run() {
          shutdown();
        }
      };
      thread.setName("jack-server-shutdown");
      thread.start();
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

  private static volatile PrintStream unblockOut = null;
  private static volatile InputStream unblockIn = null;

  private static void unblock(@Nonnull final String name) {
    logger.log(Level.INFO, "Trying to unblock '" + name + "'");

    // To unblock all processes blocked on the open of a fifo, open the fifo in read and open it in
    // write at the same time.

    Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          unblockIn = new InputStreamFile(name).getInputStream();
        } catch (IOException e) {
          // Best effort
        }
      }
    };
    thread.setName("jack-server-unblock");
    thread.start();

    try {
      unblockOut = new OutputStreamFile(name).getPrintStream();
    } catch (IOException e) {
      // Best effort
    }

    while (true) {
      try {
        thread.join();
        break;
      } catch (InterruptedException e1) {
        // If the thread does not finished, we are blocked anyway
      }
    }

    if (unblockOut != null) {
      unblockOut.close();
    }

    if (unblockIn != null) {
      try {
        unblockIn.close();
      } catch (IOException e) {
        // Best effort
      }
    }
  }

  private static class FifoStreamFile extends OutputStreamFile {
    @CheckForNull
    private volatile OutputStream tmp;

    public FifoStreamFile(@Nonnull String name) throws WrongPermissionException, NotFileException {
      super(name);
      // Check also read permission, writing without reader is useless
      checkPermissions(file, location, Permission.READ);
    }

    @Nonnull
    public synchronized OutputStream getOutputStream(@Nonnegative int timeout)
        throws TimeoutException {
      if (stream == null) {
        Thread thread = new Thread() {
          @Override
          public void run() {
            try {
              tmp = new FileOutputStream(file, isInAppendMode());
            } catch (FileNotFoundException e) {
              throw new ConcurrentIOException(e);
            }
          }
        };
        thread.setName("jack-server-open");
        thread.setDaemon(true);
        thread.start();

        try {
          thread.join(timeout);
        } catch (InterruptedException e) {
          // If interrupted, abort timeout
        }

        if (tmp == null) {
          // Try to unblock thread above
          try {
            // TODO(jack-team) The open can block again ...
            new InputStreamFile(getPath()).getInputStream().close();
          } catch (NotFileException | WrongPermissionException | NoSuchFileException e) {
            // Already check
            throw new ConcurrentIOException(e);
          } catch (IOException e) {
            // Error during close, nothing to do
          }

          throw new TimeoutException("Cannot open " + location.getDescription() + " for " + timeout
              + " ms");
        }

        stream = tmp;
      }

      assert stream != null;
      return stream;
    }

    @Nonnull
    public synchronized PrintStream getPrintStream(@Nonnegative int timeout)
        throws TimeoutException {
      if (printer == null) {
        printer = new PrintStream(getOutputStream(timeout));
      }

      return printer;
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
          shutdown();
        }
      }, timeout);
    } finally {
      lock.unlock();
    }
  }

  private static void shutdown() {
    cancelTimer();

    Connection conn = serviceConnection;
    assert conn != null;

    logger.log(Level.INFO, "Shutdowning service connection");
    logger.log(Level.INFO, "# max of concurrent compilations: " + maxLocal);
    logger.log(Level.INFO, "# total of compilations: " + totalLocal);
    logger.log(Level.INFO, "# max of concurrent forward compilations: " + maxForward);
    logger.log(Level.INFO, "# total of forward compilations: " + totalForward);
    try {
      conn.close();
      logger.log(Level.INFO, "Done");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Cannot shutdown the service connection: ", e);
    }

    conn = adminConnection;
    assert conn != null;

    logger.log(Level.INFO, "Shutdowning admin connection");
    try {
      conn.close();
      logger.log(Level.INFO, "Done");
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Cannot shutdown the asmin connection: ", e);
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

  private static final Set<PosixFilePermission> directoryRef = EnumSet.of(
      PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
      PosixFilePermission.OWNER_EXECUTE);
  private static final Set<PosixFilePermission> fifoRef = EnumSet.of(
      PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);

  private static void checkSecurity(@Nonnull String fifoCli) throws IOException {
    // Get parent directory
    java.nio.file.Path path = Paths.get(fifoCli).getParent();

    // Get current UserPrincipal
    assert path != null;
    UserPrincipal user;
    java.nio.file.Path tmp;
    try {
      tmp = Files.createTempFile(path, "jss-", "-check");
      user = Files.getOwner(tmp);
      Files.delete(tmp);
    } catch (IOException e) {
      throw new SecurityException("Cannot create/delete file in '" + path + "' to check security");
    }

    // Check parent directory
    UserPrincipal owner = Files.getOwner(path);
    Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);

    if (!owner.getName().equals(user.getName())) {
      throw new SecurityException("Directory '" + path + "' is not owned by '" + user.getName()
          + "' but by '" + owner.getName() + "'");
    }

    if (!permissions.equals(directoryRef)) {
      throw new SecurityException("Directory '" + path + "' must have permission "
          + PosixFilePermissions.toString(directoryRef) + " but have "
          + PosixFilePermissions.toString(permissions));
    }

    // Check fifo
    path = Paths.get(fifoCli);
    owner = Files.getOwner(path);
    permissions = Files.getPosixFilePermissions(path);

    if (!owner.getName().equals(user.getName())) {
      throw new SecurityException("Fifo '" + path + "' is not owned by '" + user.getName()
          + "' but by '" + owner.getName() + "'");
    }

    if (!permissions.equals(fifoRef)) {
      throw new SecurityException("Fifo '" + path + "' must have permission "
          + PosixFilePermissions.toString(fifoRef) + " but have "
          + PosixFilePermissions.toString(permissions));
    }
  }
}

