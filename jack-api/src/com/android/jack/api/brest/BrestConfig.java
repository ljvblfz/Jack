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

package com.android.jack.api.brest;

import com.android.jack.api.JackConfig;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * STOPSHIP
 */
public interface BrestConfig extends JackConfig {
  @Nonnull
  static final String PROPERTY_REPORTER = "jack.reporter";

  @Nonnull
  BrestConfig setOutputDex(@Nonnull File file) throws ConfigurationException;

  @Nonnull
  BrestConfig setOutputJack(@Nonnull File file) throws ConfigurationException;

  @Nonnull
  BrestConfig setConfigJarjar(@Nonnull File file) throws ConfigurationException;

  @Nonnull
  BrestConfig setProperty(@Nonnull String key, @Nonnull String value) throws ConfigurationException;

  @Nonnull
  BrestCompiler build() throws ConfigurationException;
}
