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
import com.android.sched.util.location.FileOrDirLocation;
import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;
import com.android.sched.util.log.LoggerFactory;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Abstract class for file or directory which manage the underlying state
 * of the file system.
 */
public abstract class FileOrDirectory implements HasLocation {
  @Nonnull
  private final Logger logger = LoggerFactory.getLogger();
  /**
   * Permission
   */
  public static class Permission {
    public static final int READ    = 1 << 0;
    public static final int WRITE   = 1 << 1;
    public static final int EXECUTE = 1 << 2;
  }

  /**
   * Change Permission
   */
  public enum ChangePermission {
    NOCHANGE,
    OWNER,
    EVERYBODY
  }

  /**
   * Existing condition
   */
  public enum Existence {
    MUST_EXIST,
    NOT_EXIST,
    MAY_EXIST
  }

  @Nonnull
  private static final Location NO_LOCATION = new NoLocation();

  @Nonnull
  protected Location location = NO_LOCATION;

  @CheckForNull
  private RunnableHooks hooks;

  @CheckForNull
  private Runnable remover;

  protected FileOrDirectory(@CheckForNull RunnableHooks hooks) {
    this.hooks = hooks;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return location;
  }

  @Nonnull
  public abstract String getPath();

  protected void setPermissions(@Nonnull File file, int permissions,
      @Nonnull FileOrDirectory.ChangePermission change) throws CannotSetPermissionException {
    if (change != ChangePermission.NOCHANGE) {
      // Set access
      if ((permissions & Permission.READ) != 0) {
        if (file.setReadable(true, change == ChangePermission.OWNER)) {
          logger.log(Level.FINE, "Set readable permission to {0} (''{1}'')",
              new Object[] {location.getDescription(), file.getAbsoluteFile()});
        } else {
          throw new CannotSetPermissionException((FileOrDirLocation) location, Permission.READ,
              change);
        }
      }

      if ((permissions & Permission.WRITE) != 0) {
        if (file.setWritable(true, change == ChangePermission.OWNER)) {
          logger.log(Level.FINE, "Set writable permission to {0} (''{1}'')",
              new Object[] {location.getDescription(), file.getAbsoluteFile()});
        } else {
          throw new CannotSetPermissionException((FileOrDirLocation) location, Permission.WRITE,
              change);
        }
      }

      if ((permissions & Permission.EXECUTE) != 0) {
        if (file.setExecutable(true, change == ChangePermission.OWNER)) {
          logger.log(Level.FINE, "Set executable permission to {0} (''{1}'')",
              new Object[] {location.getDescription(), file.getAbsoluteFile()});
        } else {
          throw new CannotSetPermissionException((FileOrDirLocation) location, Permission.EXECUTE,
              change);
        }
      }
    }
  }

  protected void checkPermissions(@Nonnull File file, int permissions)
      throws WrongPermissionException {
    if ((permissions & Permission.READ) != 0) {
      if (!file.canRead()) {
        throw new WrongPermissionException((FileOrDirLocation) location, Permission.READ);
      }
    }

    if ((permissions & Permission.WRITE) != 0) {
      if (!file.canWrite()) {
        throw new WrongPermissionException((FileOrDirLocation) location, Permission.WRITE);
      }
    }

    if ((permissions & Permission.EXECUTE) != 0) {
      if (!file.canExecute()) {
        throw new WrongPermissionException((FileOrDirLocation) location, Permission.EXECUTE);
      }
    }
  }

  protected synchronized void addRemover(@Nonnull final File file) {
    assert remover == null;

    if (hooks != null) {
      remover = new Runnable() {
        @Override
        public void run() {
          if (file.delete()) {
            logger.log(Level.FINE, "Remove {0} (''{1}'')",
                new Object[] {location.getDescription(), file.getAbsoluteFile()});
          } else {
            logger.log(Level.SEVERE,
                "Can not delete {0}",
                location.getDescription());
         }
        }
      };

      assert hooks != null;
      hooks.addHook(remover);
    }
  }

  protected synchronized void clearRemover() {
    if (remover != null) {
      assert hooks != null;

      hooks.removeHook(remover);
      remover = null;
      hooks   = null;
    }
  }
}