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

package com.android.sched.util.config.expression;

import com.android.sched.util.config.id.PropertyId;

import javax.annotation.Nonnull;

/**
 * Exception describing that a {@link PropertyId} is not required.
 */
public class PropertyNotRequiredException extends Exception {
  private static final long serialVersionUID = 1L;

  @Nonnull
  private final PropertyId<?> propertyId;

  public PropertyNotRequiredException(@Nonnull PropertyId<?> propertyId) {
    super("Property '" + propertyId.getName() + "' is not required");
    this.propertyId = propertyId;
  }

  public PropertyNotRequiredException(@Nonnull PropertyId<?> propertyId, @Nonnull String message) {
    super(message);
    this.propertyId = propertyId;
  }

  public PropertyNotRequiredException(@Nonnull PropertyId<?> propertyId, @Nonnull Throwable cause) {
    super("Property '" + propertyId.getName() + "' is not required", cause);
    this.propertyId = propertyId;
  }

  @Nonnull
  public PropertyId<?> getPropertyId() {
    return propertyId;
  }
}
