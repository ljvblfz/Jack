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
public class InputZipFile extends AbstractStreamFile {

  @Nonnull
  private final ZipFile zipFile;

  public InputZipFile(@Nonnull String path)
      throws WrongPermissionException,
      NoSuchFileException,
      NotFileException,
      ZipException {
    this(new File(path), new FileLocation(path));
  }

  public InputZipFile(@CheckForNull Directory workingDirectory, String path)
      throws NotFileException,
      WrongPermissionException,
      NoSuchFileException,
      ZipException {
    this(getFileFromWorkingDirectory(workingDirectory, path),
        new FileLocation(path));
  }

  protected InputZipFile(@Nonnull File file, @Nonnull FileLocation location,
      @Nonnull ZipFile zipFile)
      throws WrongPermissionException,
      NoSuchFileException,
      NotFileException {
    super(file, location, null /* hooks */);
    try {
      performChecks(Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE);
    } catch (FileAlreadyExistsException e) {
      throw new AssertionError(e);
    } catch (CannotCreateFileException e) {
      throw new AssertionError(e);
    } catch (CannotChangePermissionException e) {
      throw new AssertionError(e);
    }
    this.zipFile = zipFile;
  }

  private InputZipFile(@Nonnull File file, @Nonnull FileLocation location)
      throws WrongPermissionException,
      NoSuchFileException,
      NotFileException,
      ZipException {
    super(file, location, null /* hooks */);
    try {
      performChecks(Existence.MUST_EXIST, Permission.READ, ChangePermission.NOCHANGE);
    } catch (FileAlreadyExistsException e) {
      throw new AssertionError(e);
    } catch (CannotCreateFileException e) {
      throw new AssertionError(e);
    } catch (CannotChangePermissionException e) {
      throw new AssertionError(e);
    }
    zipFile = processZip(file);
  }

  @Nonnull
  protected ZipFile processZip(@Nonnull File file) throws ZipException {
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