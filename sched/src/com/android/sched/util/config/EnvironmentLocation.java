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

import javax.annotation.Nonnull;

/**
 * Class describing a environment variable.
 */
public class EnvironmentLocation extends Location {
  @Nonnull
  private final String name;

  public EnvironmentLocation(@Nonnull String name) {
    this.name = name;
  }

  @Nonnull
  public String getVariableName() {
    return name;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "environment variable '" + name + "'";
  }

  @Override
  public final boolean equals(Object obj) {
    return obj instanceof EnvironmentLocation
        && ((EnvironmentLocation) obj).name.equals(name);
  }

  @Override
  public final int hashCode() {
    return name.hashCode();
  }
}
