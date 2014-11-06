/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.jayce;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.reporting.ReportableException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.util.log.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * A factory of {@link JayceInternalReader}.
 */
public abstract class JayceReaderFactory {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  public static JayceInternalReader get(@Nonnull InputJackLibrary inputJackLibrary,
      @Nonnull InputStream in) throws LibraryFormatException {
    String majorVersionStr = inputJackLibrary.getProperty(JayceProperties.KEY_JAYCE_MAJOR_VERSION);
    String minorVersionStr = inputJackLibrary.getProperty(JayceProperties.KEY_JAYCE_MINOR_VERSION);

    int majorVersion;
    int minorVersion;

    try {
      majorVersion = Integer.parseInt(majorVersionStr);
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Failed to parse the property "
          + JayceProperties.KEY_JAYCE_MAJOR_VERSION + " from "
          + inputJackLibrary.getLocation().getDescription(), e);
      throw new LibraryFormatException(inputJackLibrary.getLocation());
    }

    try {
      minorVersion = Integer.parseInt(minorVersionStr);
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Failed to parse the property "
          + JayceProperties.KEY_JAYCE_MINOR_VERSION + " from "
          + inputJackLibrary.getLocation().getDescription(), e);
      throw new LibraryFormatException(inputJackLibrary.getLocation());
    }

    String className = "com.android.jack.jayce.v"
        + JackLibraryFactory.getVersionString(majorVersion) + ".io.JayceInternalReaderImpl";

    JayceInternalReader jayceReader = (JayceInternalReader) instantiateConstructorWithParameters(
        className, new Class[] {InputStream.class}, new Object[] {in}, majorVersionStr);


    int minorMin = jayceReader.getMinorMin();
    int currentMinor = jayceReader.getCurrentMinor();
    if (minorVersion < minorMin) {
      throw new JayceVersionException("The version of the jayce file is not supported anymore."
          + "File version: " + majorVersionStr + "." + minorVersion + " - Current version: "
          + majorVersionStr + "." + currentMinor + " - Minimum compatible version: "
          + majorVersionStr + "." + minorMin);
    } else if (minorVersion > currentMinor) {
      throw new JayceVersionException("The version of the jayce file is too recent."
          + "File version: " + majorVersionStr + "." + minorVersion + " - Current version: "
          + majorVersionStr + "." + currentMinor);
    } else if (minorVersion < currentMinor) {
      Jack.getSession().getUserLogger().log(Level.WARNING,
          "The version of the jayce file is older than the current version but is "
          + "supported. File version: {0}.{1} - Current version: {2}.{3}", new Object[] {
          Integer.valueOf(majorVersionStr), Integer.valueOf(minorVersion),
          Integer.valueOf(majorVersionStr), Integer.valueOf(currentMinor)});
    }

    if (Integer.parseInt(majorVersionStr) == 2 && minorVersion == 14) {
      // Read jayce file header, after jayce version 2.14, header does no longer exists it was moved
      // to jack library properties
      try {
        new JayceHeader(in);
      } catch (JayceFormatException e) {
        logger.log(Level.SEVERE,
            "Library " + inputJackLibrary.getLocation().getDescription() + " is invalid", e);
        throw new LibraryFormatException(inputJackLibrary.getLocation());
      } catch (IOException e) {
        ReportableException exceptionToReport = new LibraryReadingException(e);
        Jack.getSession().getReporter().report(Severity.FATAL, exceptionToReport);
        throw new JackAbortException(exceptionToReport);
      }
    }
    return jayceReader;
  }

  @Nonnull
  private static Object instantiateConstructorWithParameters(@Nonnull String className,
      @Nonnull Class<?>[] parameterTypes, @Nonnull Object[] parameterInstances,
      @Nonnull String version)
      throws JayceVersionException {
    Object constructorInstance = null;
    try {
      Class<?> jayceReaderClass = Class.forName(className);
      Constructor<?> constructor = jayceReaderClass.getConstructor(parameterTypes);
      constructorInstance = constructor.newInstance(parameterInstances);
    } catch (SecurityException e) {
      throw new AssertionError("Security issue with Jayce stream");
    } catch (IllegalArgumentException e) {
      throw new AssertionError("Illegal argument for Jayce processor for version " + version);
    } catch (ClassNotFoundException e) {
      throw new JayceVersionException("Jayce version " + version + " not supported");
    } catch (NoSuchMethodException e) {
      throw new AssertionError("Jayce processing method not found for version " + version);
    } catch (InstantiationException e) {
      throw new AssertionError("Problem instantiating Jayce processor for version " + version);
    } catch (IllegalAccessException e) {
      throw new AssertionError("Problem accessing Jayce processor for version " + version);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
    return constructorInstance;
  }
}
