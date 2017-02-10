/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack;

import com.android.jack.reporting.Reportable;
import com.android.jack.reporting.Reportable.ProblemLevel;
import com.android.sched.util.codec.EnumName;
import com.android.sched.util.codec.VariableName;

import javax.annotation.Nonnull;

/**
 * Jack verbosity level.
 * Note: The implementation of {@link ProblemLevel} assumes that the ordinal values of
 * {@link VerbosityLevel} are ordered from the highest severity to the lowest.
 */
@VariableName("level")
public enum VerbosityLevel {
  @EnumName(name = "error")
  ERROR("error"),
  @EnumName(name = "warning")
  WARNING("warning"),
  @EnumName(name = "info")
  INFO("info"),
  @EnumName(name = "debug", hide = true)
  @Deprecated DEBUG("debug"),
  @EnumName(name = "trace", hide = true)
  @Deprecated TRACE("trace");

  @Nonnull
  private final String id;

  VerbosityLevel(@Nonnull String id) {
    this.id = id;
  }

  @Nonnull
  public String getId() {
    return id;
  }

  static class DeprecatedVerbosity implements Reportable {
    @Nonnull
    private final VerbosityLevel verbosity;

    DeprecatedVerbosity(@Nonnull VerbosityLevel verbosity) {
      this.verbosity = verbosity;
    }

    @Override
    @Nonnull
    public String getMessage() {
      return "Verbosity level '" + verbosity.name().toLowerCase() + "' is deprecated";
    }

    @Override
    @Nonnull
    public ProblemLevel getDefaultProblemLevel() {
      return ProblemLevel.WARNING;
    }

  }
}