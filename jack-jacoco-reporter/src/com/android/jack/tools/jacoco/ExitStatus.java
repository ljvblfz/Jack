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

package com.android.jack.tools.jacoco;

import javax.annotation.Nonnegative;

/**
 * An enumeration of possible exit status returned by the reporter.
 */
public enum ExitStatus {
  /**
   * Successful execution without any error.
   */
  SUCCESS(0),

  /**
   * Usage error from the user (incorrect argument, missing file, ...).
   */
  USAGE_ERROR(1),

  /**
   * Internal error.
   */
  INTERNAL_ERROR(2);

  @Nonnegative
  private final int exitStatusCode;

  private ExitStatus(@Nonnegative int exitStatusCode) {
    assert exitStatusCode > 0;
    this.exitStatusCode = exitStatusCode;
  }

  /**
   * Returns the exit status code used in {@link System#exit(int)}.
   *
   * @return the exit status code.
   */
  @Nonnegative
  public int getExitStatus() {
    return exitStatusCode;
  }
}

