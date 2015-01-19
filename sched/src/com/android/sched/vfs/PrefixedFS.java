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
public class PrefixedFS extends BaseVFS<BaseVDir, BaseVFile> implements VFS{
  @Nonnull
  private final BaseVFS<BaseVDir, BaseVFile> vfs;
  @Nonnull
  private final BaseVDir rootDir;

  @SuppressWarnings("unchecked")
  public PrefixedFS(@Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> vfs,
      @Nonnull VPath prefix) throws CannotCreateFileException {
    this.vfs = (BaseVFS<BaseVDir, BaseVFile>) vfs;
    rootDir = vfs.getRootDir().createVDir(prefix);
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
    return vfs.openWrite(file);
  }

  @Override
  @Nonnull
  void delete(@Nonnull BaseVFile file) throws CannotDeleteFileException {
    vfs.delete(file);
  }

  @Override
  @Nonnull
  Collection<? extends BaseVElement> list(@Nonnull BaseVDir dir) {
    return vfs.list(dir);
  }

  @Override
  @Nonnull
  BaseVFile createVFile(@Nonnull BaseVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return vfs.createVFile(parent, name);
  }

  @Override
  @Nonnull
  BaseVDir createVDir(@Nonnull BaseVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return vfs.createVDir(parent, name);
  }

  @Override
  @Nonnull
  BaseVDir getVDir(@Nonnull BaseVDir parent, @Nonnull String name) throws NotDirectoryException,
      NoSuchFileException {
    return vfs.getVDir(parent, name);
  }

  @Override
  @Nonnull
  BaseVFile getVFile(@Nonnull BaseVDir parent, @Nonnull String name) throws NotFileException,
      NoSuchFileException {
    return vfs.getVFile(parent, name);
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
}
