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

package com.android.sched.util.file;

import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * An I/O exception that occurs when closing an output.
 */
public class CannotCloseOutputException extends WithLocationIOException {

  private static final long serialVersionUID = 1L;

  public CannotCloseOutputException(@Nonnull HasLocation locationProvider,
      @CheckForNull Exception cause) {
    super(locationProvider, cause);
  }

  public CannotCloseOutputException(@Nonnull Location location,
      @CheckForNull Exception cause) {
    super(location, cause);
  }

  @Override
  @Nonnull
  protected String createMessage(@Nonnull String description) {
    String message = "failed to close output " + description;
    if (getCause() != null) {
      message += ": " + getCause().getMessage();
    }
    return message;
  }

}
