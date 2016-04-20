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
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A {@link VFS} filter that adds a prefix to all paths.
 */
public class PrefixedFS extends BaseVFS<BaseVDir, BaseVFile> implements VFS {
  @Nonnull
  private final BaseVFS<BaseVDir, BaseVFile> vfs;
  @Nonnull
  private final BaseVDir rootDir;

  @SuppressWarnings("unchecked")
  public PrefixedFS(@Nonnull VFS vfs, @Nonnull VPath prefix) throws CannotCreateFileException,
      NotDirectoryException {
    this.vfs = (BaseVFS<BaseVDir, BaseVFile>) vfs;

    BaseVDir rootDir;
    // let's try to get the VDir before creating it because we not have write permissions.
    try {
      rootDir = this.vfs.getRootDir().getVDir(prefix);
    } catch (NoSuchFileException e) {
      rootDir = this.vfs.getRootDir().createVDir(prefix);
    }
    this.rootDir = changeVFS(rootDir);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return rootDir.getLocation();
  }

  @Override
  public void close() {
    // do not actually close
  }

  @Override
  @Nonnull
  public String getPath() {
    return vfs.getPath();
  }

  @Override
  @Nonnull
  public BaseVDir getRootDir() {
    return rootDir;
  }

  @Override
  @Nonnull
  InputStream openRead(@Nonnull BaseVFile file) throws WrongPermissionException {
    return vfs.openRead(file);
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull BaseVFile file) throws WrongPermissionException {
    return openWrite(file, false);
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull BaseVFile file, boolean append) throws WrongPermissionException {
    return vfs.openWrite(file, append);
  }

  @Override
  @Nonnull
  void delete(@Nonnull BaseVFile file) throws CannotDeleteFileException {
    vfs.delete(file);
  }

  @Override
  @Nonnull
  Collection<? extends BaseVElement> list(@Nonnull BaseVDir dir) {
    Collection<? extends BaseVElement> elements = vfs.list(dir);
    for (BaseVElement element : elements) {
      element.changeVFS(this);
    }
    return elements;
  }

  @Override
  @Nonnull
  BaseVFile createVFile(@Nonnull BaseVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return changeVFS(vfs.createVFile(parent, name));
  }

  @Override
  @Nonnull
  BaseVDir createVDir(@Nonnull BaseVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return changeVFS(vfs.createVDir(parent, name));
  }

  @Override
  @Nonnull
  BaseVDir getVDir(@Nonnull BaseVDir parent, @Nonnull String name) throws NotDirectoryException,
      NoSuchFileException {
    return changeVFS(vfs.getVDir(parent, name));
  }

  @Override
  @Nonnull
  BaseVFile getVFile(@Nonnull BaseVDir parent, @Nonnull String name) throws NotFileException,
      NoSuchFileException {
    return changeVFS(vfs.getVFile(parent, name));
  }


  @Override
  public boolean needsSequentialWriting() {
    return vfs.needsSequentialWriting();
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "prefixed wrapper";
  }

  @Override
  @Nonnull
  public Set<Capabilities> getCapabilities() {
    return vfs.getCapabilities();
  }

  @Override
  boolean isEmpty(@Nonnull BaseVDir dir) {
    return vfs.isEmpty(dir);
  }

  @Override
  long getLastModified(@Nonnull BaseVFile file) {
    return vfs.getLastModified(file);
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull BaseVFile file) {
    return vfs.getVFileLocation(file);
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull BaseVDir parent, @Nonnull String name) {
    return vfs.getVFileLocation(parent, name);
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull BaseVDir parent, @Nonnull VPath path) {
    return vfs.getVFileLocation(parent, path);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull BaseVDir dir) {
    return vfs.getVDirLocation(dir);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull BaseVDir parent, @Nonnull String name) {
    return vfs.getVDirLocation(parent, name);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull BaseVDir parent, @Nonnull VPath path) {
    return vfs.getVDirLocation(parent, path);
  }

  @Override
  @Nonnull
  VPath getPathFromDir(@Nonnull BaseVDir parent, @Nonnull BaseVFile file) {
    return vfs.getPathFromDir(parent, file);
  }

  @Override
  @Nonnull
  VPath getPathFromRoot(@Nonnull BaseVFile file) {
    return vfs.getPathFromDir(rootDir, file);
  }

  @Nonnull
  private BaseVDir changeVFS(@Nonnull BaseVDir dir) {
    dir.changeVFS(this);
    return dir;
  }

  @Nonnull
  private BaseVFile changeVFS(@Nonnull BaseVFile file) {
    file.changeVFS(this);
    return file;
  }

  @Override
  public String toString() {
    return "PrefixedFS >> " + vfs.toString();
  }
}
