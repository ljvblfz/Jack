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

import com.google.common.collect.Lists;

import com.android.sched.util.log.LoggerConfiguration.PackageLevel;

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Handler;
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

  public static void configure(LoggerConfiguration configuration) {
    LogManager manager = LogManager.getLogManager();
    // reset configuration
    manager.reset();

    List<PackageLevel> levels = configuration.getLevels();
    if (levels.isEmpty()) {
      // nothing to do
      return;
    }

    // ensure logger are created
    for (PackageLevel level : levels) {
      Logger.getLogger(level.getPackageName());
    }

    List<PackageLevel> levelsReverse = Lists.reverse(levels);
    Enumeration<String> names = manager.getLoggerNames();
    Collection<Handler> handlers = configuration.getHandlers();

    while (names.hasMoreElements()) {
      String loggerName = names.nextElement();
      Logger logger = manager.getLogger(loggerName);

      if (logger == null) {
        continue;
      }

      for (Handler handler : handlers) {
        logger.addHandler(handler);
      }

      // Iterate in reverse order to test most specific package name first and then continue to
      // parents
      for (PackageLevel level : levelsReverse) {
        if (loggerName.startsWith(level.getPackageName())) {
          logger.setLevel(level.getLevel());
          logger.setUseParentHandlers(false);
          break;
        }
      }
    }
  }

  private LoggerFactory() {}
}
