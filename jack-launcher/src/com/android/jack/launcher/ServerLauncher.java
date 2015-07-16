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
package com.android.jack.launcher;

import com.google.common.collect.Iterators;

import com.android.jack.server.api.v01.JackServer;
import com.android.jack.server.api.v01.LauncherHandle;
import com.android.jack.server.api.v01.NotInstalledException;
import com.android.jack.server.api.v01.ServerException;
import com.android.sched.util.FinalizerRunner;
import com.android.sched.util.UncomparableVersion;
import com.android.sched.util.Version;
import com.android.sched.util.stream.ByteStreamSucker;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A launcher for the server.
 */
public final class ServerLauncher {

  private static class ServerInfo {
    private final long id;

    @Nonnull
    private final Version version;

    public ServerInfo(long id, @Nonnull Version version) {
      this.id = id;
      this.version = version;
    }
  }

  private static class TaskRunner implements Runnable {
    private static class Task {
      @Nonnull
      private final String name;
      @Nonnull
      private final Runnable runnable;

      public Task(@Nonnull String name, @Nonnull Runnable runnable) {
        this.name = name;
        this.runnable = runnable;
      }
    }
    @Nonnull
    private final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
      try {
        while (true) {
          Task next = taskQueue.take();
          Thread thread = new Thread(next.runnable, next.name);
          thread.setDaemon(false);
          thread.start();
        }
      } catch (InterruptedException e) {
        logger.log(Level.FINE, "Thread " + Thread.currentThread().getName() + " interrupted");
        Thread.currentThread().interrupt();
      }
    }

    void executeTask(@Nonnull String name, @Nonnull Runnable task) {
      taskQueue.add(new Task(name, task));
    }
  }

  private class RunServerTask implements Runnable {
    @Nonnull
    private final ServerInfo serverInfo;

    @Nonnull
    private final Map<String, Object> parameters;

    public RunServerTask(@Nonnull ServerInfo serverInfo,
        @Nonnull Map<String, Object> parameters) {
      this.serverInfo = serverInfo;
      this.parameters = parameters;
    }

    @Override
    public void run() {
      File serverJar = getServerJar(serverDir, serverInfo.id);
      try {
        JackServer server = loadServer(serverJar);
        synchronized (currentServerLock) {
          assert currentServer == null;
          currentServer = server;
        }
        logger.log(Level.FINE, "Starting server " + serverInfo.version.getVerboseVersion() +
            " from " + serverJar.getName());
        server.setHandle(new ServerLauncherHandle());
        server.run(parameters);
        logger.log(Level.FINE, "Server " + serverInfo.version.getVerboseVersion() +
            " from " + serverJar.getName() + " ended");
      } catch (ServerException e) {
        logger.log(Level.SEVERE, "Server " + serverInfo.version.getVerboseVersion() +
            " from " + serverJar.getName() + " error: " + e.getMessage(), e);
      } catch (InterruptedException e) {
        logger.log(Level.FINE, "Server " + serverInfo.version.getVerboseVersion() +
            " from " + serverJar.getName() + " ended on interruption");
        Thread.currentThread().interrupt();
      } finally {
        decrementServerCount();
      }
    }
  }

  private static class Deleter implements Runnable {
    @Nonnull
    private final File[] toDelete;

    private Deleter(@Nonnull File[] toDelete) {
      this.toDelete = toDelete;
    }

    @Override
    public void run() {
      for (File file : toDelete) {
        if (!file.delete()) {
          logger.log(Level.WARNING, "Failed to delete file '" + file.getPath() + "'");
        } else {
          logger.log(Level.FINE, "Deleted file '" + file.getPath() + "'");
        }
      }
    }
  }

  private static class NotAServerJarFileName extends Exception {
    private static final long serialVersionUID = 1L;
  }

  private class ServerLauncherHandle implements LauncherHandle {

    @Override
    @Nonnull
    public File getServerDir() {
      return ServerLauncher.this.getServerDir();
    }

    @Override
    public void replaceServer(@Nonnull InputStream newServer,
        @Nonnull Map<String, Object> parameters, boolean forced)
        throws IOException, ServerException, NotInstalledException {
      ServerLauncher.this.replaceServer(newServer, parameters, forced);
    }

    @Override
    @Nonnull
    public ClassLoader getLauncherClassLoader() {
      return ServerLauncher.class.getClassLoader();
    }

    @Override
    @Nonnull
    public void deleteFilesOnGarbage(@Nonnull File[] filesToDelete, @Nonnull Object watched) {
      finalizer.registerFinalizer(new Deleter(filesToDelete), watched);
    }
  }

  @Nonnull
  private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());

  @Nonnull
  private static final String TMP_SUFFIX = ".tmp";

  private static final int ABORT_EXIT_CODE = 255;

  @Nonnull
  private static final String JACK_DIR = ".jack-home";

  @Nonnull
  private static final Pattern SERVER_JAR_PATTERN = Pattern.compile("server-(\\d+)\\.jar");

  @Nonnull
  private final FinalizerRunner finalizer = new FinalizerRunner("Launcher deleter");

  @Nonnull
  private final File serverDir;

  /**
   * Used to synchronize access to currentServerInfo and currentServer.
   */
  @Nonnull
  private final Object currentServerLock = new Object();
  @CheckForNull
  private ServerInfo currentServerInfo;
  @CheckForNull
  private JackServer currentServer;

  @Nonnegative
  private int serverCount;

  @Nonnull
  private final Object serverCountLock = new Object();

  @Nonnull
  private final TaskRunner taskRunner = new TaskRunner();

  public static void main(@Nonnull String[] args) {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        logger.log(Level.SEVERE, "Uncaught exception in thread '" + t.getName() + "'", e);
        abort("Internal error");
      }
    });
    String homeDir = System.getProperty("user.home");
    if (homeDir == null) {
      abort("Failed to locate home directory");
    }
    try {
      new ServerLauncher(new File(homeDir, JACK_DIR)).run();
    } catch (ServerException e) {
      abort("Failed to start server: " + e.getMessage());
    } catch (InterruptedException e) {
      logger.log(Level.FINE, "ServerLauncher was interrupted");
    }
  }

  public ServerLauncher(@Nonnull File serverDir) {
    this.serverDir = serverDir;
  }

  public void run() throws ServerException, InterruptedException {
    Thread taskThread = new Thread(taskRunner, "Task runner");
    taskThread.setDaemon(true);
    taskThread.start();

    File[] serverDirFiles = serverDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isFile();
      }
    });
    if (serverDirFiles == null) {
      throw new ServerException("Failed to list server install directory '" + serverDir + "'");
    }

    List<Long> serverIds = new LinkedList<Long>();
    for (File candidate : serverDirFiles) {
      try {
        serverIds.add(Long.valueOf(getServerId(candidate)));
      } catch (NotAServerJarFileName e) {
        // Not a server jar, is it a forgotten tmp?
        if (candidate.getName().endsWith(TMP_SUFFIX)) {
          deleteFile(candidate);
        }
      }
    }
    if (serverIds.size() == 0) {
      throw new ServerException("No installed Jack server");
    }
    Collections.sort(serverIds);
    Iterator<Long> iteratorAll = serverIds.iterator();
    Iterator<Long> toDeleteIterator = Iterators.limit(iteratorAll, serverIds.size() - 1);
    while (toDeleteIterator.hasNext()) {
      deleteFile(getServerJar(serverDir, toDeleteIterator.next().longValue()));
    }

    long serverId = iteratorAll.next().longValue();
    try {
      startInitialServer(serverDir, serverId);
    } catch (IOException e) {
      throw new ServerException("Failed to start installed server", e);
    }

    waitServers();
    taskThread.interrupt();
    finalizer.shutdown();
  }

  private void waitServers() throws InterruptedException {
    synchronized (serverCountLock) {
      while (serverCount > 0) {
        serverCountLock.wait();
      }
    }
    logger.log(Level.FINE, "Last server exited");
  }

  private void startInitialServer(@Nonnull File serverDir, @Nonnegative final long serverId)
      throws ServerException, IOException {
    // Don't inline in long living method, it could prevent the server to be garbaged because of
    // locals
    File serverJar = getServerJar(serverDir, serverId);
    final JackServer server = loadServer(serverJar);
    synchronized (currentServerLock) {
      currentServer = server;
      server.setHandle(new ServerLauncherHandle());

      currentServerInfo = new ServerInfo(serverId,
          new Version("jack-server", server.getClass().getClassLoader()));
    }

    incrementServerCount();
    taskRunner.executeTask("Server " + serverId, new Runnable() {
      @Override
      public void run() {
        logger.log(Level.FINE, "Starting server " + serverId);
        try {
          server.onSystemStart();
          server.run(new HashMap<String, Object>());
          logger.log(Level.FINE, "Server " + serverId + " ended");
        } catch (ServerException e) {
          logger.log(Level.SEVERE, "Server " + serverId + " Exception", e);
        } catch (InterruptedException e) {
          logger.log(Level.FINE, "Server " + serverId + " ended on interruption");
          Thread.currentThread().interrupt();
        } finally {
          decrementServerCount();
        }
      }
    });
  }

  private static File getServerJar(@Nonnull File serverDir, @Nonnegative long serverId) {
    return new File(serverDir, "server-" + serverId + ".jar");
  }

  @Nonnull
  private static JackServer loadServer(@Nonnull File jar) throws ServerException {
    ClassLoader classLoader;
    try {
      classLoader = new URLClassLoader(new URL[]{jar.toURI().toURL()},
          ServerLauncher.class.getClassLoader());
    } catch (IOException e) {
      throw new ServerException("Failed to open jar '" + jar.getPath() + "'", e);
    }
    ServiceLoader<JackServer> serviceLoader = ServiceLoader.load(JackServer.class, classLoader);
    try {
      JackServer server = serviceLoader.iterator().next();
      assert server.getClass().getClassLoader() == classLoader;
      return server;
    } catch (NoSuchElementException e) {
      throw new ServerException("Jar '" + jar.getPath() + "' does not define a server");
    } catch (ServiceConfigurationError e) {
      throw new ServerException(
          "Jar '" + jar.getPath() + "' does not define a valid server: " + e.getMessage(), e);
    }
  }

  private static void deleteFile(@Nonnull File file) {
    if (!file.delete()) {
      logger.log(Level.WARNING, "Failed to delete file '" + file.getPath() + "'");
    }
  }

  private static void abort(@Nonnull String message) {
    System.err.println(message);
    System.exit(ABORT_EXIT_CODE);
  }

  private static long getServerId(File serverJar) throws NotAServerJarFileName {
    Matcher matcher = SERVER_JAR_PATTERN.matcher(serverJar.getName());
    if (!matcher.matches()) {
      throw new NotAServerJarFileName();
    }
    return Long.parseLong(matcher.group(1));
  }

  @Nonnull
  public File getServerDir() {
    return serverDir;
  }

  private void replaceServer(@Nonnull InputStream jarIn, @Nonnull Map<String, Object> parameters,
      boolean forced)
      throws IOException, ServerException, NotInstalledException {
    FileOutputStream out = null;
    File tmpInstall = null;
    synchronized (currentServerLock) {
      try {
        tmpInstall = File.createTempFile("server.jar", TMP_SUFFIX, serverDir);
        out = new FileOutputStream(tmpInstall);
        new ByteStreamSucker(jarIn, out).suck();
        out.close();
        out = null;

        ClassLoader tmpLoader;
        try {
          tmpLoader = new URLClassLoader(new URL[]{tmpInstall.toURI().toURL()},
              ServerLauncher.class.getClassLoader());
        } catch (IOException e) {
          throw new ServerException("Failed to open jar '" + tmpInstall.getPath() + "'", e);
        }

        Version candidateVersion = new Version("jack-server", tmpLoader);
        assert currentServerInfo != null;
        Version currentVerion = currentServerInfo.version;

        if (!forced) {
          try {
            if (!candidateVersion.isNewerThan(currentVerion)) {
              if (candidateVersion.equals(currentVerion)) {
                logger.log(Level.INFO, "Server version "
                    + currentVerion.getVerboseVersion() + " was already installed");
                return;
              } else {
                throw new NotInstalledException("Not installing server "
                    + candidateVersion.getVerboseVersion()
                    + " since it is not newer than current server "
                    + currentServerInfo.version.getVerboseVersion());
              }
            }
          } catch (UncomparableVersion e) {
            if (!candidateVersion.isComparable()) {
              throw new NotInstalledException("Not installing server '"
                  + candidateVersion.getVerboseVersion() + "' without force request");
            }
            // else: current is experimental or eng, candidate is not, lets proceed
          }
        }

        assert currentServerInfo != null;
        long newServerId = currentServerInfo.id + 1;
        File newInstalledServer = getServerJar(serverDir, newServerId);
        if (!tmpInstall.renameTo(newInstalledServer)) {
          throw new IOException("Failed to rename '" + tmpInstall + "' to '" + newInstalledServer
              + "'");
        }
        tmpInstall = null;

        File replacedServerJar = getServerJar(serverDir, currentServerInfo.id);
        assert currentServer != null;
        finalizer.registerFinalizer(new Deleter(new File[]{replacedServerJar}),
            currentServer.getClass().getClassLoader());
        currentServerInfo = new ServerInfo(newServerId, candidateVersion);
      } finally {
        if (out != null) {
          try {
            out.close();
          } catch (IOException e) {
            logger.log(Level.WARNING, "Exception during close", e);
          }
        }
        if (tmpInstall != null) {
          if (!tmpInstall.delete()) {
            logger.log(Level.WARNING, "Failed to delete temp file '" + tmpInstall + "'");
          }
        }

        currentServer = null;
        assert currentServerInfo != null;
        logger.log(Level.FINE, "Starting server "  + currentServerInfo.version.getVerboseVersion());
        incrementServerCount();
        assert currentServerInfo != null;
        taskRunner.executeTask("Server " + currentServerInfo.id,
            new RunServerTask(currentServerInfo, parameters));
      }
    }
  }

  private void incrementServerCount() {
    synchronized (serverCountLock) {
      serverCount++;
    }
  }

  private void decrementServerCount() {
    synchronized (serverCountLock) {
      serverCount--;
      serverCountLock.notifyAll();
    }
  }

}
