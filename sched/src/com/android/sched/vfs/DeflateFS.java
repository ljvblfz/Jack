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
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotGetModificationTimeException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.DeflateFS.DeflateVDir;
import com.android.sched.vfs.DeflateFS.DeflateVFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link VFS} filter implementation which inflate/deflate individual file.
 */
public class DeflateFS extends BaseVFS<DeflateVDir, DeflateVFile> implements VFS{

  @Nonnull
  private final BaseVFS<BaseVDir, BaseVFile> vfs;

  static class DeflateVFile extends BaseVFile {

    @Nonnull
    private final BaseVFile wrappedFile;

    public DeflateVFile(@Nonnull BaseVFS<DeflateVDir, DeflateVFile> vfs,
        @Nonnull BaseVFile wrappedFile) {
      super(vfs, wrappedFile.getName());
      this.wrappedFile = wrappedFile;
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return wrappedFile.getLocation();
    }

    @Override
    @Nonnull
    public VPath getPath() {
      return wrappedFile.getPath();
    }

    @Nonnull
    public BaseVFile getWrappedFile() {
      return wrappedFile;
    }

    @Override
    @CheckForNull
    public String getDigest() {
      return wrappedFile.getDigest();
    }
  }

  static class DeflateVDir extends BaseVDir {

    @Nonnull
    private final BaseVDir wrappedFile;

    public DeflateVDir(@Nonnull BaseVFS<DeflateVDir, DeflateVFile> vfs,
        @Nonnull BaseVDir wrappedFile) {
      super(vfs, wrappedFile.getName());
      this.wrappedFile = wrappedFile;
    }

    @Override
    @Nonnull
    public Location getLocation() {
      return wrappedFile.getLocation();
    }

    @Override
    @Nonnull
    public VPath getPath() {
      return wrappedFile.getPath();
    }

    @Nonnull
    public BaseVDir getWrappedDir() {
      return wrappedFile;
    }
  }

  @SuppressWarnings("unchecked")
  public DeflateFS(@Nonnull VFS vfs) {
    this.vfs = (BaseVFS<BaseVDir, BaseVFile>) vfs;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "deflater wrapper";
  }

  @Override
  @Nonnull
  public Set<Capabilities> getCapabilities() {
    return vfs.getCapabilities();
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
  @Nonnull
  public String getPath() {
    return vfs.getPath();
  }

  @Override
  @Nonnull
  public DeflateVDir getRootDir() {
    return new DeflateVDir(this, vfs.getRootDir());
  }

  @Override
  @Nonnull
  InputStream openRead(@Nonnull DeflateVFile file) throws WrongPermissionException {
    return new InflaterInputStream(vfs.openRead(file.getWrappedFile()), new Inflater());
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull DeflateVFile file) throws WrongPermissionException {
    return openWrite(file, false);
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull DeflateVFile file, boolean append)
      throws WrongPermissionException {
    return new DeflaterOutputStream(vfs.openWrite(file.getWrappedFile(), append), new Deflater());
  }

  @Override
  @Nonnull
  void delete(@Nonnull DeflateVFile file) throws CannotDeleteFileException {
    vfs.delete(file.getWrappedFile());
  }

  @Override
  @Nonnull
  Collection<? extends BaseVElement> list(@Nonnull DeflateVDir dir) {
    Collection<? extends BaseVElement> elements = vfs.list(dir.getWrappedDir());
    List<BaseVElement> newElements = new ArrayList<BaseVElement>(elements.size());
    for (BaseVElement element : elements) {
      BaseVElement newElement;
      if (element.isVDir()) {
        newElement = new DeflateVDir(this, (BaseVDir) element);
      } else {
        newElement = new DeflateVFile(this, (BaseVFile) element);
      }
      newElements.add(newElement);
    }

    return newElements;
  }

  @Override
  boolean isEmpty(@Nonnull DeflateVDir dir) {
    return vfs.isEmpty(dir);
  }

  @Override
  @Nonnull
  FileTime getLastModified(@Nonnull DeflateVFile file) throws CannotGetModificationTimeException {
    return vfs.getLastModified(file.getWrappedFile());
  }

  @Override
  @Nonnull
  DeflateVFile createVFile(@Nonnull DeflateVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return new DeflateVFile(this, vfs.createVFile(parent.getWrappedDir(), name));
  }

  @Override
  @Nonnull
  DeflateVDir createVDir(@Nonnull DeflateVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return new DeflateVDir(this, vfs.createVDir(parent.getWrappedDir(), name));
  }

  @Override
  @Nonnull
  DeflateVDir getVDir(@Nonnull DeflateVDir parent, @Nonnull String name)
      throws NotDirectoryException, NoSuchFileException {
    return new DeflateVDir(this, vfs.getVDir(parent.getWrappedDir(), name));
  }

  @Override
  @Nonnull
  DeflateVFile getVFile(@Nonnull DeflateVDir parent, @Nonnull String name) throws NotFileException,
      NoSuchFileException {
    return new DeflateVFile(this, vfs.getVFile(parent.getWrappedDir(), name));
  }

  @Override
  public boolean needsSequentialWriting() {
    return vfs.needsSequentialWriting();
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull DeflateVFile file) {
    return vfs.getVFileLocation(file.getWrappedFile());
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull DeflateVDir parent, @Nonnull String name) {
    return vfs.getVFileLocation(parent.getWrappedDir(), name);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull DeflateVDir dir) {
    return vfs.getVDirLocation(dir.getWrappedDir());
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull DeflateVDir parent, @Nonnull String name) {
    return vfs.getVDirLocation(parent.getWrappedDir(), name);
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull DeflateVDir parent, @Nonnull VPath path) {
    return vfs.getVFileLocation(parent.getWrappedDir(), path);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull DeflateVDir parent, @Nonnull VPath path) {
    return vfs.getVDirLocation(parent.getWrappedDir(), path);
  }

  @Override
  @Nonnull
  VPath getPathFromDir(@Nonnull DeflateVDir parent, @Nonnull DeflateVFile file) {
    return vfs.getPathFromDir(parent.getWrappedDir(), file);
  }

  @Override
  @Nonnull
  VPath getPathFromRoot(@Nonnull DeflateVFile file) {
    return vfs.getPathFromRoot(file.getWrappedFile());
  }

  @Override
  @CheckForNull
  public String getDigest() {
    return vfs.getDigest();
  }

  @Override
  public String toString() {
    return "deflateFS >> " + vfs.toString();
  }

  @Override
  public void copy(@Nonnull VFile srcFile, @Nonnull DeflateVFile dstFile)
      throws WrongPermissionException, CannotCloseException,
      CannotReadException, CannotWriteException {
    if (srcFile instanceof DeflateVFile) { // copy without inflating/deflating
      vfs.copy(((DeflateVFile) srcFile).getWrappedFile(), dstFile.getWrappedFile());

      VFSStatCategory.OPTIMIZED_COPIES.getPercentStat(getTracer(), getInfoString()).addTrue();
    } else {
      super.copy(srcFile, dstFile);
    }
  }

  @Override
  @CheckForNull
  public String getInfoString() {
    return vfs.getInfoString();
  }
}
