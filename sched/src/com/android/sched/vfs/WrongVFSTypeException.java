/*
 * Copyright (C) 2017 The Android Open Source Project
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
 * {@link Exception} when a {@link VFS} does not match the expected VFS type, but may be a valid VFS
 * of another type.
 */
public class WrongVFSTypeException extends SchedIOException {
  private static final long serialVersionUID = 1L;

  @Nonnull
  private final VFS vfs;
  @Nonnull
  private final String expectedVfsDescription;

  public WrongVFSTypeException(@Nonnull VFS vfs, @Nonnull Location location,
      @Nonnull String expectedVfsDescription, @Nonnull Throwable cause) {
    super(location, cause);
    this.vfs = vfs;
    this.expectedVfsDescription = expectedVfsDescription;
  }

  public WrongVFSTypeException(@Nonnull VFS vfs, @Nonnull Location location,
      @Nonnull String expectedVfsDescription) {
    super(location, null);
    this.vfs = vfs;
    this.expectedVfsDescription = expectedVfsDescription;
  }

  @Override
  @Nonnull
  protected String createMessage(@Nonnull String description) {
    return "'"
        + vfs.getDescription()
        + "' VFS in "
        + description
        + " does not match the expected VFS type '"
        + expectedVfsDescription
        + "': "
        + getCause().getMessage();
  }
}
