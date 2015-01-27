/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.library;

import com.android.jack.library.v0001.OutputJackLibraryImpl;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.id.BooleanPropertyId;
import com.android.sched.util.config.id.MessageDigestPropertyId;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.GenericInputVFS;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VFS;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Factory to instantiate {@link JackLibrary}.
 */
@HasKeyId
public abstract class JackLibraryFactory {
  @Nonnull
  public static final BooleanPropertyId GENERATE_JACKLIB_DIGEST = BooleanPropertyId.create(
      "jack.library.digest", "Generate message digest in Jack library").addDefaultValue(
      Boolean.TRUE);
  @Nonnull
  public static final MessageDigestPropertyId MESSAGE_DIGEST_ALGO = MessageDigestPropertyId
      .create("jack.library.digest.algo", "Message digest algorithm use in Jack library")
      .requiredIf(GENERATE_JACKLIB_DIGEST.getValue().isTrue()).addDefaultValue("SHA");

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  public static final int DEFAULT_MAJOR_VERSION = 1;

  @Nonnull
  private static final String VERSION_FORMAT = "%04d";

  @Nonnull
  public static String getVersionString(@Nonnegative int version) {
    return String.format(VERSION_FORMAT, Integer.valueOf(version));
  }

  @Nonnull
  public static InputJackLibrary getInputLibrary(@Nonnull VFS vdir)
      throws LibraryVersionException, LibraryFormatException, NotJackLibraryException {
    GenericInputVFS giVFS = new GenericInputVFS(vdir);
    Properties libraryProperties = loadLibraryProperties(giVFS);
    String majorVersion = getMajorVersionAsString(giVFS, libraryProperties);

    InputJackLibrary inputJackLibrary = (InputJackLibrary) instantiateConstructorWithParameters(
        vdir, "com.android.jack.library.v" + majorVersion + ".InputJackLibraryImpl",
        new Class[] {VFS.class, Properties.class}, new Object[] {vdir, libraryProperties},
        String.valueOf(majorVersion));

    return inputJackLibrary;
  }

  @Nonnull
  public static OutputJackLibrary getOutputLibrary(@Nonnull VFS vfs,
      @Nonnull String emitterId, @Nonnull String emitterVersion) {
    return new OutputJackLibraryImpl(vfs, emitterId, emitterVersion);
  }

  private static String getMajorVersionAsString(@Nonnull InputVFS vdir,
      @Nonnull Properties libraryProperties) throws LibraryFormatException {
    try {
      return (getVersionString(
          Integer.parseInt((String) libraryProperties.get(JackLibrary.KEY_LIB_MAJOR_VERSION))));
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Fails to parse the property " + JackLibrary.KEY_LIB_MAJOR_VERSION
          + " from the library " + vdir, e);
      throw new LibraryFormatException(vdir.getLocation());
    }
  }

  @Nonnull
  private static Properties loadLibraryProperties(@Nonnull InputVFS vfs)
      throws NotJackLibraryException {
    Properties libraryProperties = new Properties();

    try {
      InputVFile libProp =
          vfs.getRootInputVDir().getInputVFile(JackLibrary.LIBRARY_PROPERTIES_VPATH);
      libraryProperties.load(libProp.openRead());
    } catch (IOException e) {
      throw new NotJackLibraryException(vfs.getLocation());
    }

    return libraryProperties;
  }

  @Nonnull
  private static Object instantiateConstructorWithParameters(@Nonnull VFS vdir,
      @Nonnull String className, @Nonnull Class<?>[] parameterTypes,
      @Nonnull Object[] parameterInstances, @Nonnull String version)
      throws LibraryVersionException, LibraryFormatException {
    Object constructorInstance = null;
    try {
      Class<?> libraryReaderClass = Class.forName(className);
      Constructor<?> constructor = libraryReaderClass.getConstructor(parameterTypes);
      constructorInstance = constructor.newInstance(parameterInstances);
    } catch (SecurityException e) {
      throw new AssertionError();
    } catch (IllegalArgumentException e) {
      throw new AssertionError("Illegal argument for library constructor for version " + version);
    } catch (ClassNotFoundException e) {
      throw new LibraryVersionException(
          "Library " + vdir + " has an unsupported version " + version);
    } catch (NoSuchMethodException e) {
      throw new AssertionError("Library constructor not found for version " + version);
    } catch (InstantiationException e) {
      throw new AssertionError("Problem instantiating a library for version " + version);
    } catch (IllegalAccessException e) {
      throw new AssertionError("Problem accessing library constructor for version " + version);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof LibraryFormatException) {
        throw ((LibraryFormatException) cause);
      } else if (cause instanceof LibraryVersionException) {
        throw ((LibraryVersionException) cause);
      } else if (cause instanceof RuntimeException) {
        throw ((RuntimeException) cause);
      } else if (cause instanceof Error) {
        throw ((Error) cause);
      }
      throw new AssertionError(cause);
    }
    return constructorInstance;
  }
}
