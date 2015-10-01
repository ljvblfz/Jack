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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.cache.Weigher;

import com.android.jack.api.JackProvider;
import com.android.jack.server.ServerLogConfiguration.ServerLogConfigurationException;
import com.android.jack.server.api.v01.LauncherHandle;
import com.android.jack.server.api.v01.ServerException;
import com.android.jack.server.router.AcceptContentTypeParameterRouter;
import com.android.jack.server.router.AcceptContentTypeRouter;
import com.android.jack.server.router.ContentTypeParameterRouter;
import com.android.jack.server.router.ContentTypeRouter;
import com.android.jack.server.router.ErrorContainer;
import com.android.jack.server.router.MethodRouter;
import com.android.jack.server.router.PartContentTypeParameterRouter;
import com.android.jack.server.router.PartContentTypeRouter;
import com.android.jack.server.router.PathRouter;
import com.android.jack.server.router.RootContainer;
import com.android.jack.server.tasks.GC;
import com.android.jack.server.tasks.GetJackVersions;
import com.android.jack.server.tasks.GetLauncherHome;
import com.android.jack.server.tasks.GetLauncherVersion;
import com.android.jack.server.tasks.GetServerVersion;
import com.android.jack.server.tasks.InstallJack;
import com.android.jack.server.tasks.InstallServer;
import com.android.jack.server.tasks.JackTask;
import com.android.jack.server.tasks.JillTask;
import com.android.jack.server.tasks.QueryJackVersion;
import com.android.jack.server.tasks.ReloadConfig;
import com.android.jack.server.tasks.SetLoggerParameters;
import com.android.jack.server.tasks.Stat;
import com.android.jack.server.tasks.Stop;
import com.android.jack.server.tasks.TestServerVersion;
import com.android.jack.server.type.CommandOutPrintStream;
import com.android.jack.server.type.ExactCodeVersionFinder;
import com.android.jack.server.type.TextPlain;
import com.android.sched.util.Version;
import com.android.sched.util.codec.IntCodec;
import com.android.sched.util.codec.LongCodec;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.findbugs.SuppressFBWarnings;

import org.simpleframework.http.ContentType;
import org.simpleframework.http.Method;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Server controlling the number of Jack compilations that are executed simultaneously.
 */
public class JackHttpServer implements HasVersion {

  /**
   * A program that can be run by this server as a service.
   */
  public static class Program<T> implements HasVersion {
    @Nonnull
    private final Version version;

    @Nonnull
    private final File jar;

    @Nonnull
    private Reference<T> loadedProgram;

    public Program(@Nonnull Version version, @Nonnull File jar, @CheckForNull T loadedProgram) {
      this.version = version;
      this.jar = jar;
      this.loadedProgram = new SoftReference<T>(loadedProgram);
    }

    @Override
    @Nonnull
    public Version getVersion() {
      return version;
    }

    @Nonnull
    public File getJar() {
      return jar;
    }

    @CheckForNull
    private T getLoadedProgram() {
      return loadedProgram.get();
    }

    private void setLoadedProgram(@CheckForNull T program) {
      loadedProgram = new SoftReference<>(program);
    }

  }

  /**
   * Thrown when attempting to start new task while server is closed or shutdown is in progress.
   */
  public static class ServerClosedException extends Exception {
    private static final long serialVersionUID = 1L;
  }

  private static class VersionKey implements HasVersion {

    @Nonnull
    private final Version version;

    public VersionKey(@Nonnull Version version) {
      this.version = version;
    }

    @Override
    public final boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof VersionKey) {
        VersionKey other = (VersionKey) obj;
        return version.getReleaseCode() == other.version.getReleaseCode()
            && version.getSubReleaseCode() == other.version.getSubReleaseCode();
      }
      return false;
    }

    @Override
    public final int hashCode() {
      return (version.getReleaseCode() * 7) ^ (version.getSubReleaseCode() * 17);
    }

    @Override
    @Nonnull
    public Version getVersion() {
      return version;
    }

    @Nonnull
    @Override
    public String toString() {
      return version.toString();
    }
  }

  @Nonnull
  private static final String JAR_SUFFIX = ".jar";

  @Nonnull
  private static final String DELETED_SUFFIX = ".deleted";

  private static final int TIMEOUT_DISABLED = -1;

  @Nonnegative
  private static final int MINIMAL_TIMEOUT = 60 * 60 * 24 * 7 * 2;

  @Nonnull
  private static final String DELETED_JAR_SUFFIX = JAR_SUFFIX + DELETED_SUFFIX;

  @Nonnull
  private static final String LOG_FILE_PATTERN = "logs/jack-server-%u-%g.log";

  private static final FileFilter JAR_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.isFile() && pathname.getName().endsWith(JAR_SUFFIX)
          && !new File(pathname.getPath() + DELETED_SUFFIX).exists();
    }
  };

  private static final FileFilter DELETED_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return pathname.isFile() && pathname.getName().endsWith(DELETED_JAR_SUFFIX);
    }
  };

  @Nonnull
  private static Logger logger = Logger.getLogger(JackHttpServer.class.getName());

  private int portService;

  private int portAdmin;

  private long maxJarSize;

  @Nonnull
  private final LauncherHandle launcherHandle;
  @Nonnull
  private final File serverDir;

  @CheckForNull
  private Connection serviceConnection;
  @CheckForNull
  private Connection adminConnection;
  @CheckForNull
  private Timer timer;
  @Nonnull
  private final Object lock = new Object();

  private int timeout;

  private int maxServices;

  @Nonnull
  private final ServerInfo serviceInfo = new ServerInfo();

  @Nonnull
  private final ServerInfo adminInfo = new ServerInfo();

  private boolean isAcceptingRequests = false;

  private Cache<VersionKey, Program<JackProvider>> installedJack = null;

  @CheckForNull
  private ServerSocketChannel adminChannel;

  @CheckForNull
  private ServerSocketChannel serviceChannel;

  @Nonnull
  private ServerLogConfiguration logConfiguration;

  // random does not need to be strong, it's just an help for debugging
  @SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
  JackHttpServer(@Nonnull LauncherHandle launcherHandle)
      throws IOException, ServerLogConfigurationException {
    this.launcherHandle = launcherHandle;
    serverDir = launcherHandle.getServerDir();

    logConfiguration = ServerLogConfiguration.setupLog(
        serverDir.getPath().replace(File.separatorChar, '/') + '/' + LOG_FILE_PATTERN);

    loadConfig();

    buildInstalledJackCache();

    loadInstalledJacks();
  }

  @Nonnull
  public ServerLogConfiguration getLogConfiguration() {
    return logConfiguration.clone();
  }

  public void setLogConfiguration(ServerLogConfiguration logConfiguration)
      throws IOException {
    logConfiguration.apply();
    this.logConfiguration = logConfiguration;
  }

  private void buildInstalledJackCache() {
    Cache<VersionKey, Program<JackProvider>> previousInstalledJack = installedJack;
    installedJack = CacheBuilder.newBuilder()
        .weigher(new Weigher<VersionKey, Program<JackProvider>>() {
          @Override
          public int weigh(VersionKey version, Program<JackProvider> program) {
            long length = program.getJar().length();
            return (int) Math.min(Integer.MAX_VALUE, length);
          }
        })
        .maximumWeight(maxJarSize == -1 ? Long.MAX_VALUE : maxJarSize)
        .removalListener(new RemovalListener<VersionKey, Program<JackProvider>>() {

          @Override
          public void onRemoval(
              @Nonnull RemovalNotification<VersionKey, Program<JackProvider>> notification) {
            Program<JackProvider> program = notification.getValue();
            JackProvider provider = program.getLoadedProgram();
            final File jar = program.getJar();
            if (provider != null) {
              logger.info("Queuing " + jar.getPath() + " for deletion");
              final File deleteMarker = new File(jar.getPath() + DELETED_SUFFIX);
              try {
                if (!deleteMarker.createNewFile()) {
                  throw new IOException("File already exists");
                }
              } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create delete file marker '" + deleteMarker
                    + "' aborting deletion by finalizer", e);
                return;
              }
              getLauncherHandle().deleteFilesOnGarbage(new File[]{deleteMarker, jar},
                  provider.getClass().getClassLoader());
            } else {
              logger.info("Deleting " + jar.getPath() + " immediatly");
              if (!jar.delete()) {
                logger.log(Level.SEVERE, "Failed to delete file '" + jar + "'");
              }
            }
          }
        })
        .concurrencyLevel(1)
        .build();
    if (previousInstalledJack != null) {
      installedJack.putAll(previousInstalledJack.asMap());
    }
  }

  @Nonnull
  public ServerSocketChannel getAdminChannel() {
    assert adminChannel != null;
    return adminChannel;
  }

  @Nonnull
  public ServerSocketChannel getServiceChannel() {
    assert serviceChannel != null;
    return serviceChannel;
  }

  public void addInstalledJack(@Nonnull Program<JackProvider> jack) {
    synchronized (installedJack) {
      installedJack.put(new VersionKey(jack.getVersion()), jack);
    }
    logger.log(Level.INFO, "New installed Jack " + jack.getVersion().getVerboseVersion() + " in "
        + jack.getJar().getPath());
  }

  private void loadInstalledJacks() throws IOException {
    File jackDir = new File(serverDir, "jack");
    new Directory(jackDir.getPath(), null, Existence.MAY_EXIST,
        Permission.READ | Permission.WRITE | Permission.EXECUTE, ChangePermission.NOCHANGE);
    File[] jars = jackDir.listFiles(JAR_FILTER);
    if (jars == null) {
      throw new IOException("Failed to list Jack installation directory '"
          + jackDir + "'");
    }
    for (File jackJar : jars) {
      try {
        JackProvider jackProvider = loadJack(jackJar);
        Version version = new Version("jack", jackProvider.getClass().getClassLoader());
        Program<JackProvider> jackProgram =
            new Program<JackProvider>(version, jackJar, jackProvider);
        installedJack.put(new VersionKey(version), jackProgram);
        logger.log(Level.INFO, "Jack " + version.getVerboseVersion()
            + " available in " + jackJar.getPath());
      } catch (UnsupportedProgramException | IOException e) {
        logger.log(Level.SEVERE, "Invalid installed jack file '" + jackJar
            + "'. Deleting.");
        if (!jackJar.delete()) {
          logger.log(Level.WARNING,
              "Failed to delete invalid installed jack file '" + jackJar + "'");
        }
      }
    }
  }

  public void reloadConfig() throws IOException, WrongPermissionException, NotFileException,
      ServerException {
    shutdownConnections();
    try {
      loadConfig();
      buildInstalledJackCache();
      start(new HashMap<String, Object>());
    } catch (IOException e) {
      shutdown();
      throw e;
    }
  }

  private void loadConfig() throws IOException,
      WrongPermissionException, NotFileException {
    File configFile = new File(serverDir, ConfigFile.CONFIG_FILE_NAME);
    ConfigFile config = new ConfigFile();
    config.loadIfPossible(configFile);

    logger.log(Level.INFO, "Starting jack server version: " + getVersion().getVerboseVersion());

    portService = config.getProperty(ConfigFile.SERVICE_PORT_PROPERTY, Integer.valueOf(8074),
        new IntCodec()).intValue();
    portAdmin = config.getProperty(ConfigFile.ADMIN_PORT_PROPERTY, Integer.valueOf(8075),
        new IntCodec()).intValue();
    timeout = config.getProperty(ConfigFile.TIME_OUT_PROPERTY, Integer.valueOf(7200),
        new IntCodec()).intValue();
    if (timeout < 0 && timeout != TIMEOUT_DISABLED) {
      logger.log(Level.WARNING,
          "Invalid config value for " + ConfigFile.TIME_OUT_PROPERTY + ": " + maxJarSize);
      timeout = TIMEOUT_DISABLED;
    } else {
      timeout = Math.max(timeout, MINIMAL_TIMEOUT);
    }
    maxJarSize = config.getProperty(
        ConfigFile.MAX_JAR_SIZE_PROPERTY, Long.valueOf(100 * 1024 * 1024),
        new LongCodec()).longValue();
    if (maxJarSize < -1) {
      logger.log(Level.WARNING,
          "Invalid config value for " + ConfigFile.MAX_JAR_SIZE_PROPERTY + ": " + maxJarSize);
      maxJarSize = -1;
    }
    maxServices = config.getProperty(ConfigFile.MAX_SERVICE_PROPERTY, Integer.valueOf(4),
        new IntCodec()).intValue();

    if (config.isModified() && config.getProperty(ConfigFile.CONFIG_VERSION_PROPERTY,
        Long.valueOf(-1), new LongCodec()).longValue() < ConfigFile.CURRENT_CONFIG_VERSION) {
      config.store(configFile);
    }
  }

  @Override
  @Nonnull
  public Version getVersion() {
    try {
      return new Version("jack-server", JackHttpServer.class.getClassLoader());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to read Jack-server version properties", e);
      throw new AssertionError();
    }
  }

  void start(@Nonnull Map<String, ?> parameters)
      throws ServerException {
    InetSocketAddress serviceAddress = new InetSocketAddress("127.0.0.1", portService);
    InetSocketAddress adminAddress   = new InetSocketAddress("127.0.0.1", portAdmin);


    logger.log(Level.INFO, "Starting service connection server on " + serviceAddress);
    try {

      MethodRouter router = new MethodRouter()
        .add(Method.POST,
          new ContentTypeRouter()
            .add("multipart/form-data",
              new AcceptContentTypeRouter()
                .add(CommandOutPrintStream.JACK_COMMAND_OUT_CONTENT_TYPE,
                  new AcceptContentTypeParameterRouter("version")
                    .add("1",
                      new PartContentTypeRouter("cli")
                        .add("plain/text",
                          new PartContentTypeRouter("pwd")
                            .add("plain/text",
                              new PartContentTypeRouter("version")
                                .add(ExactCodeVersionFinder.SELECT_EXACT_VERSION_CONTENT_TYPE,
                                  new PartContentTypeParameterRouter("version", "version")
                                    .add("1",
                                        new PathRouter()
                                          .add("/jack", new JackTask(this))
                                          .add("/jill", new JillTask(this))))))))));

      ContainerSocketProcessor processor =
          new ContainerSocketProcessor(new RootContainer(router), maxServices);
      SocketConnection connection = new SocketConnection(processor);
      serviceConnection = connection;
      serviceChannel = openSocket(serviceAddress, parameters.get(InstallServer.SERVICE_CHANNEL_PARAMETER));
      connection.connect(serviceChannel);
    } catch (IOException e) {
      if (e.getCause() instanceof BindException) {
        throw new ServerException("Problem during service connection: "
            + e.getCause().getMessage());
      } else {
        throw new ServerException("Problem during service connection ", e);
      }
    }

    logger.log(Level.INFO, "Starting admin connection on " + adminAddress);
    try {
      PathRouter router = new PathRouter()

        .add("/gc",
          new MethodRouter()
            .add(Method.POST, new GC(this)))

        .add("/stat",
          new MethodRouter()
            .add(Method.GET,
              new AcceptContentTypeRouter()
                .add(TextPlain.CONTENT_TYPE_NAME, new Stat(this))))

        .add("/server/stop",
            new MethodRouter()
              .add(Method.POST, new Stop(this)))

        .add("/server/reload",
          new MethodRouter()
            .add(Method.POST, new ReloadConfig(this)))

        .add("/jack",
          new MethodRouter()
            .add(Method.PUT,
              new ContentTypeRouter()
                .add("multipart/form-data",
                  new PartContentTypeRouter("force")
                    .add(TextPlain.CONTENT_TYPE_NAME,
                      new PartContentTypeRouter("jar")
                        .add("application/octet-stream", new InstallJack(this)))))
            .add(Method.HEAD,
              new ContentTypeRouter()
                .add(ExactCodeVersionFinder.SELECT_EXACT_VERSION_CONTENT_TYPE,
                  new ContentTypeParameterRouter("version")
                    .add("1", new QueryJackVersion(this))))
            .add(Method.GET,
              new AcceptContentTypeRouter()
                .add(TextPlain.CONTENT_TYPE_NAME, new GetJackVersions(this))))

        .add("/server",
          new MethodRouter()
            .add(Method.PUT,
              new ContentTypeRouter()
                .add("multipart/form-data",
                    new PartContentTypeRouter("force")
                      .add(TextPlain.CONTENT_TYPE_NAME,
                        new PartContentTypeRouter("jar")
                          .add("application/octet-stream", new InstallServer(this)))))
            .add(Method.HEAD,
              new ContentTypeRouter()
                .add(ExactCodeVersionFinder.SELECT_EXACT_VERSION_CONTENT_TYPE,
                  new ContentTypeParameterRouter("version")
                   .add("1", new TestServerVersion(this))))
            .add(Method.GET,
              new AcceptContentTypeRouter()
                .add(TextPlain.CONTENT_TYPE_NAME, new GetServerVersion(this))))

        .add("/launcher",
          new MethodRouter()
            .add(Method.PUT,
              new ErrorContainer(Status.BAD_REQUEST))
            .add(Method.GET,
              new AcceptContentTypeRouter()
                .add(TextPlain.CONTENT_TYPE_NAME, new GetLauncherVersion(this))))

        .add("/launcher/home",
          new MethodRouter()
            .add(Method.GET,
               new AcceptContentTypeRouter()
                 .add(TextPlain.CONTENT_TYPE_NAME, new GetLauncherHome(this))))

        .add("/launcher/log/level",
          new MethodRouter()
            .add(Method.PUT,
              new ContentTypeRouter()
                .add("multipart/form-data",
                   new PartContentTypeRouter("level")
                     .add(TextPlain.CONTENT_TYPE_NAME,
                       new PartContentTypeRouter("limit")
                         .add(TextPlain.CONTENT_TYPE_NAME,
                           new PartContentTypeRouter("count")
                             .add(TextPlain.CONTENT_TYPE_NAME, new SetLoggerParameters(this)))))));

      ContainerSocketProcessor processor =
          new ContainerSocketProcessor(new RootContainer(router), 1);
      SocketConnection connection = new SocketConnection(processor);
      adminConnection = connection;
      adminChannel =
          openSocket(adminAddress, parameters.get(InstallServer.ADMIN_CHANNEL_PARAMETER));
      connection.connect(adminChannel);
    } catch (IOException e) {
      if (e.getCause() instanceof BindException) {
        throw new ServerException("Problem during service connection: "
            + e.getCause().getMessage());
      } else {
        throw new ServerException("Problem during service connection ", e);
      }
    }

    startTimer();

    synchronized (lock) {
      isAcceptingRequests = true;
    }
  }

  @Nonnull
  private ServerSocketChannel openSocket(@Nonnull InetSocketAddress serviceAddress,
      @CheckForNull Object existingChannel) throws IOException,
      SocketException {
    if (existingChannel instanceof ServerSocketChannel) {
      return (ServerSocketChannel) existingChannel;
    } else {
      ServerSocketChannel channel = ServerSocketChannel.open();
      channel.configureBlocking(false);
      ServerSocket socket = channel.socket();
      socket.setReuseAddress(true);
      socket.bind(serviceAddress, 100);
      return channel;
    }
  }

  public void waitServerShutdown() throws InterruptedException {
    synchronized (lock) {
      while (isAcceptingRequests || serviceInfo.currentLocal > 0 || adminInfo.currentLocal > 0) {
        lock.wait();
      }
    }
  }

  @Nonnull
  public Program<JackProvider> selectJack(@Nonnull VersionFinder finder)
      throws NoSuchVersionException {
    synchronized (installedJack) {
      VersionKey selected = finder.select(installedJack.asMap().keySet());
      if (selected == null) {
        throw new NoSuchVersionException();
      }
      Program<JackProvider> program = installedJack.getIfPresent(selected);
      assert program != null;
      return program;
    }
  }

  @Nonnull
  public Collection<Program<JackProvider>> getInstalledJacks() {
    synchronized (installedJack) {
      return new ArrayList<>(installedJack.asMap().values());
    }
  }

  @Nonnull
  public JackProvider getProvider(@Nonnull Program<JackProvider> program)
      throws UnsupportedProgramException {
    synchronized (program) {
      JackProvider jackProvider = program.getLoadedProgram();
      if (jackProvider == null) {
        jackProvider = loadJack(program.getJar());
        program.setLoadedProgram(jackProvider);
      }
      return jackProvider;
    }
  }

  @Nonnull
  public VersionFinder parseVersionFinder(@Nonnull ContentType versionType,
      @Nonnull String versionString) throws TypeNotSupportedException, ParsingException {
    if (versionType.getType().equals(ExactCodeVersionFinder.SELECT_EXACT_VERSION_CONTENT_TYPE)
        && "1".equals(versionType.getParameter("version"))) {
      return ExactCodeVersionFinder.parse(versionString);
    } else {
      throw new TypeNotSupportedException("Unsupported version selection '" + versionString + "'");
    }
  }

  // We have no privilege restriction for now and there is no call back here from a Jack thread so
  // we should be fine keeping the code simple
  @SuppressFBWarnings("DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED")
  public JackProvider loadJack(File jackJar) throws UnsupportedProgramException {
    URLClassLoader jackLoader;
    try {
      jackLoader = new URLClassLoader(new URL[] {jackJar.toURI().toURL()},
          this.getClass().getClassLoader());
    } catch (MalformedURLException e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
      throw new AssertionError();
    }
    ServiceLoader<JackProvider> serviceLoader = ServiceLoader.load(JackProvider.class, jackLoader);
    JackProvider provider;
    try {
      provider = serviceLoader.iterator().next();
    } catch (NoSuchElementException | ServiceConfigurationError e) {
      logger.log(Level.SEVERE, "Failed to load jack from " + jackJar, e);
      throw new UnsupportedProgramException("Jack");
    }
    return provider;
  }

  private void startTimer() {
    synchronized (lock) {
      if (timeout == TIMEOUT_DISABLED) {
        return;
      }
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
          freeLoadedPrograms();
        }
      }, timeout * 1000L);
    }
  }

  public void shutdown() {
    synchronized (lock) {
      if (isAcceptingRequests) {
        shutdownConnections();

        isAcceptingRequests = false;
        lock.notifyAll();
      }
    }
  }

  public void shutdownServerOnly() {

    synchronized (lock) {
      if (isAcceptingRequests) {
        shutdownSimpleServer();

        isAcceptingRequests = false;
        lock.notifyAll();
      }
    }
  }

  // Even if its just a hint, this is a nice time for a gc.
  @SuppressFBWarnings("DM_GC")
  private void freeLoadedPrograms() {
    Collection<Program<JackProvider>> programs = getInstalledJacks();
    for (Program<JackProvider> program : programs) {
      synchronized (program) {
        program.setLoadedProgram(null);
      }
    }
    System.gc();
  }

  private void shutdownConnections() {
    shutdownSimpleServer();

    try {
      if (serviceChannel != null) {
        logger.log(Level.FINE, "Closing service server socket");
        serviceChannel.close();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Cannot close the service server socket: ", e);
    }
    serviceChannel = null;

    try {
      if (adminChannel != null) {
        logger.log(Level.FINE, "Closing admin server socket");
        adminChannel.close();
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Cannot close the admin server socket: ", e);
    }
    adminChannel = null;
  }

  private void shutdownSimpleServer() {
    synchronized (lock) {
      timeout = TIMEOUT_DISABLED;
      cancelTimer();
    }

    Connection conn = serviceConnection;
    if (conn != null) {
      logger.log(Level.INFO, "Shutdowning service connection");
      logger.log(Level.INFO, "# max of concurrent compilations: " + serviceInfo.maxLocal);
      logger.log(Level.INFO, "# total of compilations: " + serviceInfo.totalLocal);
      logger.log(Level.INFO, "# max of concurrent forward compilations: " + serviceInfo.maxForward);
      logger.log(Level.INFO, "# total of forward compilations: " + serviceInfo.totalForward);
      try {
        conn.close();
        logger.log(Level.INFO, "Done");
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Cannot shutdown the service connection: ", e);
      }
    }

    conn = adminConnection;
    if (conn != null) {
      logger.log(Level.INFO, "Shutdowning admin connection");
      try {
        conn.close();
        logger.log(Level.INFO, "Done");
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Cannot shutdown the admin connection: ", e);
      }
    }
  }

  private void cancelTimer() {
    synchronized (lock) {
      if (timer != null) {
        logger.log(Level.INFO, "Cancel timer");

        timer.cancel();
        timer.purge();
        timer = null;
      }
    }
  }

  public long startingServiceTask() throws ServerClosedException {
    return startingTask(serviceInfo);
  }

  public void endingServiceTask() {
    endingTask(serviceInfo);
  }

  public long startingAdministrativeTask() throws ServerClosedException {
    return startingTask(adminInfo);
  }

  public void endingAdministrativeTask() {
    endingTask(adminInfo);
  }

  private long startingTask(@Nonnull ServerInfo info) throws ServerClosedException{
    long id;

    synchronized (lock) {
      if (!isAcceptingRequests) {
        throw new ServerClosedException();
      }
      id = info.totalLocal;
      info.totalLocal++;
      if (info.currentLocal == 0) {
        cancelTimer();
      }

      info.currentLocal++;
      if (info.currentLocal > info.maxLocal) {
        info.maxLocal = info.currentLocal;
      }

    }
    return id;
  }

  private void endingTask(@Nonnull ServerInfo info) {
    synchronized (lock) {
      info.currentLocal--;
      if (info.currentLocal == 0) {
        startTimer();
      }
      lock.notifyAll();
    }
  }

  @Nonnull
  public File getServerDir() {
    return serverDir;
  }

  @Nonnull
  public LauncherHandle getLauncherHandle() {
    return launcherHandle;
  }

  static void cleanUp(@Nonnull LauncherHandle launcherHandle) throws IOException {
    File serverDir = launcherHandle.getServerDir();
    File jackDir = new File(serverDir, "jack");
    new Directory(jackDir.getPath(), null, Existence.MAY_EXIST,
        Permission.READ | Permission.WRITE | Permission.EXECUTE, ChangePermission.OWNER);
    File[] deletedFiles = jackDir.listFiles(DELETED_FILTER);
    if (deletedFiles == null) {
      throw new IOException("Failed to list Jack installation directory '"
          + jackDir + "'");
    }

    for (File deleteMarker : deletedFiles) {
      String path = deleteMarker.getPath();
      File marked = new File(path.substring(0, path.length() - DELETED_SUFFIX.length()));
      if (!marked.delete()) {
        logger.log(Level.SEVERE, "Failed to delete file '" + marked + "'");
      } else if (!deleteMarker.delete()) {
        logger.log(Level.SEVERE, "Failed to delete file '" + deleteMarker + "'");
      }
    }

  }

  @Nonnull
  public ServerInfo getServiceStat() {
    synchronized (lock) {
      return serviceInfo.clone();
    }
  }

  public void uninstallJack(@Nonnull Program<JackProvider> existingJack) {
    installedJack.invalidate(existingJack.getVersion());
  }

}
