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

import com.android.sched.util.codec.CheckingException;
import com.android.sched.util.codec.ParsingException;

import javax.annotation.Nonnull;

/**
 * This exception is thrown if something is wrong with the static configuration (default value,
 * embedded properties file, ...)
 */
public class ConfigurationError extends Error {

  private static final long serialVersionUID = 1L;

  public ConfigurationError() {
    super();
  }

  public ConfigurationError(@Nonnull String message) {
    super(message);
  }

  public ConfigurationError(@Nonnull String message, @Nonnull Throwable cause) {
    super(message, cause);
  }

  public ConfigurationError(@Nonnull ParsingException e) {
    super(e.getMessage(), e);
  }

  public ConfigurationError(@Nonnull CheckingException e) {
    super(e.getMessage(), e);
  }
}
