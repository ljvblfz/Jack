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
import com.android.sched.util.config.id.KeyId;
import com.android.sched.util.config.id.ObjectId;
import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.config.id.PropertyId.Value;
import com.android.sched.util.location.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

/**
 * Class representing a configuration in order to check it.
 */
public class ConfigChecker {
  @Nonnull
  private final CodecContext context;
  @Nonnull
  private final Map<PropertyId<?>, PropertyId<?>.Value> values =
      new HashMap<PropertyId<?>, PropertyId<?>.Value>();
  @Nonnull
  private final Map<KeyId<?, ?>, Object> instances = new HashMap<KeyId<?, ?>, Object>();
  @Nonnull
  private final Map<KeyId<?, ?>, Location> locations =
      new HashMap<KeyId<?, ?>, Location>();
  @Nonnull
  private final Map<KeyId<?, ?>, String> dropped = new HashMap<KeyId<?, ?>, String>();

  /**
   * @param context Context for parsers
   * @param stringValues All the property values as {@code String} objects.
   * @param instanceValues All the property values as objects.
   */
  ConfigChecker(@Nonnull CodecContext context,
      @Nonnull Map<PropertyId<?>, PropertyId<?>.Value> stringValues,
      @Nonnull Map<ObjectId<?>, Object> instanceValues,
      @Nonnull Map<KeyId<?, ?>, Location> locationsById) {
    this.context = context;
    for (Entry<PropertyId<?>, PropertyId<?>.Value> entry : stringValues.entrySet()) {
      this.values.put(entry.getKey(), (entry.getValue() != null) ? entry.getValue().duplicate()
          : null);
    }

    this.instances.putAll(instanceValues);
    this.locations.putAll(locationsById);
  }

  @Nonnull
  public synchronized <T> T parse(@Nonnull PropertyId<T> propertyId) throws PropertyIdException {
    @SuppressWarnings({"unchecked", "rawtypes"})
    PropertyId<T>.Value value = (PropertyId.Value) values.get(propertyId);

    if (value == null) {
      throw new MissingPropertyException(propertyId);
    }

    try {
      value.check(context);
      return value.getObject(context);
    } catch (ParsingException e) {
      throw new PropertyIdException(propertyId, getLocation(propertyId), e);
    }
  }

  public synchronized <T, S> void check(@Nonnull KeyId<T, S> keyId) throws PropertyIdException {
    if (instances.get(keyId) == null) {
      if (keyId instanceof PropertyId) {
        @SuppressWarnings("unchecked")
        PropertyId<T> propertyId = (PropertyId<T>) keyId;

        @SuppressWarnings({"unchecked", "rawtypes"})
        PropertyId<T>.Value value = (PropertyId.Value) values.get(propertyId);

        if (value == null) {
          throw new MissingPropertyException(propertyId);
        }

        try {
          value.check(context);
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    PropertyId<T>.Value value = (Value) values.get(propertyId);

    if (value == null) {
      throw new MissingPropertyException(propertyId);
    }

    return value.getString();
  }

  @Nonnull
  public Map<KeyId<?, ?>, Object> getInstances() {
    return instances;
  }

  @Nonnull
  public Map<PropertyId<?>, PropertyId<?>.Value> getValues() {
    return values;
  }

  @Nonnull
  public Map<KeyId<?, ?>, String> getDropCauses() {
    return dropped;
  }

  @Nonnull
  public Location getLocation(@Nonnull KeyId<?, ?> keyId) {
    assert locations.get(keyId) != null;

    return locations.get(keyId);
  }

  public void remove(@Nonnull KeyId<?, ?> keyId, @Nonnull String cause) {
    values.remove(keyId);
    instances.remove(keyId);
    locations.remove(keyId);
    dropped.put(keyId, cause);
  }
}
