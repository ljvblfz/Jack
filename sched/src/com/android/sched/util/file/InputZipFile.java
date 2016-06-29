/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched.util.file;

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.RunnableHooks;
import com.android.sched.util.location.FileLocation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipFile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a zip file designed to be read.
 */
public class InputZipFile extends StreamFile {

  @Nonnull
  ZipFile zipFile;

  public InputZipFile(@Nonnull String path, @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence, @Nonnull ChangePermission change)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotChangePermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileException,
      ZipException {
    this(new File(path), new FileLocation(path), hooks, existence, change);
  }

  public InputZipFile(@CheckForNull Directory workingDirectory, String path,
      @CheckForNull RunnableHooks hooks, @Nonnull Existence mustExist,
      @Nonnull ChangePermission change)
      throws NotFileException,
      FileAlreadyExistsException,
      CannotCreateFileException,
      CannotChangePermissionException,
      WrongPermissionException,
      NoSuchFileException,
      ZipException {
    this(getFileFromWorkingDirectory(workingDirectory, path),
        new FileLocation(path), hooks, mustExist, change);
  }

  private InputZipFile(@Nonnull File file, @Nonnull FileLocation location,
      @CheckForNull RunnableHooks hooks, @Nonnull Existence existence,
      @Nonnull ChangePermission change)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotChangePermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileException,
      ZipException {
    super(file, location, hooks, existence, Permission.READ, change);
    zipFile = processZip(file);
  }

  @Nonnull
  private ZipFile processZip(@Nonnull File file) throws ZipException {
    try {
      return new ZipFile(file);
    } catch (java.util.zip.ZipException e) {
      throw new ZipException(getLocation(), e);
    } catch (IOException e) {
      // should not happen, because checks should already have been performed in processExisting
      throw new ConcurrentIOException(e);
    }
  }

  @Nonnull
  public ZipFile getZipFile() {
    clearRemover();
    return zipFile;
  }

  @Nonnull
  public String getName() {
    assert file != null;
    return file.getName();
  }

  @Nonnull
  public FileTime getLastModified() throws CannotGetModificationTimeException {
    assert file != null;
    try {
      return Files.getLastModifiedTime(file.toPath());
    } catch (IOException e) {
      throw new CannotGetModificationTimeException(this, e);
    }
  }
}