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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Exception describing a problem when parsing environment variables to set {@code PropertyId}.
 */
public class VariableConfigurationException extends ConfigurationException {
  private static final long serialVersionUID = 1L;

  @Nonnegative
  private final String variable;

  public VariableConfigurationException(@Nonnull String variable, @Nonnull String message) {
    super(message);
    this.variable = variable;
  }

  public VariableConfigurationException(
      @Nonnull String variable, @Nonnull ChainedException causes) {
    this(variable, (Throwable) causes);

    ChainedException nextCause = causes.getNextException();

    if (nextCause != null) {
      new VariableConfigurationException(variable, nextCause).putAsLastExceptionOf(this);
    }
  }

  public VariableConfigurationException(@Nonnull String variable, @Nonnull Throwable cause) {
    super("Environment variable '" + variable + "': " + cause.getMessage(), cause);
    this.variable = variable;
  }


  public VariableConfigurationException(
      @Nonnull String variable, @Nonnull String message, @Nonnull ChainedException causes) {
    this(variable, (Throwable) causes);

    ChainedException nextCause = causes.getNextException();

    if (nextCause != null) {
      new VariableConfigurationException(variable, message, nextCause).putAsLastExceptionOf(this);
    }
  }

  public VariableConfigurationException(
      @Nonnull String variable, @Nonnull String message, @Nonnull Throwable cause) {
    super(message, cause);
    this.variable = variable;
  }

  @Nonnull
  public String getVariableName() {
    return variable;
  }
}
