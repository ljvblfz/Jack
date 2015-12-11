/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sched.util.file;

import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * Provides utility methods for working with files.
 */
public class Files {

  /** Maximum loop count when creating temp files or directories. */
  private static final int TEMP_ATTEMPTS = 10000;

  private Files() {}

  @Nonnull
  public static File createTempDir(@Nonnull String prefix)
      throws CannotCreateFileException, CannotSetPermissionException, WrongPermissionException {
    File baseDir = new File(System.getProperty("java.io.tmpdir"));
    String baseName = prefix + System.currentTimeMillis() + "-";
    Location location = null;

    for (int counter = 0; counter < TEMP_ATTEMPTS; counter++) {
      File tempDir = new File(baseDir, baseName + counter);
      location = new DirectoryLocation(tempDir);
      try {
        Directory.create(tempDir, location);
        FileOrDirectory.setPermissions(tempDir, location, Permission.WRITE,
            ChangePermission.NOCHANGE);
        FileOrDirectory.checkPermissions(tempDir, location, Permission.WRITE);
        return tempDir;
      } catch (FileAlreadyExistsException e) {
        // ignore and try again
      }
    }

    assert location != null;
    throw new CannotCreateFileException(location);
  }

  @Nonnull
  public static File createTempFile(@Nonnull String prefix)
      throws CannotCreateFileException, CannotSetPermissionException, WrongPermissionException {
    return createTempFile(prefix, "");
  }

  @Nonnull
  public static File createTempFile(@Nonnull String prefix, @Nonnull String suffix)
      throws CannotCreateFileException, CannotSetPermissionException, WrongPermissionException {
    File baseDir = new File(System.getProperty("java.io.tmpdir"));
    String baseName = prefix + System.currentTimeMillis() + "-";
    Location location = null;

    for (int counter = 0; counter < TEMP_ATTEMPTS; counter++) {
      File tempFile = new File(baseDir, baseName + counter + suffix);
      location = new FileLocation(tempFile);
      try {
        AbstractStreamFile.create(tempFile, location);
        FileOrDirectory.setPermissions(tempFile, location, Permission.WRITE,
            ChangePermission.NOCHANGE);
        FileOrDirectory.checkPermissions(tempFile, location, Permission.WRITE);
        return tempFile;
      } catch (FileAlreadyExistsException e) {
        // ignore and try again
      }

    }

    assert location != null;
    throw new CannotCreateFileException(location);
  }
}
