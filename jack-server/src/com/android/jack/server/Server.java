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

import com.android.sched.util.config.cli.TokenIterator;
import com.android.sched.util.file.AbstractStreamFile;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.NoLocation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Server controlling the number of Jack compilations that are executed simultaneously.
 */
public class Server {
  @Nonnull
  private static ServerTask serviceTest = new ServerTask() {
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

      out.println("Pre-test stdout for '" + workingDir.getPath() + "'");
      err.println("Pre-test stderr for '" + cmd + "'");

      try {
        Thread.sleep(rnd.nextInt(3000));
      } catch (InterruptedException e) {
        // Doesn't matter
      }

      out.println("Post-test stdout for '" + workingDir.getPath() + "'");
      err.println("Post-test stderr for '" + cmd + "'");

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

  @Nonnull
  private static Logger logger = Logger.getLogger(Server.class.getSimpleName());

  private static final int CMD_IDX_CMD  = 0;
  private static final int CMD_IDX_OUT  = 1;
  private static final int CMD_IDX_ERR  = 2;
  private static final int CMD_IDX_EXIT = 3;
  private static final int CMD_IDX_CLI  = 4;
  private static final int CMD_IDX_END  = 5;

  private static final int CLI_IDX_MAX     = 0;
  private static final int CLI_IDX_TIEMOUT = 1;
  private static final int CLI_IDX_FIFO    = 2;
  private static final int CLI_IDX_LOCK    = 3;
  private static final int CLI_IDX_END     = 4;

  @CheckForNull
  private static File fifo;
  @CheckForNull
  private static File lock;
  @CheckForNull
  private static BufferedReader in;

  private static int timeout;

  @Nonnull
  private static AtomicInteger nbMax = new AtomicInteger(0);
  @Nonnull
  private static AtomicLong nbCurrent = new AtomicLong(0);

  public static void main(String[] args) throws InterruptedException {
    if (args.length != CLI_IDX_END) {
      logger.log(Level.SEVERE, "Usage: <max-compile> <timeout-s> <path-fifo> <path-lock>");
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

    fifo = new File(args[CLI_IDX_FIFO]);
    lock = new File(args[CLI_IDX_LOCK]);


    try {
      AbstractStreamFile.check(fifo, new FileLocation(fifo));
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      abort();
    }

    Runtime.getRuntime().addShutdownHook(new Thread(){
      @Override
      public void run() {
        cancelTimer();
        shutdownFifo();

        if (!lock.delete()) {
          logger.log(Level.SEVERE, "Can not remove lock file '" + lock.getPath() + "'");
        }
      }
    });

    startFifo();
    startTimer();
    try {
      in = new BufferedReader(new FileReader(fifo));
    } catch (FileNotFoundException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      abort();
    }

    assert fifo != null;
    logger.log(Level.INFO, "Starting server on '" + fifo.getPath() + "'");
    ExecutorService executor = Executors.newFixedThreadPool(nbInstance);
    for (int i = 0; i < nbInstance; i++) {
      logger.log(Level.FINE, "Launching task #" + i);
      executor.execute(new Task());
    }

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);

    logger.log(Level.INFO, "Shutdown server");
    logger.log(Level.INFO, "# service runs " + nbCurrent.get());
  }

  /**
   * {@link Runnable} task launched by the server that will be in charge to call {@link ServerTask}
   * implementation that will launch a Jack compilation either in the same VM, either in a spawn VM.
   */
  public static class Task implements Runnable {
    @Override
    public void run() {
      while (true) {
        String line;

        try {
          line = getLine();
          logger.log(Level.FINE, "Read command '" + line + "'");
        } catch (IOException e) {
          logger.log(Level.FINE, "Shutdown task");
          return;
        }

        String[] command = line.split(" ");

        if (command[CMD_IDX_CMD].equals("=")) {
          continue;
        }

        if (command[CMD_IDX_CMD].equals("-")) {
          cancelTimer();
          shutdownFifo();
          continue;
        }

        if (!command[CMD_IDX_CMD].equals("+")) {
          logger.log(Level.SEVERE, "Command error '" + line + "'");
          continue;
        }

        PrintStream out = null;
        PrintStream err = null;
        PrintStream exit = null;

        try {
          if (command.length != CMD_IDX_END) {
            logger.log(Level.SEVERE, "Command format error '" + line + "'");
            continue;
          }

          logger.log(Level.FINE, "Open standard output '" + command[CMD_IDX_OUT] + "'");
          try {
            out = new OutputStreamFile(command[CMD_IDX_OUT]).getPrintStream();
          } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            continue;
          }

          logger.log(Level.FINE, "Open standard error '" + command[CMD_IDX_ERR] + "'");
          try {
            err = new OutputStreamFile(command[CMD_IDX_ERR]).getPrintStream();
          } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            continue;
          }

          logger.log(Level.FINE, "Open exit fifo  '" + command[CMD_IDX_EXIT] + "'");
          try {
            exit = new OutputStreamFile(command[CMD_IDX_EXIT]).getPrintStream();
          } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            continue;
          }

          logger.log(Level.FINE, "Parse command line");
          TokenIterator args = new TokenIterator(new NoLocation(), "@" + command[CMD_IDX_CLI]);
          args.allowFileReferenceInFile();
          if (!args.hasNext()) {
            logger.log(Level.SEVERE, "Cli format error");
            continue;
          }

          String workingDir;
          try {
            workingDir = args.next();
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Cli format error");
            continue;
          }

          if (nbMax.getAndIncrement() == 0) {
            cancelTimer();
          }

          int code = -1;
          try {
            logger.log(Level.FINE, "Run service");
            nbCurrent.incrementAndGet();
            code = service.run(out, err, new File(workingDir), args);
          } finally {
            if (nbMax.decrementAndGet() == 0) {
              startTimer();
            }

            logger.log(Level.FINE, "Write exit code '" + code + "'");
            assert exit != null;
            exit.println(code);
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

          if (exit != null) {
            exit.close();
          } else {
            unblock(command[CMD_IDX_EXIT]);
          }
        }
      }
    }
  }

  @Nonnull
  private static Object lockRead = new Object();

  private static volatile boolean stop = false;

  @Nonnull
  public static String getLine() throws IOException {
    synchronized (lockRead) {
      if  (in == null) {
        throw new IOException();
      }

      assert in != null;
      String str = in.readLine();
      while (str == null) {
        try {
          in.close();
        } catch (IOException e1) {
          // Best effort
        }
        in = null;

        if (stop) {
          throw new IOException();
        }

        in = new BufferedReader(new FileReader(fifo));
        assert in != null;
        str = in.readLine();
      }

      return str;
    }
  }

  private static void startFifo() {
    logger.log(Level.FINE, "Start FIFO");
  }

  private static void shutdownFifo() {
    logger.log(Level.FINE, "Shutdown FIFO");

    stop = true;

    Unblocker unblocker = new Unblocker();
    unblocker.setName("Unblocker");
    unblocker.setDaemon(true);
    unblocker.start();
  }

  private static class Unblocker extends Thread {
    @Override
    public void run() {
      PrintStream out = null;

      while (true) {
        try {
          out = new PrintStream(fifo);
        } catch (FileNotFoundException e) {
          // Best effort
        }

        if (out != null) {
          out.close();
        }

        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          // Best effort here
        }
      }
    }
  }

  private static void abort() {
    System.exit(1);
  }

  @CheckForNull
  private static Timer timer;

  @Nonnull
  private static Object lockTimer = new Object();

  private static void startTimer() {
    synchronized (lockTimer) {
      if (timer != null) {
        cancelTimer();
      }

      logger.log(Level.FINE, "Start timer");

      timer = new Timer("jack-server-timeout");
      assert timer != null;
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          shutdownFifo();
          cancelTimer();
        }
      }, timeout);
    }
  }

  private static void cancelTimer() {
    synchronized (lockTimer) {
      if (timer != null) {
        logger.log(Level.FINE, "Cancel timer");

        timer.cancel();
        timer.purge();
        timer = null;
      }
    }
  }

  private static void unblock(@Nonnull String name) {
    logger.log(Level.FINE, "Trying to unblock '" + name + "'");
    PrintStream out = null;
    try {
      out = new OutputStreamFile(name).getPrintStream();
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }

    if (out != null) {
      out.close();
    }
  }
}
