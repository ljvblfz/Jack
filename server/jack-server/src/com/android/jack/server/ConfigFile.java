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

import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.IntCodec;
import com.android.sched.util.codec.ListCodec;
import com.android.sched.util.codec.LongCodec;
import com.android.sched.util.codec.PairCodec;
import com.android.sched.util.codec.PairCodec.Pair;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.log.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

class ConfigFile extends Properties {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  static final int CURRENT_CONFIG_VERSION = 4;

  /**
   * Disabled value for delays returned by public methods of this class.
   */
  @Nonnegative
  static final int TIME_DISABLED_VALUE = Integer.MAX_VALUE;

  /**
   * Disabled value for delays specified in the config file.
   */
  private static final int CONFIG_TIME_DISABLED_VALUE = -1;

  @Nonnull
  private static final Charset CONFIG_CHARSET = StandardCharsets.UTF_8;
  private static final long serialVersionUID = 1L;

  @Nonnull
  private static final String ADMIN_PORT_PROPERTY = "jack.server.admin.port";
  @Nonnull
  private static final String SERVICE_PORT_PROPERTY = "jack.server.service.port";
  @Nonnull
  private static final String MAX_JAR_SIZE_PROPERTY = "jack.server.max-jars-size";
  @Nonnull
  private static final String MAX_SERVICE_PROPERTY = "jack.server.max-service";
  @Nonnull
  private static final String MAX_SERVICE_BY_MEM_PROPERTY = "jack.server.max-service.by-mem";
  @Nonnull
  private static final String TIME_OUT_PROPERTY = "jack.server.time-out";
  @Nonnull
  private static final String IDLE_PROPERTY = "jack.server.idle";
  @Nonnull
  private static final String DEEP_IDLE_PROPERTY = "jack.server.deep-idle";
  @Nonnull
  private static final String SHUTDOWN_PROPERTY = "jack.server.shutdown";
  @Nonnull
  private static final String CONFIG_VERSION_PROPERTY = "jack.server.config.version";
  @Nonnull
  private static final String CONFIG_FILE_NAME = "config.properties";

  @Nonnull
  private static final List<Pair<Integer, Long>> DEFAULT_MAX_SERVICES_BY_MEM = new ArrayList<>();

  static {
    DEFAULT_MAX_SERVICES_BY_MEM
    .add(new Pair<Integer, Long>(Integer.valueOf(1), Long.valueOf(2L * 1024 * 1024 * 1024)));
    DEFAULT_MAX_SERVICES_BY_MEM
    .add(new Pair<Integer, Long>(Integer.valueOf(2), Long.valueOf(3L * 1024 * 1024 * 1024)));
    DEFAULT_MAX_SERVICES_BY_MEM
    .add(new Pair<Integer, Long>(Integer.valueOf(3), Long.valueOf(4L * 1024 * 1024 * 1024)));
  }

  private boolean modified = false;

  @Nonnull
  private final File storageFile;

  public ConfigFile(@Nonnull File serverDir) throws IOException {
    storageFile = new File(serverDir, CONFIG_FILE_NAME);
    if (!storageFile.exists()) {
      if (!(storageFile.createNewFile())) {
        throw new IOException("Failed to create '" + storageFile.getPath() + "'");
      }
      if (!(storageFile.setExecutable(false, false)
         && storageFile.setWritable(false, false)
         && storageFile.setReadable(false, false)
         && storageFile.setWritable(true, true)
         && storageFile.setReadable(true, true))) {
        throw new IOException("Failed to set permissions of '" + storageFile.getPath() + "'");
      }
    }
    loadIfPossible(storageFile);
  }

  @Nonnull
  public File getStorageFile() {
    return storageFile;
  }

  @Override
  public Object setProperty(@Nonnull String key, @Nonnull String value) {
    modified = true;
    return super.setProperty(key, value);
  }

  private void loadIfPossible(@Nonnull File configFile) throws IOException {
    InputStreamFile streamFile;
    try {
      streamFile = new InputStreamFile(configFile.getPath());
      Reader configIn =
          new InputStreamReader(streamFile.getInputStream(), CONFIG_CHARSET);
      try {
        load(configIn);
      } finally {
        try {
          configIn.close();
        } catch (IOException e) {
          // ignore
        }
      }
    } catch (NotFileException | WrongPermissionException | NoSuchFileException e) {
      logger.log(Level.WARNING, "Not loading configuration from file: " + e.getMessage());
    }
    modified = false;
  }

  public void store() throws WrongPermissionException, NotFileException, IOException,
      CannotCreateFileException, CannotChangePermissionException {
    setProperty(ConfigFile.CONFIG_VERSION_PROPERTY,
        Integer.toString(CURRENT_CONFIG_VERSION));  // FINDBUGS

    new OutputStreamFile(storageFile.getPath(), /* hooks = */ null);
    File tmpOut;
    try {
      tmpOut = com.android.sched.util.file.Files.createTempFile(
          "jackserver-" + storageFile.getName(), ".tmp",
          new Directory(storageFile.getParentFile().getPath(),
              null,
              Existence.MUST_EXIST,
              Permission.READ | Permission.WRITE,
              ChangePermission.NOCHANGE));
    } catch (NotDirectoryException | NoSuchFileException | FileAlreadyExistsException e) {
      // storageFile.getParentFile() is serverDir, it is a directory and we do not ask for creation
      throw new AssertionError(e.getMessage(), e);
    }
    try {
      if (!(tmpOut.setExecutable(false, false)
          && tmpOut.setWritable(false, false)
          && tmpOut.setReadable(false, false)
          && tmpOut.setWritable(true, true)
          && tmpOut.setReadable(true, true))) {
        throw new IOException("Failed to set permissions of '" + tmpOut.getPath() + "'");
      }
      try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmpOut), CONFIG_CHARSET)) {
        store(writer, "");
      }
      try {
        Files.move(tmpOut.toPath(), storageFile.toPath(), StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException e) {
        logger.log(Level.WARNING, "Atomic move not supported for renaming '" + tmpOut.getPath()
          + "' to '" + storageFile.getPath() + "'");
        Files.move(tmpOut.toPath(), storageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
      tmpOut = null;
    } finally {
      if (tmpOut != null) {
        if (!tmpOut.delete()) {
          logger.log(Level.SEVERE, "Failed to delete temp file '" + tmpOut.getPath() + "'");
        }
      }
    }
  }

  public boolean isModified() {
    return modified;
  }

  public int getServicePort() {
    return getProperty(ConfigFile.SERVICE_PORT_PROPERTY, Integer.valueOf(8076), new IntCodec())
        .intValue();
  }

  public int getAdminPort() {
    return getProperty(ConfigFile.ADMIN_PORT_PROPERTY, Integer.valueOf(8077), new IntCodec())
        .intValue();
  }

  @Nonnegative
  public int getTimeout() {
    return getDelay(ConfigFile.TIME_OUT_PROPERTY, 2 * 60 * 60);
  }

  @Nonnegative
  public int getIdleDelay() {
    return getDelay(ConfigFile.IDLE_PROPERTY, 3 * 60);
  }

  @Nonnegative
  public int getDeepIdleDelay() {
    return getDelay(ConfigFile.DEEP_IDLE_PROPERTY, 15 * 60);
  }

  public int getShutdownDelay() {
    return getDelay(ConfigFile.SHUTDOWN_PROPERTY, CONFIG_TIME_DISABLED_VALUE);
  }

  public long getMaxJarSize() {
    long maxJarSize = getProperty(
          ConfigFile.MAX_JAR_SIZE_PROPERTY, Long.valueOf(100 * 1024 * 1024),
          new LongCodec())
        .longValue();
    if (maxJarSize < -1) {
      logger.log(Level.WARNING,
          "Invalid config value for " + ConfigFile.MAX_JAR_SIZE_PROPERTY + ": " + maxJarSize);
      maxJarSize = -1;
    }
    return maxJarSize;
  }

  public int getMaxServices() {
    return getProperty(ConfigFile.MAX_SERVICE_PROPERTY, Integer.valueOf(4),
        new IntCodec()).intValue();
  }

  public long getConfigVersion() {
    return getProperty(ConfigFile.CONFIG_VERSION_PROPERTY, Long.valueOf(-1), new LongCodec())
        .longValue();
  }

  @Nonnull
  public List<Pair<Integer, Long>> getMaxServiceByMem() {
    List<Pair<Integer, Long>> list = getProperty(
        ConfigFile.MAX_SERVICE_BY_MEM_PROPERTY, DEFAULT_MAX_SERVICES_BY_MEM,
        new ListCodec<>(
            new PairCodec<>(new IntCodec(1, Integer.MAX_VALUE), new LongCodec()).on("="))
        .setSeparator(":"));

    return list;
  }

  @Nonnegative
  private int getDelay(@Nonnull String property, int defaultValue) {
    int delay = getProperty(property, Integer.valueOf(defaultValue), new IntCodec())
        .intValue();
    if (delay == CONFIG_TIME_DISABLED_VALUE) {
      delay = TIME_DISABLED_VALUE;
    } else if (delay < 0) {
      logger.log(Level.WARNING, "Invalid config value for " + property + ": " + delay);
      delay = TIME_DISABLED_VALUE;
    }
    return delay;
  }

  @Nonnull
  private <T> T getProperty(
      @Nonnull String key,
      @Nonnull T defaultValue,
      @Nonnull StringCodec<T> codec) {
    String stringValue = getProperty(key);
    T value = null;
    if (stringValue != null) {
      try {
        CodecContext context = new CodecContext();
        value = codec.checkString(context, stringValue);
        if (value == null) {
          value = codec.parseString(context, stringValue);
        }
      } catch (ParsingException e) {
        logger.log(Level.SEVERE, e.getMessage());
      }
    }
    if (value == null) {
      value = defaultValue;
      setProperty(key, codec.formatValue(defaultValue));
    }
    return value;
  }

  // FINDBUGS
  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  // FINDBUGS
  @Override
  public int hashCode() {
    return super.hashCode();
  }
}