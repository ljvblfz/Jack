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

import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.FileUtils;
import com.android.sched.util.file.Files;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.Location;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@link VFS} backed by a real filesystem directory, compressed into a zip archive when closed.
 */
public class ReadWriteZipFS extends BaseVFS<BaseVDir, BaseVFile> implements VFS {

  @Nonnull
  private final VFSToVFSWrapper vfs;
  @Nonnull
  private final File dir;

  public ReadWriteZipFS(@Nonnull OutputZipFile file, int numGroups, int groupSize,
      @Nonnull MessageDigestFactory mdf, boolean debug)
      throws NotDirectoryException,
      WrongPermissionException,
      CannotChangePermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      CannotCreateFileException {
    int permissions = Permission.READ | Permission.WRITE;
    dir = Files.createTempDir("vfs-");
    VFS workVFS;
    try {
      CachedDirectFS cdFS = new CachedDirectFS(new Directory(dir.getPath(), null,
          Existence.MUST_EXIST, permissions, ChangePermission.NOCHANGE), permissions);
      workVFS = new CaseInsensitiveFS(cdFS,
          numGroups, groupSize, mdf, debug);
      cdFS.setInfoString("tmp-for-zip");
    } catch (WrongVFSFormatException e) {
      // Directory is empty, so this cannot happen
      throw new AssertionError(e);
    }
    WriteZipFS finalVFS = new WriteZipFS(file);
    this.vfs = new VFSToVFSWrapper(workVFS, finalVFS);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return vfs.getLocation();
  }

  @Override
  public void close() throws CannotCloseException {
    vfs.close();
    try {
      if (dir.exists()) {
        FileUtils.deleteDir(dir);
      }
    } catch (IOException e) {
      throw new CannotCloseException(new DirectoryLocation(dir), e);
    }
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "zip archive writer that uses a temporary directory";
  }

  @Override
  @Nonnull
  public String getPath() {
    return vfs.getPath();
  }

  @Override
  @Nonnull
  public BaseVDir getRootDir() {
    return vfs.getRootDir();
  }

  @Override
  public boolean needsSequentialWriting() {
    return vfs.needsSequentialWriting();
  }

  @Override
  @Nonnull
  public Set<Capabilities> getCapabilities() {
    return vfs.getCapabilities();
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
  @Nonnull
  BaseVDir createVDir(@Nonnull BaseVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return vfs.createVDir(parent, name);
  }

  @Override
  @Nonnull
  BaseVFile createVFile(@Nonnull BaseVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return vfs.createVFile(parent, name);
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

  public void setWorkVFS(@Nonnull VFS workVFS) {
    vfs.setWorkVFS(workVFS);
  }

  @Override
  @Nonnull
  VPath getPathFromDir(@Nonnull BaseVDir parent, @Nonnull BaseVFile file) {
    return vfs.getPathFromDir(parent, file);
  }

  @Override
  @Nonnull
  VPath getPathFromRoot(@Nonnull BaseVFile file) {
    return getPathFromDir(getRootDir(), file);
  }

  @Nonnull
  public VFS getWorkVFS() {
    return vfs.getWorkVFS();
  }

  @Override
  @CheckForNull
  public String getInfoString() {
    return vfs.getInfoString();
  }

  public void setInfoString(@CheckForNull String infoString) {
    ((WriteZipFS) vfs.getFinalVFS()).setInfoString(infoString);
  }

  @Override
  public String toString() {
    return "rwZipFS >> " + vfs.toString();
  }
}
