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
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Abstract class representing a stream from a file path or a standard input/output.
 */
public abstract class AbstractStreamFile extends FileOrDirectory {
  @Nonnull
  private static final Logger  logger = LoggerFactory.getLogger();
  @CheckForNull
  protected final File    file;

  protected AbstractStreamFile(@Nonnull String name,
      @CheckForNull RunnableHooks hooks) {
    this(new File(name), new FileLocation(name), hooks);
  }

  protected AbstractStreamFile(@Nonnull File file,  @Nonnull FileLocation location,
      @CheckForNull RunnableHooks hooks) {
    super(hooks);

    this.file = file;
    this.location = location;
  }

  protected AbstractStreamFile(@Nonnull Location location) {
    super(null);
    this.file = null;
    this.location = location;
  }

  protected void performChecks(@Nonnull Existence existence, int permissions,
      @Nonnull ChangePermission change)
      throws NoSuchFileException,
      NotFileException,
      WrongPermissionException,
      FileAlreadyExistsException,
      CannotCreateFileException,
      CannotChangePermissionException {
    assert file != null;

    if (existence == Existence.MAY_EXIST) {
      if (file.exists()) {
        existence = Existence.MUST_EXIST;
      } else {
        existence = Existence.NOT_EXIST;
      }
    }

    switch (existence) {
      case MUST_EXIST:
        AbstractStreamFile.check(file, location);
        FileOrDirectory.checkPermissions(file, location, permissions);
        break;
      case NOT_EXIST:
        AbstractStreamFile.create(file, location);
        addRemover(file);
        FileOrDirectory.setPermissions(file, location, permissions, change);
        FileOrDirectory.checkPermissions(file, location, permissions);
        break;
      case MAY_EXIST:
        throw new AssertionError();
    }
  }

  public boolean isStandard() {
    return this.file == null;
  }

  @Override
  @Nonnull
  public String toString() {
    return location.getDescription();
  }

  public static void create(@Nonnull File file, @Nonnull Location location)
      throws FileAlreadyExistsException, CannotCreateFileException {
    try {
      if (file.createNewFile()) {
        logger.log(Level.FINE, "Create {0} (''{1}'')",
            new Object[] {location.getDescription(), file.getAbsoluteFile()});
        return;
      }
    } catch (IOException e) {
      throw new CannotCreateFileException(location);
    }

    throw new FileAlreadyExistsException(location);
  }

  public static void check(@Nonnull File file, @Nonnull Location location)
      throws NoSuchFileException, NotFileException {
    // Check existing
    if (!file.exists()) {
      throw new NoSuchFileException(location);
    }

    // Check if it is not a Directory
    if (file.isDirectory()) {
      throw new NotFileException(location);
    }
  }

  @Override
  @Nonnull
  public String getPath() {
    assert file != null;
    return file.getPath();
  }
}
