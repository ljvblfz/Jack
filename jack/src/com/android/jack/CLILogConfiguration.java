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

package com.android.jack;

import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.EnumCodec;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.VariableName;
import com.android.sched.util.log.LogFormatter;
import com.android.sched.util.log.LoggerConfiguration;
import com.android.sched.util.log.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Configure Logging for Jack.
 */
public class CLILogConfiguration {

  /**
   * Thrown when logging can't be configured.
   */
  public static class LogConfigurationException extends Exception {
    private static final long serialVersionUID = 1L;

    public LogConfigurationException(String message) {
      super(message);
    }
  }

  @VariableName("level")
  private static enum LogLevel implements LoggerConfiguration {
    ERROR {
      @Override
      public List<PackageLevel> getLevels() {
        return Arrays.asList(new PackageLevel("", Level.SEVERE));
      }
    },
    WARNING {
      @Override
      public List<PackageLevel> getLevels() {
        return Arrays.asList(new PackageLevel("", Level.WARNING));
      }
    },
    DEBUG {
      @Override
      public List<PackageLevel> getLevels() {
        return Arrays.asList(
            new PackageLevel("", Level.WARNING),
            new PackageLevel("com.android.jack", Level.FINE),
            new PackageLevel("com.android.sched", Level.WARNING));
      }
    },
    TRACE {
      @Override
      public List<PackageLevel> getLevels() {
        return Arrays.asList(new PackageLevel("", Level.FINEST));
      }
    };

    @Override
    public Collection<Handler> getHandlers() {
      ConsoleHandler handler = new ConsoleHandler();
      handler.setFormatter(new LogFormatter());
      handler.setLevel(Level.FINEST);
      return Collections.<Handler>singletonList(handler);
    }
  }

  public static void setupLogs() throws LogConfigurationException {
    LogLevel level = LogLevel.WARNING;
    String stringLevel = System.getProperty("com.android.jack.log");
    if (stringLevel != null) {
      try {
        level = new EnumCodec<LogLevel>(LogLevel.class, LogLevel.values()).checkString(
            new CodecContext(), stringLevel);
      } catch (ParsingException e) {
        throw new LogConfigurationException(e.getMessage());
      }
    }
    LoggerFactory.configure(level);
  }
}
