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

import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * An Exception designed to wrap a {@link java.util.zip.ZipException} while adding {@link Location}
 * info.
 */
public class ZipException extends SchedIOException {
  private static final long serialVersionUID = 1L;

  @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION") // we don't want cause to be null
  public ZipException(@Nonnull Location location, @Nonnull java.util.zip.ZipException exception) {
    super(location, exception);
  }

  @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION") // we don't want cause to be null
  public ZipException(@Nonnull HasLocation locationProvider,
      @Nonnull java.util.zip.ZipException exception) {
    super(locationProvider, exception);
  }

  @Override
  @Nonnull
  protected String createMessage(@Nonnull String description) {
    return description + ": " + getCause().getMessage();
  }

}
