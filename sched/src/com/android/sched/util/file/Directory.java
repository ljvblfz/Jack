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

import com.android.sched.util.RunnableHooks;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileOrDirLocation;
import com.android.sched.util.log.LoggerFactory;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a directory path.
 */
public class Directory extends FileOrDirectory {
  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private final File   file;

  public Directory(@Nonnull String name, @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence, int permissions, @Nonnull ChangePermission change)
      throws WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      NotFileOrDirectoryException,
      FileAlreadyExistsException,
      CannotCreateFileException {
    super(hooks);

    this.file = new File(name);
    this.location = new DirectoryLocation(name);

    if (existence == Existence.MAY_EXIST) {
      if (file.exists()) {
        existence = Existence.MUST_EXIST;
      } else {
        existence = Existence.NOT_EXIST;
      }
    }

    switch (existence) {
      case MUST_EXIST:
        processExisting(permissions);
        break;
      case NOT_EXIST:
        processNotExisting(permissions, change);
        break;
      case MAY_EXIST:
        throw new AssertionError();
    }
  }

  @Nonnull
  public File getFile() {
    clearRemover();

    return file;
  }

  @Override
  @Nonnull
  public String toString() {
    return location.getDescription();
  }

  private void processExisting(int permissions)
      throws WrongPermissionException, NoSuchFileException, NotFileOrDirectoryException {
    assert file != null;

    // Check existing
    if (!file.exists()) {
      throw new NoSuchFileException((FileOrDirLocation) location);
    }

    // Check directory
    if (!file.isDirectory()) {
      throw new NotFileOrDirectoryException((FileOrDirLocation) location);
    }

    checkPermissions(file, permissions);
  }

  private void processNotExisting(int permissions, @Nonnull ChangePermission change)
      throws WrongPermissionException, CannotSetPermissionException, FileAlreadyExistsException,
      CannotCreateFileException {
    assert file != null;

    // Check Existing
    if (file.exists()) {
      throw new FileAlreadyExistsException((FileOrDirLocation) location);
    }

    // Create
    if (file.mkdir()) {
      logger.log(Level.FINE, "Create {0} (''{1}'')",
          new Object[] {location.getDescription(), file.getAbsoluteFile()});
      addRemover(file);
    } else {
      throw new CannotCreateFileException((FileOrDirLocation) location);
    }

    setPermissions(file, permissions, change);
    checkPermissions(file, permissions);
  }

  @Override
  @Nonnull
  public String getPath() {
    return ((DirectoryLocation) location).getPath();
  }
}