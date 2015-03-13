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
import com.android.sched.util.file.CannotSetPermissionException;
import com.android.sched.util.file.FileOrDirectory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.NoLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
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
        Thread.sleep(rnd.nextInt(30000));
      } catch (InterruptedException e) {
        // Doesn't matter
      }

      out.println("Post-test stdout for '" + workingDir.getPath() + "'");
      err.println("Post-test stderr for '" + cmd + "'");

      return rnd.nextInt(30);
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
  private static final int CLI_IDX_END     = 3;

  @CheckForNull
  private static File fifo;
  @CheckForNull
  private static LineNumberReader in;

  private static int timeout;

  @Nonnull
  private static AtomicInteger nbMax = new AtomicInteger(0);
  @Nonnull
  private static AtomicLong nbCurrent = new AtomicLong(0);

  public static void main(String[] args) throws InterruptedException {
    if (args.length != CLI_IDX_END) {
      logger.log(Level.SEVERE, "Usage: <max-compile> <timeout-s> <path-fifo>");
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
    assert fifo != null;
    if (fifo.canWrite()) {
      logger.log(Level.WARNING, "Already running, aborting");
      abort();
    }

    Runtime.getRuntime().addShutdownHook(new Thread(){
      @Override
      public void run() {
        cancelTimer();
        shutdownFifo();
      }
    });

    try {
      AbstractStreamFile.check(fifo, new FileLocation(fifo));
    } catch (IOException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      abort();
    }

    startFifo();
    try {
      in = new LineNumberReader(new FileReader(fifo));
    } catch (FileNotFoundException e) {
      throw new AssertionError(e);
    }

    assert fifo != null;
    logger.log(Level.INFO, "Starting server on '" + fifo.getPath() + "'");
    ExecutorService executor = Executors.newFixedThreadPool(nbInstance);
    for (int i = 0; i < nbInstance; i++) {
      logger.log(Level.INFO, "Launching task #" + i);
      executor.execute(new Task());
    }

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.HOURS);

    logger.log(Level.INFO, "Shutdown server");
    logger.log(Level.INFO, "# service runs " + nbCurrent.get());
  }

  /**
   * STOPSHIP
   */
  public static class Task implements Runnable {
    @Override
    public void run() {
      while (true) {
        String line;

        try {
          line = getLine();
          logger.log(Level.INFO, "Read command '" + line + "'");
        } catch (IOException e) {
          logger.log(Level.INFO, "Shutdown task");
          return;
        }

        String[] command = line.split(" ");

        if (command[CMD_IDX_CMD].equals("-")) {
          cancelTimer();
          shutdownFifo();
          continue;
        }

        if (!command[CMD_IDX_CMD].equals("+")) {
          logger.log(Level.SEVERE, "Command error '" + line + "'");
          continue;
        }

        if (command.length != CMD_IDX_END) {
          logger.log(Level.SEVERE, "Command format error '" + line + "'");
          continue;
        }

        logger.log(Level.INFO, "Open standard output '" + command[CMD_IDX_OUT] + "'");
        PrintStream out = null;
        try {
          out = new OutputStreamFile(command[CMD_IDX_OUT]).getPrintStream();
        } catch (IOException e) {
          logger.log(Level.SEVERE, e.getMessage(), e);
          continue;
        }

        logger.log(Level.INFO, "Open standard error '" + command[CMD_IDX_ERR] + "'");
        PrintStream err = null;
        try {
          err = new OutputStreamFile(command[CMD_IDX_ERR]).getPrintStream();
        } catch (IOException e) {
          logger.log(Level.SEVERE, e.getMessage(), e);
          continue;
        }

        logger.log(Level.INFO, "Parse command line");
        TokenIterator args = new TokenIterator(new NoLocation(), "@" + command[CMD_IDX_CLI]);
        args.allowFileReferenceInFile();

        if (!args.hasNext()) {
          logger.log(Level.WARNING, "Cli format error");
          continue;
        }
        String workingDir;
        try {
          workingDir = args.next();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Cli format error");
          continue;
        }

        if (nbMax.getAndIncrement() == 0) {
          cancelTimer();
        }

        int code;
        try {
          logger.log(Level.INFO, "Run service");
          nbCurrent.incrementAndGet();
          code = service.run(out, err, new File(workingDir), args);
        } finally {
          if (nbMax.decrementAndGet() == 0) {
            startTimer();
          }
        }

        assert out != null;
        out.close();
        assert err != null;
        err.close();

        try {
          logger.log(Level.INFO, "Open exit fifo  '" + command[CMD_IDX_EXIT] + "'");
          PrintStream exit = new OutputStreamFile(command[CMD_IDX_EXIT]).getPrintStream();
          logger.log(Level.INFO, "Write exit code '" + code + "'");
          exit.println(code);
          exit.close();
        } catch (IOException e) {
          logger.log(Level.SEVERE, e.getMessage(), e);
          continue;
        }

      }
    }
  }

  @Nonnull
  private static Object lockRead = new Object();

  @Nonnull
  public static String getLine() throws IOException {
    synchronized (lockRead) {
      assert in != null;
      String str = in.readLine();
      while (str == null) {
        try {
          in.close();
        } catch (IOException e1) {
          // Best effort
        }

        in = new LineNumberReader(new FileReader(fifo));
        assert in != null;
        str = in.readLine();
      }

      return str;
    }
  }

  private static void startFifo() {
    logger.log(Level.FINE, "Start FIFO");

    try {
      assert fifo != null;
      FileOrDirectory.setPermissions(fifo, new FileLocation(fifo),
          Permission.READ | Permission.WRITE, ChangePermission.OWNER);
    } catch (CannotSetPermissionException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      abort();
    }
  }

  private static void shutdownFifo() {
    OutputStream out = null;

    logger.log(Level.FINE, "Shutdown FIFO");

    try {
      out = new FileOutputStream(fifo);
    } catch (FileNotFoundException e) {
      // Best effort
    }

    try {
      assert fifo != null;
      FileOrDirectory.unsetPermissions(fifo, new FileLocation(fifo), Permission.READ
          | Permission.WRITE, ChangePermission.OWNER);
    } catch (CannotSetPermissionException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      abort();
    }

    if (out != null) {
      try {
        out.close();
      } catch (IOException e) {
        // Best effort
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

      logger.log(Level.INFO, "Start timer");

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
        logger.log(Level.INFO, "Cancel timer");

        timer.cancel();
        timer.purge();
        timer = null;
      }
    }
  }
}
