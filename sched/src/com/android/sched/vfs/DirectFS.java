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

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.file.AbstractStreamFile;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A {@link VFS} implementation backed by a real file system.
 */
public class DirectFS extends BaseVFS<ParentVDir, ParentVFile> implements VFS {
  @Nonnull
  private final Directory  dir;
  @Nonnull
  private final ParentVDir root;
  @Nonnull
  private final Set<Capabilities> capabilities;

  public DirectFS(@Nonnull Directory dir, int permissions) {
    this.dir = dir;
    this.root = new ParentVDir(this, "");

    Set<Capabilities> capabilities = EnumSet.noneOf(Capabilities.class);
    if ((permissions & Permission.READ) != 0) {
      capabilities.add(Capabilities.READ);
      capabilities.add(Capabilities.PARALLEL_READ);
    }
    if ((permissions & Permission.WRITE) != 0) {
      capabilities.add(Capabilities.WRITE);
      capabilities.add(Capabilities.PARALLEL_WRITE);
    }
    this.capabilities = Collections.unmodifiableSet(capabilities);
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "directory on disk";
  }

  @Nonnull
  @Override
  public Set<Capabilities> getCapabilities() {
    return capabilities;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return dir.getLocation();
  }

  @Override
  public synchronized void close() {
    closed = true;
  }

  @Override
  @Nonnull
  public String getPath() {
    return dir.getPath();
  }

  @Override
  public ParentVDir getRootDir() {
    return root;
  }

  @Override
  @Nonnull
  InputStream openRead(@Nonnull ParentVFile file) throws WrongPermissionException {
    assert !isClosed();
    assert capabilities.contains(Capabilities.READ);

    File path = getNativeFile(file.getPath());
    try {
      return new FileInputStream(path);
    } catch (FileNotFoundException e) {
      FileOrDirectory.checkPermissions(path, file.getLocation(), Permission.READ);
      throw new ConcurrentIOException(e);
    }
  }

  @Nonnull
  @Override
  OutputStream openWrite(@Nonnull ParentVFile file) throws WrongPermissionException {
    assert !isClosed();
    assert capabilities.contains(Capabilities.WRITE);

    File path = getNativeFile(file.getPath());
    try {
      return new FileOutputStream(path);
    } catch (FileNotFoundException e) {
      FileOrDirectory.checkPermissions(path, file.getLocation(), Permission.WRITE);
      throw new ConcurrentIOException(e);
    }
  }

  @Nonnull
  @Override
  Collection<? extends BaseVElement> list(@Nonnull ParentVDir dir) {
    assert !isClosed();
    assert capabilities.contains(Capabilities.READ);

    File path = getNativeFile(dir.getPath());
    File[] subs = path.listFiles();
    if (subs == null) {
      throw new ConcurrentIOException(new ListDirException(path));
    }
    if (subs.length == 0) {
      return Collections.emptyList();
    }

    ArrayList<BaseVElement> items = new ArrayList<BaseVElement>(subs.length);
    for (File sub : subs) {
      if (sub.isFile()) {
        items.add(new ParentVFile(this, dir, sub.getName()));
      } else {
        items.add(new ParentVDir(this, dir, sub.getName()));
      }
    }

    return items;
  }

  @Override
  boolean isEmpty(@Nonnull ParentVDir dir) {
    assert !isClosed();
    assert capabilities.contains(Capabilities.READ);

    return getNativeFile(dir.getPath()).listFiles().length == 0;
  }

  @Override
  @Nonnull
  ParentVFile createVFile(@Nonnull ParentVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    assert !isClosed();
    assert capabilities.contains(Capabilities.WRITE);

    File path = getNativeFile(parent.getPath(), name);
    try {
      AbstractStreamFile.create(path, new FileLocation(path));
    } catch (FileAlreadyExistsException e) {
      // Nothing to do
    }

    return new ParentVFile(this, parent, name);
  }

  @Override
  @Nonnull
  ParentVDir getVDir(@Nonnull ParentVDir parent, @Nonnull String name)
      throws NotDirectoryException, NoSuchFileException {
    assert !isClosed();
    assert capabilities.contains(Capabilities.READ);

    File path = getNativeFile(parent.getPath(), name);
    Directory.check(path, new DirectoryLocation(path));

    return new ParentVDir(this, parent, name);
  }

  @Override
  @Nonnull
  ParentVFile getVFile(@Nonnull ParentVDir parent, @Nonnull String name)
      throws NotFileException, NoSuchFileException {
    assert !isClosed();
    assert capabilities.contains(Capabilities.READ);

    File path = getNativeFile(parent.getPath(), name);
    AbstractStreamFile.check(path, new FileLocation(path));

    return new ParentVFile(this, parent, name);
  }

  @Override
  @Nonnull
  void delete(@Nonnull ParentVFile file) throws CannotDeleteFileException {
    assert !isClosed();
    assert capabilities.contains(Capabilities.WRITE);

    File path = getNativeFile(file.getPath());
    if (!path.delete() || path.exists()) {
      throw new CannotDeleteFileException(file.getLocation());
    }
  }

  @Override
  @Nonnull
  ParentVDir createVDir(@Nonnull ParentVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    assert !isClosed();
    assert capabilities.contains(Capabilities.WRITE);

    File path = getNativeFile(parent.getPath(), name);
    try {
      Directory.create(path, new DirectoryLocation(path));
    } catch (FileAlreadyExistsException e) {
      // Nothing to do
    }

    return new ParentVDir(this, parent, name);
  }

  @Override
  public boolean needsSequentialWriting() {
    return false;
  }

  @Override
  synchronized boolean isClosed() {
    return closed;
  }

  @Override
  @Nonnull
  FileLocation getVFileLocation(@Nonnull ParentVFile file) {
    return new FileLocation(getNativeFile(file.getPath()));
  }

  @Override
  @Nonnull
  FileLocation getVFileLocation(@Nonnull ParentVDir parent, @Nonnull String name) {
    return new FileLocation(getNativeFile(parent.getPath(), name));
  }

  @Override
  @Nonnull
  DirectoryLocation getVDirLocation(@Nonnull ParentVDir dir) {
    return new DirectoryLocation(getNativeFile(dir.getPath()));
  }

  @Override
  @Nonnull
  DirectoryLocation getVDirLocation(@Nonnull ParentVDir parent, @Nonnull String name) {
    return new DirectoryLocation(getNativeFile(parent.getPath(), name));
  }

  @Override
  @Nonnull
  FileLocation getVFileLocation(ParentVDir parent, VPath path) {
    return new FileLocation(getNativeFile(parent.getPath().clone().appendPath(path)));
  }

  @Override
  @Nonnull
  DirectoryLocation getVDirLocation(ParentVDir parent, VPath path) {
    return new DirectoryLocation(getNativeFile(parent.getPath().clone().appendPath(path)));
  }

  @Nonnull
  private File getNativeFile(@Nonnull VPath path) {
    return new File(dir.getFile(), path.getPathAsString(File.separatorChar));
  }

  @Nonnull
  private File getNativeFile(@Nonnull VPath path, @Nonnull String name) {
    return new File(new File(dir.getFile(), path.getPathAsString(File.separatorChar)), name);
  }
}
