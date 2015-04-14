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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

/**
 * Launcher protecting called main class from deletion of its classpath.
 */
public class Main {

  @Nonnull
  private static Logger logger = Logger.getLogger(Main.class.getName());

  private static final int CMD_IDX_CLASSPATH_OPTION = 0;
  private static final int CMD_IDX_CLASSPATH_VALUE = 1;
  private static final int CMD_IDX_MAIN_CLASS = 2;
  private static final int CMD_HANDLED_IDX = 3;


  public static void main(@Nonnull String[] args) {
    if (args.length < CMD_HANDLED_IDX) {
      if (args.length < CMD_HANDLED_IDX) {
        logUsage();
        abort();
      }
    }

    if (!args[CMD_IDX_CLASSPATH_OPTION].equals("-cp")) {
      logUsage();
      abort();
    }

    String classpath = args[CMD_IDX_CLASSPATH_VALUE];
    String[] classpathEntries = classpath.split(Pattern.quote(File.pathSeparator));

    ZipFile[] zips = new ZipFile[classpathEntries.length];
    for (int i = 0; i < zips.length; i++) {
      try {
        zips[i] = new ZipFile(classpathEntries[i]);
      } catch (IOException e) {
        logger.log(Level.INFO, "IOEception while trying to open '" + classpathEntries[i] + "'", e);
        logger.log(Level.SEVERE, "Failed to open '" + classpathEntries[i] + "'");
        abort();
      }
    }

    ClassLoader loader = new ZipLoader(zips);
    Class<?> clazz;
    try {
      clazz = loader.loadClass(args[CMD_IDX_MAIN_CLASS]);
    } catch (ClassNotFoundException e) {
      logger.log(Level.SEVERE, "Failed to find class '" + args[CMD_IDX_MAIN_CLASS] + "'");
      abort();
      return;
    }
    Method method;
    try {
      method = clazz.getMethod("main", args.getClass());
    } catch (SecurityException e) {
      throw new AssertionError();
    } catch (NoSuchMethodException e) {
      logger.log(Level.SEVERE, "Failed to find main method in class '" + args[CMD_IDX_MAIN_CLASS]
          + "'");
      abort();
      return;
    }
    String[] invokeArgs = new String[args.length - CMD_HANDLED_IDX];
    System.arraycopy(args, CMD_HANDLED_IDX, invokeArgs, 0, args.length - CMD_HANDLED_IDX);
    try {
      method.invoke(null, (Object) invokeArgs);
    } catch (IllegalArgumentException e) {
      throw new AssertionError();
    } catch (IllegalAccessException e) {
      throw new AssertionError();
    } catch (InvocationTargetException e) {
      logger.log(Level.SEVERE, "An error occured during delegate execution", e.getCause());
      abort();
    }
  }

  private static void logUsage() {
    logger.log(Level.SEVERE, "Usage: -cp <classpath> <main class> ...");
  }

  private static void abort() {
    System.exit(1);
  }

}
