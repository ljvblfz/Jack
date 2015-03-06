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

package com.android.jack.reporting;

import com.android.jack.Options.VerbosityLevel;

import javax.annotation.Nonnull;

/**
 * An object that can be reported by a {@link Reporter}.
 */
public interface Reportable {

  /**
   * The level of a problem.
   * The implementation assumes that the ordinal values of {@link VerbosityLevel} are ordered from
   * the highest severity to the lowest.
   */
  public static enum ProblemLevel {
    ERROR(VerbosityLevel.ERROR), WARNING(VerbosityLevel.WARNING), INFO(VerbosityLevel.INFO);

    @Nonnull
    private final VerbosityLevel verbosityLevelThreshold;

    ProblemLevel(@Nonnull VerbosityLevel verbosityLevelThreshold) {
      this.verbosityLevelThreshold = verbosityLevelThreshold;
    }

    public boolean isVisibleWith(@Nonnull VerbosityLevel verbosityLevel) {
      return verbosityLevel.ordinal() >= verbosityLevelThreshold.ordinal();
    }
  }

  @Nonnull
  public String getMessage();

  @Nonnull
  public ProblemLevel getDefaultProblemLevel();

}
