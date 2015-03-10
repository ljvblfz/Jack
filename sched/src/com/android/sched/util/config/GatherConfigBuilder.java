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

package com.android.sched.util.config;

import com.android.sched.util.RunnableHooks;
import com.android.sched.util.config.ChainedException.ChainedExceptionBuilder;
import com.android.sched.util.config.category.Category;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class is used to configure a {@link Config} object. All methods but {@link #build()} do
 * not report any problem. Problems are reported in one block through a {@link ChainedException}
 * during the {@link #build()} operation.
 */
public class GatherConfigBuilder {
  @Nonnull
  private final AsapConfigBuilder builder = new AsapConfigBuilder();

  @Nonnull
  private final ChainedExceptionBuilder<ConfigurationException> exceptions =
      new ChainedExceptionBuilder<ConfigurationException>();

  @Nonnull
  public GatherConfigBuilder load(@Nonnull InputStream is, @Nonnull Location location)
      throws IOException {
    try {
      builder.load(is, location);
    } catch (ConfigurationException e) {
      exceptions.appendException(e);
    }

    return this;
  }

  @Nonnull
  public GatherConfigBuilder setString(@Nonnull String name, @Nonnull String value) {
    try {
      builder.setString(name, value);
    } catch (PropertyIdException e) {
      exceptions.appendException(e);
    } catch (UnknownPropertyNameException e) {
      exceptions.appendException(e);
    }

    return this;
  }


  @Nonnull
  public <T> GatherConfigBuilder set(@Nonnull String name, @Nonnull T value) {
    try {
      builder.set(name, value);
    } catch (PropertyIdException e) {
      exceptions.appendException(e);
    } catch (UnknownPropertyNameException e) {
      exceptions.appendException(e);
    }

    return this;
  }

  @Nonnull
  public GatherConfigBuilder setString(
      @Nonnull String name, @Nonnull String value, @Nonnull Location location) {
    try {
      builder.setString(name, value, location);
    } catch (UnknownPropertyNameException e) {
      exceptions.appendException(e);
    } catch (PropertyIdException e) {
      exceptions.appendException(e);
    }

    return this;
  }


  @Nonnull
  public <T> GatherConfigBuilder set(
      @Nonnull String name, @Nonnull T value, @Nonnull Location location) {
    try {
      builder.set(name, value, location);
    } catch (UnknownPropertyNameException e) {
      exceptions.appendException(e);
    } catch (PropertyIdException e) {
      exceptions.appendException(e);
    }

    return this;
  }

  @Nonnull
  public GatherConfigBuilder setString(@Nonnull PropertyId<?> propertyId, @Nonnull String value) {
    try {
      builder.setString(propertyId, value);
    } catch (UnknownPropertyIdException e) {
      exceptions.appendException(e);
    } catch (PropertyIdException e) {
      exceptions.appendException(e);
    }

    return this;
  }

  @Nonnull
  public <T> GatherConfigBuilder set(@Nonnull PropertyId<T> propertyId, @Nonnull T value) {
    try {
      builder.set(propertyId, value);
    } catch (UnknownPropertyIdException e) {
      exceptions.appendException(e);
    } catch (PropertyIdException e) {
      exceptions.appendException(e);
    }

    return this;
  }

  @Nonnull
  public GatherConfigBuilder setString(
      @Nonnull PropertyId<?> propertyId, @Nonnull String value, @Nonnull Location location) {
    try {
      builder.setString(propertyId, value, location);
    } catch (UnknownPropertyIdException e) {
      exceptions.appendException(e);
    } catch (PropertyIdException e) {
      exceptions.appendException(e);
    }

    return this;
  }

  @Nonnull
  public <T> GatherConfigBuilder set(
      @Nonnull PropertyId<T> propertyId, @Nonnull T value, @Nonnull Location location) {
    try {
      builder.set(propertyId, value, location);
    } catch (UnknownPropertyIdException e) {
      exceptions.appendException(e);
    } catch (PropertyIdException e) {
      exceptions.appendException(e);
    }

    return this;
  }

  @Nonnull
  public <T> GatherConfigBuilder set(@Nonnull ObjectId<T> objectId, @Nonnull T value) {
    builder.set(objectId, value);

    return this;
  }

  @Nonnull
  public <T> GatherConfigBuilder set(
      @Nonnull ObjectId<T> objectId, @Nonnull T value, @Nonnull Location location) {
    builder.set(objectId, value, location);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder setDebug() {
    builder.setDebug();

    return this;
  }

  @Nonnull
  public GatherConfigBuilder setStandardInput(@Nonnull InputStream in) {
    builder.setStandardInput(in);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder setStandardOutput(@Nonnull PrintStream printer) {
    builder.setStandardOutput(printer);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder setStandardError(@Nonnull PrintStream printer) {
    builder.setStandardError(printer);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder setWorkingDirectory(@Nonnull File workingDirectory)
      throws NotDirectoryException, WrongPermissionException, NoSuchFileException {
    builder.setWorkingDirectory(workingDirectory);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder setStrictMode() {
    builder.setStrictMode();

    return this;
  }

  @Nonnull
  public GatherConfigBuilder setHooks(@Nonnull RunnableHooks hooks) {
    builder.setHooks(hooks);

    return this;
  }

  /**
   * Builds the {@link Config} with all defined property values.
   *
   * @return the {@link Config}.
   * @throws ConfigurationException
   */
  @Nonnull
  public Config build() throws ConfigurationException {
    Config config;

    try {
      config = builder.build();
    } catch (ConfigurationException e) {
      exceptions.appendException(e);
      throw exceptions.getException();
    }

    exceptions.throwIfNecessary();

    return config;
  }

  @Nonnull
  public Collection<PropertyId<?>> getPropertyIds() {
    return builder.getPropertyIds(Category.class);
  }

  @Nonnull
  public Collection<PropertyId<?>> getPropertyIds(@Nonnull Class<? extends Category> category) {
    return builder.getPropertyIds(category);
  }

  @CheckForNull
  public String getDefaultValue(@Nonnull PropertyId<?> propertyId) {
    return builder.getDefaultValue(propertyId);
  }

  @Nonnull
  public GatherConfigBuilder processEnvironmentVariables(@Nonnull String envPrefix) {
    try {
      builder.processEnvironmentVariables(envPrefix);
    } catch (ConfigurationException e) {
      exceptions.appendException(e);
    }

    return this;
  }

  //
  // Default location
  //

  public void pushDefaultLocation(@Nonnull Location location) {
    builder.pushDefaultLocation(location);
  }

  public void popDefaultLocation() {
    builder.popDefaultLocation();
  }

  //
  // Commodity helper
  //

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Boolean> propertyId, boolean value) {
    set(propertyId, Boolean.valueOf(value));

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Boolean> propertyId, boolean value,
      @Nonnull Location location) {
    set(propertyId, Boolean.valueOf(value), location);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Byte> propertyId, byte value) {
    set(propertyId, Byte.valueOf(value));

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Byte> propertyId, byte value,
      @Nonnull Location location) {
    set(propertyId, Byte.valueOf(value), location);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Short> propertyId, short value) {
    set(propertyId, Short.valueOf(value));

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Short> propertyId, short value,
      @Nonnull Location location) {
    set(propertyId, Short.valueOf(value), location);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Character> propertyId, char value) {
    set(propertyId, Character.valueOf(value));

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Character> propertyId, char value,
      @Nonnull Location location) {
    set(propertyId, Character.valueOf(value), location);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Integer> propertyId, int value) {
    set(propertyId, Integer.valueOf(value));

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Integer> propertyId, int value,
      @Nonnull Location location) {
    set(propertyId, Integer.valueOf(value), location);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Long> propertyId, long value) {
    set(propertyId, Long.valueOf(value));

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Long> propertyId, long value,
      @Nonnull Location location) {
    set(propertyId, Long.valueOf(value), location);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Float> propertyId, float value) {
    set(propertyId, Float.valueOf(value));

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Float> propertyId, float value,
      @Nonnull Location location) {
    set(propertyId, Float.valueOf(value), location);

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Double> propertyId, double value) {
    set(propertyId, Double.valueOf(value));

    return this;
  }

  @Nonnull
  public GatherConfigBuilder set(@Nonnull PropertyId<Double> propertyId, double value,
      @Nonnull Location location) {
    set(propertyId, Double.valueOf(value), location);

    return this;
  }
}
