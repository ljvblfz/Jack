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

package com.android.sched.util.codec;

import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Exception when a path is not a service jar file.
 */
public class NotServiceFileException extends NotFileOrDirectoryException {
  private static final long serialVersionUID = 1L;

  @Nonnull
  private final Class<?> type;

  public NotServiceFileException(@Nonnull Location location, @Nonnull Class<?> type) {
    super(location, null);
    this.type = type;
  }

  public NotServiceFileException(@Nonnull Location location, @Nonnull Class<?> type,
      @CheckForNull Throwable cause) {
    super(location, cause);
    this.type = type;
  }

  public NotServiceFileException(@Nonnull HasLocation locationProvider, @Nonnull Class<?> type) {
    super(locationProvider, null);
    this.type = type;
  }

  public NotServiceFileException(@Nonnull HasLocation location, @Nonnull Class<?> type,
      @CheckForNull Throwable cause) {
    super(location, cause);
    this.type = type;
  }

  @Nonnull
  public Class<?> getServiceType() {
    return type;
  }

  @Override
  protected String createMessage(@Nonnull String description) {
    return description + " is not a '" + type.getCanonicalName() + "' service jar file";
  }
}
