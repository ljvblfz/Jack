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

package com.android.sched.vfs.direct;

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.InputOutputVDir;
import com.android.sched.vfs.InputRootVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.SequentialOutputVDir;
import com.android.sched.vfs.VPath;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Directory in the file system.
 */
public class DirectDir extends SequentialOutputVDir implements InputRootVDir, InputOutputVDir {

  @Nonnull
  private final File dir;
  @CheckForNull
  private ArrayList<InputVElement> list;
  @Nonnull
  private final Location location;
  @Nonnull
  private final InputOutputVDir vfsRoot;

  public DirectDir(@Nonnull Directory directory) {
    dir = directory.getFile();
    location = directory.getLocation();
    vfsRoot = this;
  }

  public DirectDir(@Nonnull File dir) throws NotFileOrDirectoryException {
    if (!dir.isDirectory()) {
      throw new NotFileOrDirectoryException(new DirectoryLocation(dir));
    }
    this.dir = dir;
    location = new FileLocation(dir);
    vfsRoot = this;
  }

  DirectDir(@Nonnull File dir, @Nonnull InputOutputVDir vfsRoot)
      throws NotFileOrDirectoryException {
    if (!dir.isDirectory()) {
      throw new NotFileOrDirectoryException(new DirectoryLocation(dir));
    }
    this.dir = dir;
    location = new FileLocation(dir);
    this.vfsRoot = vfsRoot;
  }

  @Nonnull
  @Override
  public String getName() {
    return dir.getName();
  }

  @Nonnull
  @Override
  public synchronized Collection<? extends InputVElement> list() {
    if (list == null) {
      File[] subs = dir.listFiles();
      if (subs == null) {
        throw new ConcurrentIOException(new ListDirException(dir));
      }
      if (subs.length == 0) {
        return Collections.emptyList();
      }

      list = new ArrayList<InputVElement>(subs.length);
      for (File sub : subs) {
        try {
          if (sub.isFile()) {
            list.add(new DirectFile(sub, vfsRoot));
          } else {
            list.add(new DirectDir(sub, vfsRoot));
          }
        } catch (NotFileOrDirectoryException e) {
          throw new ConcurrentIOException(e);
        }
      }
    }

    assert list != null;
    return list;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return location;
  }

  @Override
  @Nonnull
  public InputVFile getInputVFile(@Nonnull VPath path) throws NotFileOrDirectoryException {
    File file = new File(dir, path.getPathAsString(File.separatorChar));
    if (!file.isFile()) {
      throw new NotFileOrDirectoryException(new FileLocation(file));
    }
    return new DirectFile(file, vfsRoot);
  }

  @Override
  @Nonnull
  public OutputVFile createOutputVFile(@Nonnull VPath path) throws CannotCreateFileException {
    File file = new File(dir, path.getPathAsString(getSeparator()));
    if (!file.getParentFile().mkdirs() && !file.getParentFile().isDirectory()) {
      throw new CannotCreateFileException(new DirectoryLocation(file.getParentFile()));
    }
    return new DirectFile(file, vfsRoot);
  }

  @Override
  public char getSeparator() {
    return File.separatorChar;
  }

  @Override
  public boolean isVDir() {
    return true;
  }
}
