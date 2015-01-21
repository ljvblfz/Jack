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

import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.location.Location;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * A base implementation of a {@link VDir}.
 */
abstract class BaseVDir extends BaseVElement implements VDir {
  public BaseVDir(@Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> vfs,
      @Nonnull String name) {
    super(vfs, name);
  }

  @Override
  @Nonnull
  public BaseVDir getVDir(@Nonnull String name) throws NotDirectoryException,
      NoSuchFileException {
    return vfs.getVDir(this, name);
  }

  @Override
  @Nonnull
  public BaseVDir getVDir(@Nonnull VPath path) throws NotDirectoryException,
      NoSuchFileException {
    BaseVDir dir = this;
    for (String name : path.split()) {
      dir = dir.getVDir(name);
    }

    return dir;
  }

  @Override
  @Nonnull
  public BaseVFile getVFile(@Nonnull String name) throws NoSuchFileException, NotFileException {
    return vfs.getVFile(this, name);
  }

  @Override
  @Nonnull
  public BaseVFile getVFile(@Nonnull VPath path) throws NoSuchFileException,
      NotDirectoryException, NotFileException {
    BaseVDir  dir = this;

    Iterator<String> iter = path.split().iterator();
    String name;

    while (iter.hasNext()) {
      name = iter.next();
      if (iter.hasNext()) {
        dir = dir.getVDir(name);
      } else {
        return dir.getVFile(name);
      }
    }

    // Path is empty
    throw new AssertionError();
  }

  @Override
  @Nonnull
  public void delete(@Nonnull VFile file) throws CannotDeleteFileException {
    vfs.delete((BaseVFile) file);
  }

  @Override
  @Nonnull
  public BaseVDir createVDir(@Nonnull VPath path) throws CannotCreateFileException {
    BaseVDir dir = this;
    for (String name : path.split()) {
      dir = dir.createVDir(name);
    }

    return dir;
  }

  @Override
  @Nonnull
  public BaseVFile createVFile(@Nonnull VPath path) throws CannotCreateFileException {
    BaseVDir dir = this;

    Iterator<String> iter = path.split().iterator();
    String name;

    while (iter.hasNext()) {
      name = iter.next();
      if (iter.hasNext()) {
        dir = dir.createVDir(name);
      } else {
        return dir.createVFile(name);
      }
    }

    // Path is empty
    throw new AssertionError();
  }

  @Override
  @Nonnull
  public Collection<? extends BaseVElement> list() {
    return vfs.list(this);
  }

  @Override
  public boolean isVDir() {
    return true;
  }

  @Override
  @Nonnull
  public String getName() {
    return name;
  }

  @Override
  @Nonnull
  public BaseVDir createVDir(@Nonnull String name) throws CannotCreateFileException {
    return vfs.createVDir(this, name);
  }

  @Override
  @Nonnull
  public BaseVFile createVFile(@Nonnull String name) throws CannotCreateFileException {
    return vfs.createVFile(this, name);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return vfs.getVDirLocation(this);
  }

  @Nonnull
  public Location getVDirLocation(@Nonnull VPath path) {
    return vfs.getVDirLocation(this, path);
  }

  @Nonnull
  public Location getVFileLocation(@Nonnull VPath path) {
    return vfs.getVFileLocation(this, path);
  }

  @Nonnull
  public Location getVDirLocation(@Nonnull String name) {
    return vfs.getVDirLocation(this, name);
  }

  @Nonnull
  public Location getVFileLocation(@Nonnull String name) {
    return vfs.getVFileLocation(this, name);
  }
}
