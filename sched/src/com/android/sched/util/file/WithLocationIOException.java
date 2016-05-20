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

package com.android.sched.util.file;

import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Common superclass for IOExceptions which are related to a specific location.
 */
public abstract class WithLocationIOException extends IOException {
  private static final long serialVersionUID = 1L;

  @CheckForNull
  private final Location location;
  @CheckForNull
  private final HasLocation locationProvider;

  @Nonnull
  protected abstract String createMessage(@Nonnull String description);

  protected WithLocationIOException(@Nonnull Location location, @CheckForNull Throwable cause) {
    super("", cause);
    assert location != null;
    this.location = location;
    this.locationProvider = null;
  }

  protected WithLocationIOException(@Nonnull HasLocation locationProvider,
      @CheckForNull Throwable cause) {
    super("", cause);
    assert locationProvider != null;
    this.location = null;
    this.locationProvider = locationProvider;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return createMessage(getLocation().getDescription());
  }

  @Nonnull
  public Location getLocation() {
    if (location != null) {
      return location;
    }

    if (locationProvider != null) {
      return locationProvider.getLocation();
    }

    throw new AssertionError();
  }
}
