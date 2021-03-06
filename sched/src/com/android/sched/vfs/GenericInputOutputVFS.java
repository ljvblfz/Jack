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

import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.location.Location;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link InputOutputVFS} which take a {@link VFS}.
 */
public class GenericInputOutputVFS extends AbstractVFS implements InputOutputVFS {
  @Nonnull
  final VFS vfs;

  public GenericInputOutputVFS(@Nonnull VFS vfs) {
    this.vfs  = vfs;
  }

  @Override
  @Nonnull
  public InputOutputVDir getRootDir() {
    return new GenericInputOutputVDir(vfs.getRootDir());
  }

  @Override
  @Nonnull
  public String getPath() {
    return vfs.getPath();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return vfs.getLocation();
  }

  @Override
  public void close() throws CannotCloseException {
    vfs.close();
  }

  @Override
  public boolean needsSequentialWriting() {
    return vfs.needsSequentialWriting();
  }

  @Override
  @CheckForNull
  public String getDigest() {
    return vfs.getDigest();
  }

  @Override
  public boolean isClosed() {
    return vfs.isClosed();
  }

  @Override
  @Nonnull
  public VFS getVFS() {
    return vfs;
  }

  @Override
  @Nonnull
  public String toString() {
    return "ioFS >> " + vfs.toString();
  }
}
