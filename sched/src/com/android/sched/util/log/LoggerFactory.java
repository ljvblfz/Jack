/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.sched.util.log;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Factory class to manage logger
 */
public class LoggerFactory  {
  /**
   * Create a logger and set the name of a class accordingly to the caller.
   *
   * @return a logger
   */
  @Nonnull
  public static Logger getLogger() {
    StackTraceElement caller = findCaller();
    return Logger.getLogger(caller.getClassName());
  }

  @Nonnull
  private static StackTraceElement findCaller() {
    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    int idx = 0;

    // First, synchronize on this class
    for (; idx < stack.length; idx++) {
      if (stack[idx].getClassName().equals(LoggerFactory.class.getName())) {
        break;
      }
    }

    // Second, search the caller
    for (; idx < stack.length; idx++) {
      if (stack[idx].getClassName().equals(LoggerFactory.class.getName())) {
        continue;
      }

      return stack[idx];
    }

    throw new AssertionError();
  }

  /**
   * Overrides current logging properties by the one stored in filename to override the default
   * ones.
   * @param filename Path of the properties file either absolute in or relative
   *                 to clazz in classpath.
   * @param clazz Helps to locate the resource file. Usually the the class of the caller.
   */
  public static void loadLoggerConfiguration (@Nonnull Class<?> clazz, @Nonnull String filename) {
    assert (filename != null) && (clazz != null);

    InputStream is = null;

    try {
      is = clazz.getResourceAsStream(filename);
      if (is != null) {
        LogManager.getLogManager().readConfiguration(is);
      } else {
        LoggerFactory.getLogger().log(Level.WARNING,
            "Unable to locate custom logger properties file ''{0}''", filename);
      }
    } catch (IOException e) {
      LoggerFactory.getLogger().log(Level.WARNING,
          "An error occured while reading logger config file ''{0}''", clazz.getResource(filename));
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // Nothing more to be done.
        }
      }
    }
  }

  private LoggerFactory() {}
}
