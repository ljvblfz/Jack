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
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

/**
 * A directory in an {@link InputOutputZipVFS} VFS.
 */
class InputOutputZipVDir extends AbstractVElement implements InputOutputVDir {

  @Nonnull
  protected final File dir;
  @Nonnull
  protected final InputOutputZipVFS vfs;
  @Nonnull
  private final ZipEntry zipEntry;

  public InputOutputZipVDir(@Nonnull InputOutputZipVFS vfs, @Nonnull File dir,
      @Nonnull ZipEntry zipEntry) {
    this.vfs = vfs;
    this.dir = dir;
    this.zipEntry = zipEntry;
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
      String subZipEntryName = zipEntry.getName() + sub.getName();
      if (sub.isFile()) {
        items.add(new InputOutputZipVFile(vfs, sub, new ZipEntry(subZipEntryName)));
      } else {
        items.add(new InputOutputZipVDir(vfs, sub,
            new ZipEntry(subZipEntryName + ZipUtils.ZIP_SEPARATOR)));
      }
    }

    return items;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new ZipLocation(vfs.getLocation(), zipEntry);
  }

  @Override
  public boolean isVDir() {
    return true;
  }

  @Override
  @Nonnull
  public OutputVFile createOutputVFile(@Nonnull VPath path) throws CannotCreateFileException {
    assert !vfs.isClosed();
    File file = new File(dir, path.getPathAsString(ZipUtils.ZIP_SEPARATOR));
    if (!file.getParentFile().mkdirs() && !file.getParentFile().isDirectory()) {
      throw new CannotCreateFileException(new DirectoryLocation(file.getParentFile()));
    }

    assert !(path.equals(VPath.ROOT));
    String newEntryName = path.getPathAsString(ZipUtils.ZIP_SEPARATOR);
    String parentEntryName = zipEntry.getName();
    if (!parentEntryName.isEmpty()) {
      newEntryName = parentEntryName + newEntryName;
    }
    return new InputOutputZipVFile(vfs, file, new ZipEntry(newEntryName));
  }

  @Override
  @Nonnull
  public OutputVDir createOutputVDir(@Nonnull VPath path) throws CannotCreateFileException {
    assert !vfs.isClosed();
    File file = new File(dir, path.getPathAsString(ZipUtils.ZIP_SEPARATOR));
    if (!file.getParentFile().mkdirs() && !file.getParentFile().isDirectory()) {
      throw new CannotCreateFileException(new DirectoryLocation(file.getParentFile()));
    }

    if (!file.mkdir()) {
      throw new CannotCreateFileException(new DirectoryLocation(file));
    }

    assert !(path.equals(VPath.ROOT));
    String newEntryName = path.getPathAsString(ZipUtils.ZIP_SEPARATOR);
    String parentEntryName = zipEntry.getName();
    if (!parentEntryName.isEmpty()) {
      newEntryName = parentEntryName + newEntryName;
    }
    return new InputOutputZipVDir(vfs, file, new ZipEntry(newEntryName));
  }

  @Override
  @Nonnull
  public InputOutputVDir getInputVDir(@Nonnull VPath path) throws NotFileOrDirectoryException,
      NoSuchFileException {
    File file = new File(dir, path.getPathAsString(File.separatorChar));
    if (!file.exists()) {
      throw new NoSuchFileException(new FileLocation(file));
    }
    if (file.isFile()) {
      throw new NotFileOrDirectoryException(new FileLocation(file));
    }
    return new InputOutputZipVDir(vfs, file,
        new ZipEntry(path.getPathAsString(ZipUtils.ZIP_SEPARATOR) + ZipUtils.ZIP_SEPARATOR));
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
    return new InputOutputZipVFile(vfs, file,
        new ZipEntry(path.getPathAsString(ZipUtils.ZIP_SEPARATOR)));
  }

  @Override
  @Nonnull
  public void delete(@Nonnull VPath path) throws CannotDeleteFileException {
    assert !vfs.isClosed();
    File file = new File(dir, path.getPathAsString(File.separatorChar));
    if (!file.delete()) {
      throw new CannotDeleteFileException(
          file.isDirectory() ? new DirectoryLocation(file) : new FileLocation(file));
    }
  }
}
