/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.sched.util.codec;

import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.InputJarFile;
import com.android.sched.util.location.Location;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link StringCodec} is used to create an instance of a {@link ServiceLoader}.
 *
 * @param <T> the type of the service
 */
public class ServiceJarCodec<T> implements StringCodec<InputJarFile> {
  @Nonnull
  private final InputJarCodec codec = new InputJarCodec();
  @Nonnull
  private final Class<T> type;

  public ServiceJarCodec(@Nonnull Class<T> type) {
    this.type = type;
  }

  @Override
  @Nonnull
  public InputJarFile parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @CheckForNull
  public InputJarFile checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    InputJarFile jar = codec.checkString(context, string);
    if (jar == null) {
      jar = codec.parseString(context, string);
    }

    try {
      checkJarFile(jar);
      return jar;
    } catch (Exception e) {
      throw new ParsingException(e);
    }
  }

  @Nonnull
  protected void throwException(@Nonnull Location location) throws Exception {
    throw new NotServiceFileException(location, type);
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "a path to a service '" + type.getCanonicalName() + "' jar file ("
        + codec.getDetailedUsage() + ")";
  }

  @Override
  @Nonnull
  public String getVariableName() {
    return "jar";
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull InputJarFile jar) {
    return jar.getPath();
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull InputJarFile jar)
      throws CheckingException {
    try {
      checkJarFile(jar);
    } catch (Exception e) {
      throw new CheckingException(e);
    }
  }

  private void checkJarFile(@Nonnull InputJarFile jar) throws Exception {
    ServiceLoader<T> loader;
    try {
      loader =
          ServiceLoader.load(type, new URLClassLoader(new URL[] {jar.getFile().toURI().toURL()},
              ServiceJarCodec.class.getClassLoader()));

      if (!loader.iterator().hasNext()) {
        throwException(jar.getLocation());
      }
    } catch (MalformedURLException e) {
      throw new AssertionError(e);
    }
  }
}
