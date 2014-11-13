/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

/**
 * A VFS directory backed by a real filesystem directory.
 */
public class DirectDir extends AbstractVElement implements InputOutputVDir {
  @Nonnull
  private final File dir;
  @Nonnull
  private final InputOutputVFS vfs;

  DirectDir(@Nonnull File dir, @Nonnull InputOutputVFS vfs)
      throws NotFileOrDirectoryException {
    if (!dir.isDirectory()) {
      throw new NotFileOrDirectoryException(new DirectoryLocation(dir));
    }
    this.dir = dir;
    this.vfs = vfs;
  }

  @Nonnull
  @Override
  public String getName() {
    return dir.getName();
  }

  @Nonnull
  @Override
  public synchronized Collection<? extends InputVElement> list() {
    File[] subs = dir.listFiles();
    if (subs == null) {
      throw new ConcurrentIOException(new ListDirException(dir));
    }
    if (subs.length == 0) {
      return Collections.emptyList();
    }

    ArrayList<InputVElement> items = new ArrayList<InputVElement>(subs.length);
    for (File sub : subs) {
      try {
        if (sub.isFile()) {
          items.add(new DirectFile(sub, vfs));
        } else {
          items.add(new DirectDir(sub, vfs));
        }
      } catch (NotFileOrDirectoryException e) {
        throw new ConcurrentIOException(e);
      }
    }

    return items;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new DirectoryLocation(dir);
  }

  @Override
  @Nonnull
  public InputVFile getInputVFile(@Nonnull VPath path) throws NotFileOrDirectoryException,
      NoSuchFileException {
    File file = new File(dir, path.getPathAsString(File.separatorChar));
    if (!file.exists()) {
      throw new NoSuchFileException(new FileLocation(file));
    }
    if (!file.isFile()) {
      throw new NotFileOrDirectoryException(new FileLocation(file));
    }
    return new DirectFile(file, vfs);
  }

  @Override
  @Nonnull
  public InputVDir getInputVDir(@Nonnull VPath path) throws NotFileOrDirectoryException,
      NoSuchFileException {
    File file = new File(dir, path.getPathAsString(File.separatorChar));
    if (!file.exists()) {
      throw new NoSuchFileException(new FileLocation(file));
    }
    if (file.isFile()) {
      throw new NotFileOrDirectoryException(new FileLocation(file));
    }
    return new DirectDir(file, vfs);
  }

  @Override
  @Nonnull
  public OutputVFile createOutputVFile(@Nonnull VPath path) throws CannotCreateFileException {
    File file = new File(dir, path.getPathAsString(File.separatorChar));
    if (!file.getParentFile().mkdirs() && !file.getParentFile().isDirectory()) {
      throw new CannotCreateFileException(new DirectoryLocation(file.getParentFile()));
    }
    return new DirectFile(file, vfs);
  }

  @Override
  public boolean isVDir() {
    return true;
  }
}
