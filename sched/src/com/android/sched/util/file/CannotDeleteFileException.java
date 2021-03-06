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
 * Exception thrown when deletion of a file or a directory fails.
 */
public class CannotDeleteFileException extends SchedIOException {
  private static final long serialVersionUID = 1L;

  public CannotDeleteFileException(@Nonnull Location location) {
    super(location, null);
  }

  public CannotDeleteFileException(@Nonnull Location location,
      @CheckForNull Throwable cause) {
    super(location, cause);
  }

  public CannotDeleteFileException(@Nonnull HasLocation locationProvider) {
    super(locationProvider, null);
  }

  public CannotDeleteFileException(@Nonnull HasLocation locationProvider,
      @CheckForNull Throwable cause) {
    super(locationProvider, cause);
  }

  @Override
  protected String createMessage(@Nonnull String description) {
    return description + " can not be deleted";
  }
}
