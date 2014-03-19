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

import javax.annotation.Nonnull;

/**
 * Exception describing that a {@link PropertyId} is not registered in the system.
 */
public class UnknownPropertyIdException extends PropertyIdException {
  private static final long serialVersionUID = 1L;

  public UnknownPropertyIdException(@Nonnull PropertyId<?> propertyId) {
    super(propertyId, NO_LOCATION, "Property '" + propertyId.getName()
        + "' is unknown (see annotation @" + HasKeyId.class.getSimpleName() + ")");
  }

  public UnknownPropertyIdException(@Nonnull PropertyId<?> propertyId, @Nonnull String message) {
    super(propertyId, NO_LOCATION, message);
  }
}
