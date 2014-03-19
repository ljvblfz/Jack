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

import javax.annotation.Nonnull;

/**
 * Implementation of a fully built {@code Config}.
 */
class ConfigImpl implements Config {
  @Nonnull
  private final CodecContext context;
  @Nonnull
  private final Map<PropertyId<?>, String> stringValuesById = new HashMap<PropertyId<?>, String>();
  @Nonnull
  private final Map<KeyId<?, ?>, Object> instanceValuesById = new HashMap<KeyId<?, ?>, Object>();

  /**
   * @param context Context for parsers
   * @param stringValues All the property values as {@code String} objects.
   * @param instanceValues All the property values as objects.
   */
  ConfigImpl(@Nonnull CodecContext context, @Nonnull Map<PropertyId<?>, String> stringValues,
      @Nonnull Map<KeyId<?, ?>, Object> instanceValues) {
    this.context = context;
    this.stringValuesById.putAll(stringValues);
    this.instanceValuesById.putAll(instanceValues);
  }

  @Override
  @Nonnull
  public synchronized <T, S> T get(@Nonnull KeyId<T, S> keyId) {
    @SuppressWarnings("unchecked")
    T instance = (T) instanceValuesById.get(keyId);

    if (instance == null) {
      if (keyId instanceof PropertyId) {
        @SuppressWarnings("unchecked")
        PropertyId<T> propertyId = (PropertyId<T>) keyId;

        String value = stringValuesById.get(propertyId);
        if (value == null) {
          throw new ConfigurationError("Property '" + propertyId.getName()
              + "' is unknown (see annotation @" + HasKeyId.class.getSimpleName()
              + " or requiredIf expression)");
        }

        instance = propertyId.getCodec().parseString(context, value);
        instanceValuesById.put(propertyId, instance);
        stringValuesById.remove(propertyId);
      } else {
        @SuppressWarnings("unchecked")
        ObjectId<T> objectId = (ObjectId<T>) keyId;

        instance = objectId.createObject();
        instanceValuesById.put(objectId, instance);
      }
    }

    return instance;
  }

  @Override
  @Nonnull
  public Collection<PropertyId<?>> getPropertyIds() {
    ArrayList<PropertyId<?>> result =
        new ArrayList<PropertyId<?>>(instanceValuesById.size() + stringValuesById.size());

    for (KeyId<?, ?> keyId : stringValuesById.keySet()) {
      if (keyId.isPublic()) {
        if (keyId instanceof PropertyId) {
          result.add((PropertyId<?>) keyId);
        }
      }
    }

    for (KeyId<?, ?> keyId : instanceValuesById.keySet()) {
      if (keyId.isPublic()) {
        if (keyId instanceof PropertyId) {
          result.add((PropertyId<?>) keyId);
        }
      }
    }

    return result;
  }

  @Override
  @Nonnull
  public <T> String getAsString(@Nonnull PropertyId<T> propertyId) {
    String result;

    result = stringValuesById.get(propertyId);
    if (result == null) {
      @SuppressWarnings("unchecked")
      T instance = (T) instanceValuesById.get(propertyId);
      if (instance == null) {
        throw new ConfigurationError("Property '" + propertyId.getName()
            + "' is unknown (see annotation @" + HasKeyId.class.getSimpleName()
            + " or requiredIf expression)");
      }

      result = propertyId.getCodec().formatValue(instance);
    }

    return result;
  }
}
