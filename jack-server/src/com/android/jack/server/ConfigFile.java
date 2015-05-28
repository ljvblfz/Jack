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
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

class ConfigFile extends Properties {

  @Nonnull
  private static Logger logger = Logger.getLogger(ConfigFile.class.getName());

  static final int CURRENT_CONFIG_VERSION = 1;
  @Nonnull
  private static final Charset CONFIG_CHARSET = StandardCharsets.UTF_8;
  private static final long serialVersionUID = 1L;
  private boolean modified = false;

  @Nonnull
  static final String ADMIN_PORT_PROPERTY = "jack.server.admin.port";

  @Nonnull
  static final String SERVICE_PORT_PROPERTY = "jack.server.service.port";

  @Nonnull
  static final String MAX_JAR_SIZE_PROPERTY = "jack.server.max-jars-size";

  @Nonnull
  static final String MAX_SERVICE_PROPERTY = "jack.server.max-service";

  @Nonnull
  static final String TIME_OUT_PROPERTY = "jack.server.time-out";

  @Nonnull
  static final String CONFIG_VERSION_PROPERTY = "jack.server.config.version";

  @Nonnull
  static final String CONFIG_FILE_NAME = "config.properties";

  @Override
  public Object setProperty(String key, String value) {
    modified = true;
    return super.setProperty(key, value);
  }

  public void loadIfPossible(File configFile) throws IOException {
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

  public void store(File configFile) throws WrongPermissionException, NotFileException,
    IOException {
    setProperty(ConfigFile.CONFIG_VERSION_PROPERTY,
        Integer.toString(CURRENT_CONFIG_VERSION));  // FINDBUGS

    new OutputStreamFile(configFile.getPath(), null, Existence.MAY_EXIST,
        ChangePermission.NOCHANGE, /* append = */ false);
    File tmpOut = File.createTempFile(configFile.getName(), ".tmp", configFile.getParentFile());
    try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmpOut), CONFIG_CHARSET)) {
      store(writer, "");
    }
    if (!tmpOut.renameTo(configFile)) {
      throw new IOException("failed to rename temp config file '" + tmpOut);
    }
  }

  public boolean isModified() {
    return modified;
  }

  public <T> T getProperty(String key, T defaultValue, StringCodec<T> codec) {
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

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}