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
import com.android.sched.util.codec.ParsingException;
import com.android.sched.util.codec.StringCodec;
import com.android.sched.util.config.id.KeyId;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.PropertyId;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Class representing a configuration in order to check it.
 */
public class ConfigChecker {
  @Nonnull
  private final CodecContext context;
  @Nonnull
  private final Map<PropertyId<?>, String> stringValuesById = new HashMap<PropertyId<?>, String>();
  @Nonnull
  private final Map<KeyId<?, ?>, Object> instanceValuesById = new HashMap<KeyId<?, ?>, Object>();
  @Nonnull
  private final Map<KeyId<?, ?>, Location> locationsById =
      new HashMap<KeyId<?, ?>, Location>();
  @Nonnull
  private final Map<KeyId<?, ?>, String> droppedById = new HashMap<KeyId<?, ?>, String>();

  /**
   * @param context Context for parsers
   * @param stringValues All the property values as {@code String} objects.
   * @param instanceValues All the property values as objects.
   */
  ConfigChecker(@Nonnull CodecContext context,
      @Nonnull Map<PropertyId<?>, String> stringValues,
      @Nonnull Map<ObjectId<?>, Object> instanceValues,
      @Nonnull Map<KeyId<?, ?>, Location> locationsById) {
    this.context = context;
    this.stringValuesById.putAll(stringValues);
    this.instanceValuesById.putAll(instanceValues);
    this.locationsById.putAll(locationsById);
  }

  @Nonnull
  public synchronized <T> T parse(@Nonnull PropertyId<T> propertyId) throws PropertyIdException
       {
    @SuppressWarnings("unchecked")
    T instance = (T) instanceValuesById.get(propertyId);

    if (instance == null) {
      String value = getRawValue(propertyId);
      try {
        StringCodec<T> parser = propertyId.getCodec();

        instance = parser.checkString(context, value);
        if (instance == null) {
          instance = parser.parseString(context, value);
        }

        instanceValuesById.put(propertyId, instance);
        stringValuesById.remove(propertyId);
      } catch (ParsingException e) {
        throw new PropertyIdException(propertyId, getLocation(propertyId), e);
      }
    }

    return instance;
  }

  public synchronized <T, S> void check(@Nonnull KeyId<T, S> keyId) throws PropertyIdException {
    if (instanceValuesById.get(keyId) == null) {
      if (keyId instanceof PropertyId) {
        @SuppressWarnings("unchecked")
        PropertyId<T> propertyId = (PropertyId<T>) keyId;
        String value = getRawValue(propertyId);
        try {
          T instance = propertyId.getCodec().checkString(context, value);
          if (instance != null) {
            instanceValuesById.put(propertyId, instance);
            stringValuesById.remove(keyId);
          }
        } catch (ParsingException e) {
          throw new PropertyIdException(propertyId, getLocation(propertyId), e);
        }

      } else {
        assert keyId instanceof ObjectId;

        @SuppressWarnings("unchecked")
        ObjectId<T> objectId = (ObjectId<T>) keyId;
        objectId.checkInstantiability();
      }
    }
  }

  @Nonnull
  public <T> String getRawValue(@Nonnull PropertyId<T> propertyId)
      throws MissingPropertyException {
    String value = stringValuesById.get(propertyId);

    if (value == null) {
      throw new MissingPropertyException(propertyId);
    }

    return value;
  }

  @Nonnull
  public Map<KeyId<?, ?>, Object> getInstances() {
    return instanceValuesById;
  }

  @Nonnull
  public Map<PropertyId<?>, String> getStrings() {
    return stringValuesById;
  }

  @Nonnull
  public Map<KeyId<?, ?>, String> getDropCauses() {
    return droppedById;
  }

  @Nonnull
  public Location getLocation(@Nonnull KeyId<?, ?> keyId) {
    assert locationsById.get(keyId) != null;

    return locationsById.get(keyId);
  }

  public void remove(@Nonnull KeyId<?, ?> keyId, @Nonnull String cause) {
    stringValuesById.remove(keyId);
    instanceValuesById.remove(keyId);
    locationsById.remove(keyId);
    droppedById.put(keyId, cause);
  }
}
