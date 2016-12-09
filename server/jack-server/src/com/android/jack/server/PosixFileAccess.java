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

package com.android.jack.server;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

class PosixFileAccess extends FileAccess {

  @Nonnull
  private final PosixFileAttributeView view;

  public PosixFileAccess(@Nonnull Path path, @Nonnull PosixFileAttributeView posixView) {
    super(path);
    this.view = posixView;
  }

  @Override
  public void removeAccessRightButOwner() throws IOException {
    final Set<PosixFilePermission> permissions = view.readAttributes().permissions();
    permissions.removeAll(
        EnumSet.of(
        PosixFilePermission.GROUP_READ,
        PosixFilePermission.GROUP_WRITE,
        PosixFilePermission.GROUP_EXECUTE,
        PosixFilePermission.OTHERS_READ,
        PosixFilePermission.OTHERS_WRITE,
        PosixFilePermission.OTHERS_EXECUTE));
    view.setPermissions(permissions);
  }

  @Override
  public void checkAccessibleOnlyByOwner() throws IOException {
    Set<PosixFilePermission> check = EnumSet.of(PosixFilePermission.OWNER_READ,
        PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);
    Set<PosixFilePermission> invalidPerimissions = view.readAttributes().permissions();
    invalidPerimissions.removeAll(check);
    if (invalidPerimissions.size() != 0) {
      throw new IOException("'" + getPath().toString() + "' has the following invalid permissions "
          + PosixFilePermissions.toString(invalidPerimissions));
    }
  }

  @Override
  @Nonnull
  public UserPrincipal getOwner() throws IOException {
    return view.getOwner();
  }

}
