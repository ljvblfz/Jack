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

import com.android.sched.util.config.id.PropertyId;
import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Exception describing a problem on a {@link PropertyId}.
 */
public class PropertyIdException extends ConfigurationException implements HasLocation {
  private static final long serialVersionUID = 1L;
  @Nonnull
  protected static final Location NO_LOCATION = new NoLocation();

  @Nonnull
  private final Location location;

  @Nonnull
  private final PropertyId<?> propertyId;

  public PropertyIdException(
      @Nonnull PropertyId<?> propertyId, @Nonnull Location location, @Nonnull String message) {
    super(message);
    this.propertyId = propertyId;
    this.location = location;
  }

  public PropertyIdException(@Nonnull PropertyId<?> propertyId, @Nonnull Location location,
      @Nonnull ChainedException causes) {
    this(propertyId, location, (Throwable) causes);

    Iterator<ChainedException> iter = causes.iterator();
    iter.next();
    while (iter.hasNext()) {
      new PropertyIdException(propertyId, location, iter.next()).putAsLastExceptionOf(this);
    }
  }

  public PropertyIdException(
      @Nonnull PropertyId<?> propertyId, @Nonnull Location location, @Nonnull Throwable cause) {
    super("", cause);

    this.propertyId = propertyId;
    this.location = location;

    setMessage("Property '" + propertyId.getName() + "'" + getDetails(location) + ": "
        + cause.getMessage());
  }

  public PropertyIdException(@Nonnull PropertyId<?> propertyId, @Nonnull Location location,
      @Nonnull String message, @Nonnull ChainedException causes) {
    this(propertyId, location, message, (Throwable) causes);

    Iterator<ChainedException> iter = causes.iterator();
    iter.next();
    while (iter.hasNext()) {
      new PropertyIdException(propertyId, location, message, iter.next()).putAsLastExceptionOf(
          this);
    }
  }

  public PropertyIdException(@Nonnull PropertyId<?> propertyId, @Nonnull Location location,
      @Nonnull String message, @Nonnull Throwable cause) {
    super(message, cause);
    this.propertyId = propertyId;
    this.location = location;
  }

  @Nonnull
  private String getDetails(@Nonnull Location location) {
    String result = location.getDescription();
    if (!result.isEmpty()) {
      result = " (in " + result + ")";
    }

    return result;
  }

  @Nonnull
  public PropertyId<?> getPropertyId() {
    return propertyId;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return location;
  }
}
