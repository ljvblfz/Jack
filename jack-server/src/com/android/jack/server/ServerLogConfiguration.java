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
import com.android.sched.util.codec.EnumCodec;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.log.LogFormatter;
import com.android.sched.util.log.LoggerConfiguration;
import com.android.sched.util.log.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Configure Logging for the server.
 */
public class ServerLogConfiguration implements LoggerConfiguration {

  /**
   * Thrown when logging can't be configured.
   */
  public static class ServerLogConfigurationException extends Exception {
    private static final long serialVersionUID = 1L;

    public ServerLogConfigurationException(String message) {
      super(message);
    }
  }

  @VariableName("level")
  private static enum LogLevel  {
    ERROR {
      @Nonnull
      @Override
      public List<PackageLevel> getLevels() {
        return Arrays.asList(new PackageLevel("", Level.SEVERE),
            new PackageLevel("com.android.jack.server", Level.INFO));
      }
    },
    WARNING {
      @Nonnull
      @Override
      public List<PackageLevel> getLevels() {
        return Arrays.asList(
            new PackageLevel("", Level.WARNING),
            new PackageLevel("com.android.jack.server", Level.INFO));
      }
    },
    DEBUG {
      @Nonnull
      @Override
      public List<PackageLevel> getLevels() {
        return Arrays.asList(
            new PackageLevel("", Level.WARNING),
            new PackageLevel("com.android.jack", Level.FINE),
            new PackageLevel("com.android.sched", Level.WARNING));
      }
    },
    TRACE {
      @Nonnull
      @Override
      public List<PackageLevel> getLevels() {
        return Arrays.asList(new PackageLevel("", Level.FINEST));
      }
    };

    @Nonnull
    protected abstract List<PackageLevel> getLevels();
  }

  @Nonnull
  private static final String LOG_FILE_PROPERTY = "com.android.jack.server.log.file";

  @Nonnull
  private static final String LOG_LEVEL_PROPERTY = "com.android.jack.server.log";

  @Nonnegative
  private static final int MAX_LOG_FILE_SIZE = 1 * 1024 * 1024;
  @Nonnegative
  private static final int LOG_FILE_COUNT = 2;


    @Nonnull
    private final Handler handler;

    @Nonnull
    private final LogLevel level;

    private ServerLogConfiguration(@Nonnull LogLevel level, @Nonnull String logFilePattern)
        throws IOException {
      this.level = level;
      handler = new FileHandler(logFilePattern, MAX_LOG_FILE_SIZE, LOG_FILE_COUNT);
      handler.setFormatter(new LogFormatter());
      handler.setLevel(Level.FINEST);

    }

    @Nonnull
    @Override
    public Collection<Handler> getHandlers() {
      return Collections.<Handler>singletonList(handler);

    }

    @Nonnull
    @Override
    public List<PackageLevel> getLevels() {
      return level.getLevels();
    }

  @Nonnull
  public static void setupLog() throws ServerLogConfigurationException {
    String stringLevel = System.getProperty(LOG_LEVEL_PROPERTY, LogLevel.WARNING.name());
    String logFilePattern = System.getProperty(LOG_FILE_PROPERTY, getDefaultLogPattern());
    try {
      LogLevel level = new EnumCodec<>(LogLevel.class, LogLevel.values()).checkString(
          new CodecContext(), stringLevel);
      LoggerFactory.configure(new ServerLogConfiguration(level, logFilePattern));
    } catch (IOException e) {
      throw new ServerLogConfigurationException(
          "Failed to open log file(s)");
    } catch (ParsingException e) {
      throw new ServerLogConfigurationException(e.getMessage());
    }
  }

  @Nonnull
  private static String getDefaultLogPattern() {
    String tmpDir = System.getProperty("java.io.tmpdir");
    if (tmpDir != null) {
      tmpDir = tmpDir.replace(File.separatorChar, '/') + '/';
    } else {
      tmpDir = "";
    }

    return tmpDir + "jack-" + System.getProperty("user.name") + "/jack-server-%u-%g.log";
  }

}
