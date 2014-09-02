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

package com.android.sched.util.config;

import com.google.common.base.CharMatcher;

import com.android.sched.reflections.ReflectionFactory;
import com.android.sched.reflections.ReflectionManager;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.config.ChainedException.ChainedExceptionBuilder;
import com.android.sched.util.config.expression.BooleanExpression;
import com.android.sched.util.config.id.KeyId;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.location.EnvironmentLocation;
import com.android.sched.util.location.FieldLocation;
import com.android.sched.util.location.LineLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class is used to configure a {@link Config} object. All methods reporting problems throw
 * exceptions As Soon As Possible.
 */
public class AsapConfigBuilder {
  @Nonnull
  private static final Logger logger = Logger.getLogger(AsapConfigBuilder.class.getName());

  @Nonnull
  private static final NoLocation NO_LOCATION = new NoLocation();

  @Nonnull
  private static final Map<String, KeyId<?, ?>> keyIdsByName =
      new HashMap<String, KeyId<?, ?>>();

  @Nonnull
  private static final Map<KeyId<?, ?>, FieldLocation> defaultLocationsByKeyId =
      new HashMap<KeyId<?, ?>, FieldLocation>();

  @Nonnull
  private final Map<PropertyId<?>, PropertyId<?>.Value> valuesById =
      new HashMap<PropertyId<?>, PropertyId<?>.Value>();

  @Nonnull
  private final Map<ObjectId<?>, Object> instances =
      new HashMap<ObjectId<?>, Object>();

  @Nonnull
  private final Map<KeyId<?, ?>, Location> locationsByKeyId =
      new HashMap<KeyId<?, ?>, Location>();

  @Nonnull
  private final Stack<Location> defaultLocations = new Stack<Location>();

  @Nonnull
  private final CodecContext context = new CodecContext();

  private boolean strict = false;

  public AsapConfigBuilder() {
    defaultLocations.push(NO_LOCATION);
  }

  static {
    ReflectionManager reflectionManager = ReflectionFactory.getManager();

    Set<Class<? extends HasKeyId>> classesWithIds =
        reflectionManager.getSubTypesOf(HasKeyId.class);

    boolean hasErrors = false;
    for (Class<? extends HasKeyId> propertyIdClass : classesWithIds) {
      Field[] fields = propertyIdClass.getDeclaredFields();

      for (Field field : fields) {
        if (KeyId.class.isAssignableFrom(field.getType())) {
          if ((field.getModifiers() & Modifier.STATIC) == 0) {
            logger.log(Level.WARNING, "Key id ''{0}'' should be declared static in ''{1}''",
                new Object[] {field.getName(), propertyIdClass.getName()});

            continue;
          }

          if ((field.getModifiers() & Modifier.FINAL) == 0) {
            hasErrors = true;
            logger.log(Level.SEVERE, "Key id ''{0}'' must be declared final in ''{1}''",
                new Object[] {field.getName(), propertyIdClass.getName()});
          }

          try {
            field.setAccessible(true);
            KeyId<?, ?> keyId = (KeyId<?, ?>) field.get(null);
            if (defaultLocationsByKeyId.containsKey(keyId)) {
              hasErrors = true;
              logger.log(Level.SEVERE, "A Key id named ''{0}'' already exists in ''{1}''",
                  new Object[] {
                      keyId.getName(), defaultLocationsByKeyId.get(keyId).getDescription()});
            }

            defaultLocationsByKeyId.put(keyId, new FieldLocation(field));
            keyIdsByName.put(keyId.getName(), keyId);
          } catch (IllegalArgumentException e) {
            throw new AssertionError(e);
          } catch (IllegalAccessException e) {
            throw new AssertionError(e);
          }
        }
      }
    }

    if (hasErrors) {
      throw new ExceptionInInitializerError(
          new ConfigurationError("Problem on property declarations (see log)"));
    }
  }

  @Nonnull
  public AsapConfigBuilder load(@Nonnull InputStream is, @Nonnull Location location)
      throws IOException, ConfigurationException {
    ChainedExceptionBuilder<ConfigurationException> exceptions =
        new ChainedExceptionBuilder<ConfigurationException>();
    LineNumberReader br = new LineNumberReader(new InputStreamReader(is));

    String line = br.readLine();
    while (line != null) {
      if (line.length() > 0     &&
          line.charAt(0) != '#' &&
          !CharMatcher.WHITESPACE.matchesAllOf(line)) {
        int indexOfEqual = line.indexOf('=');

        if (indexOfEqual == -1) {
          exceptions.appendException(
              new FormatConfigurationException(new LineLocation(location, br.getLineNumber())));
        } else {
          String propertyName  = line.substring(0, indexOfEqual).trim();
          String propertyValue = line.substring(indexOfEqual + 1, line.length()).trim();

          try {
            set(propertyName, propertyValue, new LineLocation(location, br.getLineNumber()));
          } catch (ConfigurationException e) {
            exceptions.appendException(e);
          }
        }
      }

      line = br.readLine();
    }

    exceptions.throwIfNecessary();

    return this;
  }

  @Nonnull
  public AsapConfigBuilder setString(
      @Nonnull String name, @Nonnull String value, @Nonnull Location location)
      throws UnknownPropertyNameException, PropertyIdException {
    KeyId<?, ?> keyId = keyIdsByName.get(name);
    if (keyId == null || !(keyId instanceof PropertyId)) {
      throw new UnknownPropertyNameException(name);
    }

    try {
      setString((PropertyId<?>) keyId, value, location);
    } catch (UnknownPropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public <T> AsapConfigBuilder set(
      @Nonnull String name, @Nonnull T value, @Nonnull Location location)
      throws UnknownPropertyNameException, PropertyIdException {
    KeyId<?, ?> keyId = keyIdsByName.get(name);
    if (keyId == null || !(keyId instanceof PropertyId)) {
      throw new UnknownPropertyNameException(name);
    }

    @SuppressWarnings("unchecked")
    PropertyId<T> propertyId = (PropertyId<T>) keyId;

    if (context.isDebug()) {
      try {
        propertyId.getCodec().checkValue(context, value);
      } catch (Exception e) {
        throw new ConfigurationError("Property '" + name + "': " + e.getMessage());
      }
    }

    try {
      set(propertyId, value, location);
    } catch (UnknownPropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder setString(
      @Nonnull PropertyId<?> propertyId, @Nonnull String value, @Nonnull Location location)
      throws PropertyIdException {
    if (!keyIdsByName.values().contains(propertyId)) {
      throw new UnknownPropertyIdException(propertyId);
    }

    if (strict) {
      try {
        propertyId.getCodec().checkString(context, value);
      } catch (ParsingException e) {
        throw new PropertyIdException(propertyId, location, e);
      }
    }

    valuesById.put(propertyId, propertyId.new Value(value));
    locationsByKeyId.put(propertyId, location);

    return this;
  }


  @Nonnull
  public <T> AsapConfigBuilder set(
      @Nonnull PropertyId<T> propertyId, @Nonnull T value, @Nonnull Location location)
      throws PropertyIdException {
    if (!keyIdsByName.values().contains(propertyId)) {
      throw new UnknownPropertyIdException(propertyId);
    }

    if (context.isDebug()) {
      try {
        propertyId.getCodec().checkValue(context, value);
      } catch (Exception e) {
        throw new ConfigurationError("Property '" + propertyId.getName() + "': " + e.getMessage());
      }
    }

    valuesById.put(propertyId, propertyId.new Value(value));
    locationsByKeyId.put(propertyId, location);

    return this;
  }

  @Nonnull
  public <T> AsapConfigBuilder set(
      @Nonnull ObjectId<T> objectId, @Nonnull T value, @Nonnull Location location) {
    instances.put(objectId, value);
    locationsByKeyId.put(objectId, location);

    return this;
  }

  @Nonnull
  public AsapConfigBuilder setDebug() {
    context.setDebug();

    return this;
  }

  @Nonnull
  public AsapConfigBuilder setStrictMode() {
    this.strict = true;

    return this;
  }


  @Nonnull
  public AsapConfigBuilder setHooks(@Nonnull RunnableHooks hooks) {
    context.setHooks(hooks);

    return this;
  }

  /**
   * Builds the {@Config} with all defined property values.
   *
   * @return the {@code Config}.
   * @throws ConfigurationException
   */
  @Nonnull
  public <X> Config build() throws ConfigurationException {
    ChainedExceptionBuilder<ConfigurationException> exceptions =
        new ChainedExceptionBuilder<ConfigurationException>();

    if (context.isDebug()) {
      logger.setLevel(Level.FINE);
    } else {
      logger.setLevel(Level.INFO);
    }

    @Nonnull
    Map<PropertyId<?>, PropertyId<?>.Value> values =
        new HashMap<PropertyId<?>, PropertyId<?>.Value>();
    processValues(values);
    processDefaultValues(values);

    ConfigChecker checker =
        new ConfigChecker(context, values, instances, locationsByKeyId);

    for (KeyId<?, ?> keyId : keyIdsByName.values()) {
      boolean needChecks = false;

      try {
        // If, there is no constraints, or constraints are fulfilled
        needChecks = keyId.isRequired(checker);
        if (!needChecks) {
          // No need for that one
          BooleanExpression expression = keyId.getRequiredExpression();
          assert expression != null;
          checker.remove(keyId, expression.getCause(checker));
        }
      } catch (ConfigurationException e) {
        // Parsing error during constraints checking is ignored.
        // The error will be caught later
      }

      if (needChecks) {
        try {
          // Check the value
          checker.check(keyId);
        } catch (ConfigurationException e) {
          // If there are constraints, be more specific in the message
          BooleanExpression expression = keyId.getRequiredExpression();
          if (expression != null) {
            try {
              StringBuilder sb = new StringBuilder();

              sb.append(" (required because ");
              sb.append(expression.getCause(checker));
              sb.append(')');

              String detailed = sb.toString();
              for (ChainedException all : e) {
                e.setMessage(e.getMessage() + detailed);
              }
            } catch (PropertyIdException ignore) {
              // If not possible to get cause message, keep the original exception
            }
          }

          exceptions.appendException(e);
        }
      }
    }

    exceptions.throwIfNecessary();

    if (context.isDebug()) {
      return new ConfigDebug(
          context, checker.getValues(), checker.getInstances(), checker.getDropCauses());
    } else {
      return new ConfigImpl(context, checker.getValues(), checker.getInstances());
    }
  }

  @Nonnull
  public Collection<PropertyId<?>> getPropertyIds() {
    List<PropertyId<?>> result = new ArrayList<PropertyId<?>>(keyIdsByName.size());

    for (KeyId<?, ?> keyId : keyIdsByName.values()) {
      if (keyId.isPublic() && keyId instanceof PropertyId<?>) {
        result.add((PropertyId<?>) keyId);
      }
    }

    return result;
  }

  @CheckForNull
  public <T> String getDefaultValue(@Nonnull PropertyId<T> propertyId) {
    PropertyId<T>.Value value = propertyId.getDefaultValue(context);

    if (value != null) {
      return value.getString();
    } else {
      return null;
    }
  }

  private void processValues(@Nonnull Map<PropertyId<?>, PropertyId<?>.Value> values) {
    values.putAll(valuesById);
  }

  private void processDefaultValues(@Nonnull Map<PropertyId<?>, PropertyId<?>.Value> values) {
    for (KeyId<?, ?> keyId : keyIdsByName.values()) {
      if (keyId instanceof PropertyId) {
        PropertyId<?> propertyId = (PropertyId<?>) keyId;

        if (!values.containsKey(keyId)) {
          values.put(propertyId, propertyId.getDefaultValue(context));
          locationsByKeyId.put(keyId, defaultLocationsByKeyId.get(keyId));
        }
      }
    }
  }

  public AsapConfigBuilder processEnvironmentVariables(@Nonnull String envPrefix)
      throws ConfigurationException {
    ChainedExceptionBuilder<ConfigurationException> exceptions =
        new ChainedExceptionBuilder<ConfigurationException>();

    for (Entry<String, String> envKeyValue : System.getenv().entrySet()) {
      String envKey = envKeyValue.getKey();

      if (envKey.startsWith(envPrefix)) {
        PropertyId<?> propertyId = null;
        int numMatches = 0;
        String variable = envKey.substring(envPrefix.length());

        for (Entry<String, KeyId<?, ?>> entry : keyIdsByName.entrySet()) {
          String keyIdName = entry.getKey();
          String value = envKeyValue.getValue();

          if (keyIdName.equalsIgnoreCase(variable)
              || keyIdName.replace('.', '_').equalsIgnoreCase(variable)) {

            if (entry.getValue() instanceof PropertyId) {
              PropertyId<?> previousPropertyId = propertyId;
              propertyId = (PropertyId<?>) entry.getValue();

              numMatches++;
              if (numMatches == 2) {
                // If it is the second match, register the first one as an exception
                exceptions.appendException(new VariableMatchesSeveralConfigurationException(
                    envKey, previousPropertyId));
              }
              if (numMatches >= 2) {
                // Register the second and following matches as an exception
                assert propertyId != null;
                exceptions.appendException(
                    new VariableMatchesSeveralConfigurationException(envKey, propertyId));
              }

              logger.log(Level.INFO,
                  "Property ''{0}'' is overridden by environment variable ''{1}'' with ''{2}''",
                  new Object[] {keyIdName, envKey, value});

              try {
                assert propertyId != null;
                setString(propertyId, value, new EnvironmentLocation(envKey));
              } catch (ConfigurationException e) {
                exceptions.appendException(e);
              }
            } else {
              logger.log(Level.WARNING,
                  "Environment variable ''{0}'' try to overridde an object-id ''{1}'' with ''{2}''",
                  new Object[] {envKey, keyIdName, value});
            }
          }
        }

        if (numMatches == 0) {
          exceptions.appendException(new VariableDoesNotMatchConfigurationException(envKey));
        }
      }
    }

    exceptions.throwIfNecessary();

    return this;
  }

  //
  // Default location
  //

  public void pushDefaultLocation(@Nonnull Location location) {
    defaultLocations.push(location);
  }

  public void popDefaultLocation() {
    assert defaultLocations.size() > 1;
    defaultLocations.pop();
  }

  @Nonnull
  public <T> AsapConfigBuilder set(@Nonnull ObjectId<T> objectId, @Nonnull T value) {
    return set(objectId, value, defaultLocations.peek());
  }

  @Nonnull
  public <T> AsapConfigBuilder set(@Nonnull String name, @Nonnull T value)
      throws UnknownPropertyNameException, PropertyIdException {
    return set(name, value, defaultLocations.peek());
  }

  @Nonnull
  public <T> AsapConfigBuilder set(@Nonnull PropertyId<T> propertyId, @Nonnull T value)
      throws PropertyIdException {
    return set(propertyId, value, defaultLocations.peek());
  }

  @Nonnull
  public AsapConfigBuilder setString(@Nonnull PropertyId<?> propertyId, @Nonnull String value)
      throws PropertyIdException {
    return setString(propertyId, value, defaultLocations.peek());
  }

  @Nonnull
  public AsapConfigBuilder setString(@Nonnull String name, @Nonnull String value)
      throws UnknownPropertyNameException, PropertyIdException {
    return setString(name, value, defaultLocations.peek());
  }

  //
  // Commodity helper
  //

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Boolean> propertyId, boolean value) {
    try {
      set(propertyId, Boolean.valueOf(value));
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Boolean> propertyId, boolean value,
      @Nonnull Location location) {
    try {
      set(propertyId, Boolean.valueOf(value), location);
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Byte> propertyId, byte value) {
    try {
      set(propertyId, Byte.valueOf(value));
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Byte> propertyId, byte value,
      @Nonnull Location location) {
    try {
      set(propertyId, Byte.valueOf(value), location);
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Short> propertyId, short value) {
    try {
      set(propertyId, Short.valueOf(value));
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Short> propertyId, short value,
      @Nonnull Location location) {
    try {
      set(propertyId, Short.valueOf(value), location);
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Character> propertyId, char value) {
    try {
      set(propertyId, Character.valueOf(value));
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Character> propertyId, char value,
      @Nonnull Location location) {
    try {
      set(propertyId, Character.valueOf(value), location);
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Integer> propertyId, int value) {
    try {
      set(propertyId, Integer.valueOf(value));
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Integer> propertyId, int value,
      @Nonnull Location location) {
    try {
      set(propertyId, Integer.valueOf(value), location);
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Long> propertyId, long value) {
    try {
      set(propertyId, Long.valueOf(value));
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Long> propertyId, long value,
      @Nonnull Location location) {
    try {
      set(propertyId, Long.valueOf(value), location);
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Float> propertyId, float value) {
    try {
      set(propertyId, Float.valueOf(value));
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Float> propertyId, float value,
      @Nonnull Location location) {
    try {
      set(propertyId, Float.valueOf(value), location);
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Double> propertyId, double value) {
    try {
      set(propertyId, Double.valueOf(value));
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }

  @Nonnull
  public AsapConfigBuilder set(@Nonnull PropertyId<Double> propertyId, double value,
      @Nonnull Location location) {
    try {
      set(propertyId, Double.valueOf(value), location);
    } catch (PropertyIdException e) {
      throw new AssertionError();
    }

    return this;
  }
}
