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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;

import javax.annotation.Nonnull;

abstract class FileAccess {

  @Nonnull
  public static FileAccess get(@Nonnull Path path) throws IOException {
    PosixFileAttributeView posixView =
        Files.getFileAttributeView(path, PosixFileAttributeView.class);
    if (posixView != null) {
      return new PosixFileAccess(path, posixView);
    }

    AclFileAttributeView aclView =
        Files.getFileAttributeView(path, AclFileAttributeView.class);
    if (aclView != null) {
      return new AclFileAccess(path, aclView);
    }

    throw new UnsupportedFileSystemAccessException(path);
  }

  @Nonnull
  private final Path path;

  protected FileAccess(@Nonnull Path path) {
    this.path = path;
  }

  public abstract void removeAccessRightButOwner() throws IOException;

  public abstract void checkAccessibleOnlyByOwner() throws IOException;

  public void checkOwner(@Nonnull UserPrincipal expectedOwner) throws IOException {
    UserPrincipal effectiveOwner = getOwner();
    if (!expectedOwner.equals(effectiveOwner)) {
      throw new IOException("'" + path.toString() + "' is not owned by '" + expectedOwner
          + "' but by '" + effectiveOwner.getName() + "'");
    }
  }

  @Nonnull
  public abstract UserPrincipal getOwner() throws IOException;

  @Nonnull
  protected Path getPath() {
    return path;
  }
}
