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
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.file.WrongPermissionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

class ConfigFile extends Properties {

  @Nonnull
  private static Logger logger = Logger.getLogger(ConfigFile.class.getName());

  static final int CURRENT_CONFIG_VERSION = 2;

  static final int TIMEOUT_DISABLED = -1;

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
      CannotCreateFileException {
    setProperty(ConfigFile.CONFIG_VERSION_PROPERTY,
        Integer.toString(CURRENT_CONFIG_VERSION));  // FINDBUGS

    new OutputStreamFile(storageFile.getPath(), /* hooks = */ null);
    File tmpOut = File.createTempFile("jackserver-" + storageFile.getName(), ".tmp",
        storageFile.getParentFile());
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
      if (!tmpOut.renameTo(storageFile)) {
        throw new IOException("failed to rename temp config file '" + tmpOut);
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

  public int getTimeout() {
    int timeout = getProperty(ConfigFile.TIME_OUT_PROPERTY, Integer.valueOf(7200), new IntCodec())
        .intValue();
    if (timeout < 0 && timeout != TIMEOUT_DISABLED) {
      logger.log(Level.WARNING,
          "Invalid config value for " + ConfigFile.TIME_OUT_PROPERTY + ": " + timeout);
      timeout = TIMEOUT_DISABLED;
    }
    return timeout;
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