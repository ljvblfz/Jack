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

import com.android.sched.util.file.CannotCloseInputException;
import com.android.sched.util.file.CannotCloseOutputException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.util.stream.LocationByteStreamSucker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A double {@link VFS} wrapper that uses a work {@link VFS} during its life, and dumps it contents
 * into a final {@link VFS} when it is closed.
 */
public class VFSToVFSWrapper extends BaseVFS<BaseVDir, BaseVFile> implements VFS {

  @Nonnull
  private BaseVFS<BaseVDir, BaseVFile> workVFS;
  @Nonnull
  private final BaseVFS<BaseVDir, BaseVFile> finalVFS;
  @Nonnull
  private final Set<Capabilities> capabilities;

  @SuppressWarnings("unchecked")
  public VFSToVFSWrapper(@Nonnull VFS workVFS, @Nonnull VFS finalVFS) {
    this.workVFS = (BaseVFS<BaseVDir, BaseVFile>) workVFS;
    this.finalVFS = (BaseVFS<BaseVDir, BaseVFile>) finalVFS;

    Set<Capabilities> capabilities = EnumSet.noneOf(Capabilities.class);
    for (Capabilities capability : workVFS.getCapabilities()) {
      switch (capability) {
        case CASE_SENSITIVE:
          if (finalVFS.getCapabilities().contains(capability)) {
            // both VFSes need to support case sensitivity for this wrapper to support it
            capabilities.add(capability);
          }
          break;
        case DIGEST:
          capabilities.add(capability);
          break;
        case PARALLEL_READ:
          capabilities.add(capability);
          break;
        case PARALLEL_WRITE:
          capabilities.add(capability);
          break;
        case READ:
          capabilities.add(capability);
          break;
        case UNIQUE_ELEMENT:
          capabilities.add(capability);
          break;
        case WRITE:
          capabilities.add(capability);
          break;
        default:
          throw new AssertionError();
      }
    }
    this.capabilities = Collections.unmodifiableSet(capabilities);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return finalVFS.getLocation();
  }

  @Override
  public void close() throws CannotCloseOutputException {
    if (!closed) {
      try {
        dumpToDir(getRootDir(), finalVFS.getRootDir());
        finalVFS.close();
        workVFS.close();
        closed = true;
      } catch (CannotCloseInputException | CannotCloseOutputException | CannotReadException
          | CannotWriteException | CannotCreateFileException | WrongPermissionException e) {
        throw new CannotCloseOutputException(this, e);
      }
    }
  }

  private void dumpToDir(VDir srcRootDir, VDir destRootDir)
      throws CannotCreateFileException, WrongPermissionException, CannotCloseInputException,
      CannotCloseOutputException, CannotReadException, CannotWriteException {
    for (VElement element : srcRootDir.list()) {
      String elementName = element.getName();
      if (element.isVDir()) {
        VDir dir = destRootDir.createVDir(elementName);
        dumpToDir((VDir) element, dir);
      } else {
        VFile file = destRootDir.createVFile(elementName);

        try (InputStream is = ((VFile) element).getInputStream()) {
          try (OutputStream os = file.getOutputStream()) {
            new LocationByteStreamSucker(is, os, element, file).suck();
          } catch (IOException e) {
            throw new CannotCloseOutputException(file, e);
          }
        } catch (IOException e) {
          throw new CannotCloseInputException(element, e);
        }
      }
    }

  }

  @Override
  @Nonnull
  public String getDescription() {
    return "a VFS-to-VFS wrapper";
  }

  @Override
  @Nonnull
  public String getPath() {
    return finalVFS.getPath();
  }

  @Override
  @Nonnull
  public BaseVDir getRootDir() {
    return workVFS.getRootDir();
  }

  @Override
  public boolean needsSequentialWriting() {
    return workVFS.needsSequentialWriting();
  }

  @Override
  @Nonnull
  public Set<Capabilities> getCapabilities() {
    return this.capabilities;
  }

  @Override
  @Nonnull
  InputStream openRead(@Nonnull BaseVFile file) throws WrongPermissionException {
    return workVFS.openRead(file);
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull BaseVFile file) throws WrongPermissionException {
    return openWrite(file, false);
  }

  @Override
  @Nonnull
  OutputStream openWrite(@Nonnull BaseVFile file, boolean append) throws WrongPermissionException {
    return workVFS.openWrite(file, append);
  }

  @Override
  @Nonnull
  BaseVDir getVDir(@Nonnull BaseVDir parent, @Nonnull String name) throws NotDirectoryException,
      NoSuchFileException {
    return workVFS.getVDir(parent, name);
  }

  @Override
  @Nonnull
  BaseVFile getVFile(@Nonnull BaseVDir parent, @Nonnull String name) throws NotFileException,
      NoSuchFileException {
    return workVFS.getVFile(parent, name);
  }

  @Override
  @Nonnull
  BaseVDir createVDir(@Nonnull BaseVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return workVFS.createVDir(parent, name);
  }

  @Override
  @Nonnull
  BaseVFile createVFile(@Nonnull BaseVDir parent, @Nonnull String name)
      throws CannotCreateFileException {
    return workVFS.createVFile(parent, name);
  }

  @Override
  @Nonnull
  void delete(@Nonnull BaseVFile file) throws CannotDeleteFileException {
    workVFS.delete(file);
  }

  @Override
  @Nonnull
  Collection<? extends BaseVElement> list(@Nonnull BaseVDir dir) {
    return workVFS.list(dir);
  }

  @Override
  boolean isEmpty(@Nonnull BaseVDir dir) {
    return workVFS.isEmpty(dir);
  }

  @Override
  long getLastModified(@Nonnull BaseVFile file) {
    return workVFS.getLastModified(file);
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull BaseVFile file) {
    return workVFS.getVFileLocation(file);
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull BaseVDir parent, @Nonnull String name) {
    return workVFS.getVFileLocation(parent, name);
  }

  @Override
  @Nonnull
  Location getVFileLocation(@Nonnull BaseVDir parent, @Nonnull VPath path) {
    return workVFS.getVFileLocation(parent, path);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull BaseVDir dir) {
    return workVFS.getVDirLocation(dir);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull BaseVDir parent, @Nonnull String name) {
    return workVFS.getVDirLocation(parent, name);
  }

  @Override
  @Nonnull
  Location getVDirLocation(@Nonnull BaseVDir parent, @Nonnull VPath path) {
    return workVFS.getVDirLocation(parent, path);
  }

  @SuppressWarnings("unchecked")
  public void setWorkVFS(@Nonnull VFS temporaryVFS) {
    workVFS = (BaseVFS<BaseVDir, BaseVFile>) temporaryVFS;
  }

  @Override
  @Nonnull
  VPath getPathFromDir(@Nonnull BaseVDir parent, @Nonnull BaseVFile file) {
    return workVFS.getPathFromDir(parent, file);
  }

  @Override
  @Nonnull
  VPath getPathFromRoot(@Nonnull BaseVFile file) {
    return workVFS.getPathFromRoot(file);
  }

  @Nonnull
  public VFS getWorkVFS() {
    return workVFS;
  }

  @Override
  public String toString() {
    return "(workVFS >> " + workVFS.toString() + " / finalVFS >> " + finalVFS.toString() + ')';
  }
}
