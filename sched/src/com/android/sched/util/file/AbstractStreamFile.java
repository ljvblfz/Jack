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
import com.android.sched.util.location.FileOrDirLocation;
import com.android.sched.util.location.StandardInputLocation;
import com.android.sched.util.location.StandardOutputLocation;
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
  private final Logger  logger = LoggerFactory.getLogger();
  @CheckForNull
  protected final File    file;

  protected AbstractStreamFile(@Nonnull String name,
      @CheckForNull RunnableHooks hooks) {
    super(hooks);

    this.file = new File(name);
    this.location = new FileLocation(file);
  }

  protected AbstractStreamFile(int permissions) {
    super(null);

    assert permissions == Permission.READ || permissions == Permission.WRITE;

    this.file = null;

    if (permissions == Permission.READ) {
      this.location = new StandardInputLocation();
    } else {
      this.location = new StandardOutputLocation();
    }
  }

  protected void performChecks(@Nonnull Existence existence, int permissions,
      @Nonnull ChangePermission change)
      throws NoSuchFileException,
      NotFileException,
      WrongPermissionException,
      FileAlreadyExistsException,
      CannotCreateFileException,
      CannotSetPermissionException {
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
        processExisting(permissions);
        break;
      case NOT_EXIST:
        processNotExisting(permissions, change);
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

  private void processNotExisting(int permissions, @Nonnull ChangePermission change)
      throws FileAlreadyExistsException, CannotCreateFileException, CannotSetPermissionException,
      WrongPermissionException {
    assert file != null;

    // Check existing
    if (file.exists()) {
      throw new FileAlreadyExistsException((FileOrDirLocation) location);
    }

    // Create file
    try {
      if (file.createNewFile()) {
        logger.log(Level.FINE, "Create {0} (''{1}'')",
            new Object[] {location.getDescription(), file.getAbsoluteFile()});
        addRemover(file);
      } else {
        throw new CannotCreateFileException((FileOrDirLocation) location);
      }
    } catch (IOException e) {
      throw new CannotCreateFileException((FileOrDirLocation) location);
    }

    setPermissions(file, permissions, change);
    checkPermissions(file, permissions);
  }

  private void processExisting(int permissions)
      throws NoSuchFileException, NotFileException, WrongPermissionException {
    assert file != null;

    // Check existing
    if (!file.exists()) {
      throw new NoSuchFileException((FileOrDirLocation) location);
    }

    // Check it is a file
    if (!file.isFile()) {
      throw new NotFileException((FileLocation) location);
    }

    checkPermissions(file, permissions);
  }

  @Override
  @Nonnull
  public String getPath() {
    return ((FileLocation) location).getPath();
  }
}