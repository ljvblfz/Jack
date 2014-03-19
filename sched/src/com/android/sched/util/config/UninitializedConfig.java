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

import com.android.sched.util.config.id.KeyId;
import com.android.sched.util.config.id.PropertyId;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * This object represents a {@link Config} which has not been created by the builder.
 */
class UninitializedConfig implements Config {

  @Override
  @Nonnull
  public <T, S> T get(@Nonnull KeyId<T, S> keyId) {
    throw new ConfigurationError("Configuration has not been initialized");
  }

  @Override
  @Nonnull
  public <T> String getAsString(@Nonnull PropertyId<T> propertyId) {
    throw new ConfigurationError("Configuration has not been initialized");
  }

  @Override
  @Nonnull
  public Collection<PropertyId<?>> getPropertyIds() {
    throw new ConfigurationError("Configuration has not been initialized");
  }
}
