/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * Exception when the permissions of a file or directory cannot be changed.
 */
public class CannotChangePermissionException extends WithLocationIOException {

  /**
   * Represents whether the permission should be set or cleared.
   */
  public static enum SetOrClearPermission {
    SET, CLEAR;
  }

  private static final long serialVersionUID = 1L;

  private final int permission;
  @Nonnull
  private final ChangePermission change;
  @Nonnull
  private final SetOrClearPermission setOrClear;

  public CannotChangePermissionException(@Nonnull Location location, int permission,
      @Nonnull ChangePermission change, @Nonnull SetOrClearPermission setOrClear) {
    this(location, permission, change, setOrClear, null);
  }

  public CannotChangePermissionException(@Nonnull Location location, int permission,
      @Nonnull ChangePermission change, @Nonnull SetOrClearPermission setOrClear,
      @CheckForNull Throwable cause) {
    super(location, cause);
    this.permission = permission;
    this.change = change;
    this.setOrClear = setOrClear;
  }

  public CannotChangePermissionException(@Nonnull HasLocation locationProvider, int permission,
      @Nonnull ChangePermission change, @Nonnull SetOrClearPermission setOrClear) {
    this(locationProvider, permission, change, setOrClear, null);
  }

  public CannotChangePermissionException(@Nonnull HasLocation locationProvider, int permission,
      @Nonnull ChangePermission change, @Nonnull SetOrClearPermission setOrClear,
      @CheckForNull Throwable cause) {
    super(locationProvider, cause);
    this.permission = permission;
    this.change = change;
    this.setOrClear = setOrClear;
  }

  @Override
  protected String createMessage(@Nonnull String description) {
    return description + " cannot have its " +
      ((permission == Permission.READ)    ? "readable" :
      ((permission == Permission.WRITE)   ? "writable" :
      ((permission == Permission.EXECUTE) ? "executable" : "???"))) + " permission " +
      ((setOrClear == SetOrClearPermission.SET)    ? "set" :
      ((setOrClear == SetOrClearPermission.CLEAR)    ? "cleared" : "???")) +
      ((change == ChangePermission.EVERYBODY) ? " for everybody" : "");
  }
}
