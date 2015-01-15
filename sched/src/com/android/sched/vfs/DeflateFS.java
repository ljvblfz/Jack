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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.annotation.Nonnull;

/**
 * A {@link VFS} filter implementation which inflate/deflate individual file.
 */
public class DeflateFS extends BaseVFS<BaseVDir, BaseVFile> implements VFS{
  @Nonnull
  private final BaseVFS<BaseVDir, BaseVFile> vfs;

  @SuppressWarnings("unchecked")
  public DeflateFS(@Nonnull BaseVFS<? extends BaseVDir, ? extends BaseVFile> vfs) {
    this.vfs = (BaseVFS<BaseVDir, BaseVFile>) vfs;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return vfs.getLocation();
  }

  @Override
  public void close() throws IOException {
    vfs.close();
  }

  @Override
  @Nonnull
  public String getPath() {
    return vfs.getPath();
  }

  @Override
  @Nonnull
  public BaseVDir getRootDir() {
    return changeVFS(vfs.getRootDir());
  }

  @Override
  @Nonnull
  InputStream openRead(@Nonnull BaseVFile file) throws WrongPermissionException {
    return new InflaterInputStream(vfs.openRead(file), new Inflater());
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull BaseVFile file) throws WrongPermissionException {
    return new DeflaterOutputStream(vfs.openWrite(file), new Deflater());
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

  @Nonnull
  private BaseVFile changeVFS(@Nonnull BaseVFile file) {
    file.changeVFS(this);
    return file;
  }

  @Nonnull
  private BaseVDir changeVFS(@Nonnull BaseVDir dir) {
    dir.changeVFS(this);
    return dir;
  }
}
