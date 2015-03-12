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
import com.android.sched.util.location.Location;
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
  private static final Logger logger = LoggerFactory.getLogger();
  @Nonnull
  private final File file;

  public Directory(@Nonnull String name, @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence, int permissions, @Nonnull ChangePermission change)
      throws WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      NotDirectoryException,
      FileAlreadyExistsException,
      CannotCreateFileException {

    this(new File(name), new DirectoryLocation(name), hooks, existence, permissions, change);
  }

  public Directory(@CheckForNull Directory workingDirectory,
      @Nonnull String string,
      @CheckForNull RunnableHooks runnableHooks,
      @Nonnull Existence existence,
      int permissions,
      @Nonnull ChangePermission change)
      throws NotDirectoryException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      CannotCreateFileException {
    this(getFileFromWorkingDirectory(workingDirectory, string),
        new DirectoryLocation(string),
        runnableHooks,
        existence,
        permissions,
        change);
  }

  private Directory(@Nonnull File file,
      @Nonnull DirectoryLocation location,
      @CheckForNull RunnableHooks hooks,
      @Nonnull Existence existence,
      int permissions,
      @Nonnull ChangePermission change)
      throws WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      NotDirectoryException,
      FileAlreadyExistsException,
      CannotCreateFileException {
    super(hooks);

    this.file = file;
    this.location = location;

    if (existence == Existence.MAY_EXIST) {
      if (file.exists()) {
        existence = Existence.MUST_EXIST;
      } else {
        existence = Existence.NOT_EXIST;
      }
    }

    switch (existence) {
      case MUST_EXIST:
        Directory.check(file, location);
        FileOrDirectory.checkPermissions(file, location, permissions);
        break;
      case NOT_EXIST:
        Directory.create(file, location);
        addRemover(file);
        FileOrDirectory.setPermissions(file, location, permissions, change);
        FileOrDirectory.checkPermissions(file, location, permissions);
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

  public static void check(@Nonnull File file, @Nonnull DirectoryLocation location)
      throws NoSuchFileException, NotDirectoryException {
    assert file != null;

    // Check existing
    if (!file.exists()) {
      throw new NoSuchFileException(location);
    }

    // Check if it is a directory
    if (!file.isDirectory()) {
      throw new NotDirectoryException(location);
    }
  }

  public static void create(@Nonnull File file, @Nonnull Location location)
      throws FileAlreadyExistsException, CannotCreateFileException {
    assert file != null;

    // Check Existing
    if (file.exists()) {
      throw new FileAlreadyExistsException(location);
    }

    // Create
    if (file.mkdir()) {
      logger.log(Level.FINE, "Create {0} (''{1}'')",
          new Object[] {location.getDescription(), file.getAbsoluteFile()});
    } else {
      if (!file.exists()) {
        throw new CannotCreateFileException(location);
      }
    }
  }

  @Override
  @Nonnull
  public String getPath() {
    return file.getPath();
  }
}
