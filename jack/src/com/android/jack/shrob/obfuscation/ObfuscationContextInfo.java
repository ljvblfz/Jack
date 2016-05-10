/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation;

import com.android.jack.reporting.Reportable;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * A {@link Reportable} information that occurs during the obfuscation phase.
 */
public class ObfuscationContextInfo implements Reportable {

  @Nonnull
  private final Location location;

  @Nonnull
  private final ProblemLevel level;

  @Nonnull
  private final Throwable cause;

  public ObfuscationContextInfo(
      @Nonnull Location location, @Nonnull ProblemLevel level, @Nonnull Throwable cause) {
    this.location = location;
    this.cause = cause;
    this.level = level;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return location.getDescription() + ": Obfuscation: " + cause.getMessage();
  }

  @Override
  @Nonnull
  public ProblemLevel getDefaultProblemLevel() {
    return level;
  }

}
