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

import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;

import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A base implementation of a {@link VFile}.
 */
abstract class BaseVFile extends BaseVElement implements VFile {
  public BaseVFile(@Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> vfs,
      @Nonnull String name) {
    super(vfs, name);
  }

  @Override
  @Nonnull
  public InputStream getInputStream() throws WrongPermissionException {
    return vfs.openRead(this);
  }

  @Override
  public boolean isVDir() {
    return false;
  }

  @Override
  @Nonnull
  public OutputStream getOutputStream() throws WrongPermissionException {
    return getOutputStream(false);
  }

  @Override
  @Nonnull
  public OutputStream getOutputStream(boolean append) throws WrongPermissionException {
    return vfs.openWrite(this, append);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return vfs.getVFileLocation(this);
  }

  @Override
  @CheckForNull
  public String getDigest() {
    return null;
  }

  @Override
  public long getLastModified() {
    return vfs.getLastModified(this);
  }

  @Override
  public void delete() throws CannotDeleteFileException {
    vfs.delete(this);
  }

  @Override
  @Nonnull
  public VPath getPathFromRoot() {
    return vfs.getPathFromRoot(this);
  }

  @Override
  @Nonnull
  public String toString() {
    return getPathFromRoot().getPathAsString('/') + " (" + getLocation().getDescription() + ')';
  }
}
