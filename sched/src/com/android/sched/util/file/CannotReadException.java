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

package com.android.sched.util.file;

import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Exception when a problem occurs while reading.
 */
public class CannotReadException extends WithLocationIOException {
  private static final long serialVersionUID = 1L;

  public CannotReadException(@Nonnull Location location) {
    super(location, null);
  }

  public CannotReadException(@Nonnull Location location,
      @CheckForNull Throwable cause) {
    super(location, cause);
  }

  public CannotReadException(@Nonnull HasLocation locationProvider) {
    super(locationProvider, null);
  }

  public CannotReadException(@Nonnull HasLocation locationProvider,
      @CheckForNull Throwable cause) {
    super(locationProvider, cause);
  }

  @Override
  protected String createMessage(@Nonnull String description) {
    String message = description + " failed to be read";
    Throwable cause = getCause();
    if (cause != null) {
      String detail = cause.getMessage();
      if (detail != null) {
        message += ": " + detail;
      }
    }
    return message;
  }
}
