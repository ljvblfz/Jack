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
import com.android.sched.vfs.DirectFS.DirectVDir;
import com.android.sched.vfs.DirectFS.DirectVFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

/**
 * A {@link VFS} implementation backed by a real file system.
 */
public class DirectFS extends BaseVFS<DirectVDir, DirectVFile> implements VFS {
  @Nonnull
  private final DirectVDir root;

  private final int permissions;

  /**
   * A {@link VDir} base implementation for a {@link DirectFS}.
   */
  public abstract static class DirectVDir extends BaseVDir {
    private DirectVDir(BaseVFS<? extends BaseVDir, ? extends BaseVFile> vfs, String name) {
      super(vfs, name);
    }

    @Nonnull
    abstract File getNativePath();
  }

  /**
   * A {@link VDir} implementation for non-root directory of a {@link DirectFS}.
   */
  public static class DirectNonRootVDir extends DirectVDir {
    @Nonnull
    protected final VDir parent;

    private DirectNonRootVDir(@Nonnull DirectFS vfs, @Nonnull VDir parent, @Nonnull String name) {
      super(vfs, name);
      this.parent = parent;
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return new DirectoryLocation(getNativePath());
    }

    @Override
    @Nonnull
    File getNativePath() {
      assert parent != null;

      return new File(((DirectVDir) parent).getNativePath(), name);
    }

    @Override
    @Nonnull
    public VPath getPath() {
      return new VPath(getNativePath().getPath(), File.separatorChar);
    }
  }

  /**
   * A {@link VDir} implementation for root directory of a {@link DirectFS}.
   */
  public static class DirectRootVDir extends DirectVDir {
    @Nonnull
    private final Directory dir;

    private DirectRootVDir(@Nonnull DirectFS vfs, @Nonnull Directory dir) {
      super(vfs, "");
      this.dir = dir;
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return dir.getLocation();
    }

    @Override
    @Nonnull
    File getNativePath() {
      return dir.getFile();
    }

    @Override
    @Nonnull
    public VPath getPath() {
      return new VPath(getNativePath().getPath(), File.separatorChar);
    }
  }

  /**
   * A {@link VFile} implementation for a {@link DirectFS}.
   */
  public static class DirectVFile extends BaseVFile {
    @Nonnull
    protected final VDir parent;

    private DirectVFile(@Nonnull DirectFS vfs, @Nonnull VDir parent, @Nonnull String name) {
      super(vfs, name);
      this.parent = parent;
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return new FileLocation(getNativePath());
    }

    @Nonnull
    File getNativePath() {
      return new File(((DirectVDir) parent).getNativePath(), name);
    }

    @Override
    @Nonnull
    public VPath getPath() {
      return new VPath(getNativePath().getPath(), File.separatorChar);
    }
  }

  public DirectFS(@Nonnull Directory dir, int permissions) {
    this.root = new DirectRootVDir(this, dir);
    this.permissions = permissions;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return root.getLocation();
  }

  @Override
  public void close() {}

  @Override
  @Nonnull
  public String getPath() {
    return root.getPath().getPathAsString(File.separatorChar);
  }

  @Override
  public DirectVDir getRootDir() {
    return root;
  }

  @Override
  @Nonnull
  InputStream openRead(@Nonnull DirectVFile file) throws WrongPermissionException {
    assert (permissions & Permission.READ) != 0;

    File path = file.getNativePath();
    try {
      return new FileInputStream(path);
    } catch (FileNotFoundException e) {
      FileOrDirectory.checkPermissions(path, file.getLocation(), Permission.READ);
      throw new ConcurrentIOException(e);
    }
  }

  @Nonnull
  @Override
  OutputStream openWrite(@Nonnull DirectVFile file) throws WrongPermissionException {
    assert (permissions & Permission.WRITE) != 0;

    // XXX Sequential support
    File path = file.getNativePath();
    try {
      return new FileOutputStream(path);
    } catch (FileNotFoundException e) {
      FileOrDirectory.checkPermissions(path, file.getLocation(), Permission.WRITE);
      throw new ConcurrentIOException(e);
    }
  }

  @Nonnull
  @Override
  Collection<? extends BaseVElement> list(@Nonnull DirectVDir dir) {
    assert (permissions & Permission.READ) != 0;

    File file = dir.getNativePath();
    File[] subs = file.listFiles();
    if (subs == null) {
      throw new ConcurrentIOException(new ListDirException(file));
    }
    if (subs.length == 0) {
      return Collections.emptyList();
    }

    ArrayList<BaseVElement> items = new ArrayList<BaseVElement>(subs.length);
    for (File sub : subs) {
      if (sub.isFile()) {
        items.add(new DirectNonRootVDir(this, dir, sub.getName()));
      } else {
        items.add(new DirectVFile(this, dir, sub.getName()));
      }
    }

    return items;
  }

  @Override
  @Nonnull
  DirectVFile createVFile(@Nonnull DirectVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    assert (permissions & Permission.WRITE) != 0;

    File file = new File(parent.getNativePath().getPath(), name);
    try {
      AbstractStreamFile.create(file, new FileLocation(file));
    } catch (FileAlreadyExistsException e) {
      // Nothing to do
    }

    return new DirectVFile(this, parent, name);
  }

  @Override
  @Nonnull
  DirectVDir getVDir(@Nonnull DirectVDir parent, @Nonnull String name)
      throws NotDirectoryException, NoSuchFileException {
    assert (permissions & Permission.READ) != 0;

    File file = new File(parent.getNativePath(), name);
    Directory.check(file, new DirectoryLocation(file));

    return new DirectNonRootVDir(this, parent, name);
  }

  @Override
  @Nonnull
  DirectVFile getVFile(@Nonnull DirectVDir parent, @Nonnull String name)
      throws NotFileException, NoSuchFileException {
    assert (permissions & Permission.READ) != 0;

    File file = new File(parent.getNativePath(), name);
    AbstractStreamFile.check(file, new FileLocation(file));

    return new DirectVFile(this, parent, name);
  }

  @Override
  @Nonnull
  void delete(@Nonnull DirectVFile file) throws CannotDeleteFileException {
    assert (permissions & Permission.WRITE) != 0;

    File rawFile = file.getNativePath();
    if (!rawFile.delete() || rawFile.exists()) {
      throw new CannotDeleteFileException(file.getLocation());
    }
  }

  @Override
  @Nonnull
  DirectVDir createVDir(@Nonnull DirectVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    assert (permissions & Permission.WRITE) != 0;

    File file = new File(parent.getNativePath(), name);
    try {
      Directory.create(file, new DirectoryLocation(file));
    } catch (FileAlreadyExistsException e) {
      // Nothing to do
    }

    return new DirectNonRootVDir(this, parent, name);
  }
}
