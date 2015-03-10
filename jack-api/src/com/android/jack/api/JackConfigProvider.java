/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.api;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Provides instances of {@link JackConfig}.
 */
public interface JackConfigProvider {
  @Nonnull
  static final String CLASS_NAME = "com.android.jack.api.impl.JackConfigProviderImpl";

  /**
   * Creates a {@link JackConfig} instance for an interface representing a {@link JackConfig} API
   * version.
   * @param cls the {@link JackConfig} API interface
   * @return the {@link JackConfig} instance
   * @throws ConfigNotSupportedException If no implementation is found for the given interface.
   */
  @Nonnull
  <T extends JackConfig> T getConfig(@Nonnull Class<T> cls) throws ConfigNotSupportedException;

  /**
   * Gives a {@link Collection} containing supported {@link JackConfig} API versions.
   * @return the supported {@link JackConfig} API versions
   */
  @Nonnull
  Collection<Class<? extends JackConfig>> getSupportedConfigs();

  /**
   * The code name of this Jack compiler.
   * @return the code name
   */
  @Nonnull
  String getCompilerCodeName();

  /**
   * The version of this Jack compiler.
   * @return the version
   */
  @Nonnull
  String getCompilerVersion();

  /**
   * The build ID of this Jack compiler.
   * @return the build ID
   */
  @Nonnull
  String getCompilerBuildId();

  /**
   * The code base of this Jack compiler.
   * @return the code base
   */
  @Nonnull
  String getCompilerCodeBase();
}
