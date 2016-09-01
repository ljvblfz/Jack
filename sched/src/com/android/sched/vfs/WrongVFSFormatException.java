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

package com.android.sched.vfs;

import com.android.sched.util.file.SchedIOException;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * Exception when a {@link VFS} has a wrong format.
 */
public class WrongVFSFormatException extends SchedIOException {
  private static final long serialVersionUID = 1L;

  @Nonnull
  private final VFS vfs;

  public WrongVFSFormatException(@Nonnull VFS vfs, @Nonnull Location location,
      @Nonnull Throwable cause) {
    super(location, cause);
    this.vfs = vfs;
  }

  @Override
  @Nonnull
  protected String createMessage(@Nonnull String description) {
    return "'" + vfs.getDescription() + "' VFS format in " + description
    + " is wrong: " + getCause().getMessage();
  }
}
