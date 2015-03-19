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

import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Exception when a file or directory can not be cleared of the expected permission.
 */
public class CannotUnsetPermissionException extends WithLocationException {
  private static final long serialVersionUID = 1L;

  private final int permission;
  @Nonnull
  private final ChangePermission change;

  public CannotUnsetPermissionException(@Nonnull Location location, int permission,
      @Nonnull ChangePermission change) {
    this(location, permission, change, null);
  }

  public CannotUnsetPermissionException(@Nonnull Location location, int permission,
      @Nonnull ChangePermission change, @CheckForNull Throwable cause) {
    super(location, cause);
    this.permission = permission;
    this.change = change;
  }

  public CannotUnsetPermissionException(@Nonnull HasLocation locationProvider, int permission,
      @Nonnull ChangePermission change) {
    this(locationProvider, permission, change, null);
  }

  public CannotUnsetPermissionException(@Nonnull HasLocation locationProvider, int permission,
      @Nonnull ChangePermission change, @CheckForNull Throwable cause) {
    super(locationProvider, cause);
    this.permission = permission;
    this.change = change;
  }

  @Override
  @Nonnull
  protected String createMessage(@Nonnull String description) {
    return description + " cannot have its " +
        ((permission == Permission.READ)    ? "readable " :
        ((permission == Permission.WRITE)   ? "writable " :
        ((permission == Permission.EXECUTE) ? "executable " : "???"))) + "permission removed" +
        ((change == ChangePermission.EVERYBODY) ? " for everybody" : "");
  }
}
