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

import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.config.id.KeyId;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.PropertyId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Implementation of a fully built {@code Config}.
 */
class ConfigImpl implements Config, InternalConfig {
  @Nonnull
  private final CodecContext context;
  @Nonnull
  private final Map<PropertyId<?>, PropertyId<?>.Value> valuesById =
      new HashMap<PropertyId<?>, PropertyId<?>.Value>();
  @Nonnull
  private final Map<KeyId<?, ?>, Object> instancesById = new HashMap<KeyId<?, ?>, Object>();

  /**
   * @param context Context for parsers
   * @param values All the property values as {@code String} objects.
   * @param instances All the property values as objects.
   */
  ConfigImpl(@Nonnull CodecContext context, @Nonnull Map<PropertyId<?>, PropertyId<?>.Value> values,
      @Nonnull Map<KeyId<?, ?>, Object> instances) {
    this.context = context;
    this.valuesById.putAll(values);
    this.instancesById.putAll(instances);
  }

  @Override
  @Nonnull
  public <T> T get(@Nonnull PropertyId<T> propertyId) {
    @SuppressWarnings({"unchecked", "rawtypes"})
    PropertyId<T>.Value value = (PropertyId.Value) valuesById.get(propertyId);

    if (value == null) {
      throw new ConfigurationError("Property '" + propertyId.getName()
          + "' is unknown (see annotation @" + HasKeyId.class.getSimpleName()
          + " or requiredIf expression)");
    }

    return value.getObject(context);
  }

  @Override
  @CheckForNull
  public <T> T getObjectIfAny(@Nonnull PropertyId<T> propertyId) {
    @SuppressWarnings({"unchecked", "rawtypes"})
    PropertyId<T>.Value value = (PropertyId.Value) valuesById.get(propertyId);

    if (value == null) {
      throw new ConfigurationError("Property '" + propertyId.getName()
          + "' is unknown (see annotation @" + HasKeyId.class.getSimpleName()
          + " or requiredIf expression)");
    }

    return value.getObjectIfAny();
  }

  @Override
  @Nonnull
  public <T> String getAsString(@Nonnull PropertyId<T> propertyId) {
    @SuppressWarnings({"unchecked", "rawtypes"})
    PropertyId<T>.Value value = (PropertyId.Value) valuesById.get(propertyId);

    if (value == null) {
      throw new ConfigurationError("Property '" + propertyId.getName()
          + "' is unknown (see annotation @" + HasKeyId.class.getSimpleName()
          + " or requiredIf expression)");
    }

    return value.getString();
  }

  @Override
  @Nonnull
  public synchronized <T> T get(@Nonnull ObjectId<T> objectId) {
    @SuppressWarnings("unchecked")
    T instance = (T) instancesById.get(objectId);

    if (instance == null) {
      instance = objectId.createObject();
      instancesById.put(objectId, instance);
    }

    return instance;
  }

  @Override
  @Nonnull
  public Collection<PropertyId<?>> getPropertyIds() {
    ArrayList<PropertyId<?>> result =
        new ArrayList<PropertyId<?>>(instancesById.size() + valuesById.size());

    for (KeyId<?, ?> keyId : valuesById.keySet()) {
      if (keyId.isPublic()) {
        if (keyId instanceof PropertyId) {
          result.add((PropertyId<?>) keyId);
        }
      }
    }

    for (KeyId<?, ?> keyId : instancesById.keySet()) {
      if (keyId.isPublic()) {
        if (keyId instanceof PropertyId) {
          result.add((PropertyId<?>) keyId);
        }
      }
    }

    return result;
  }
}
