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

import com.google.common.io.Files;

import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotSetPermissionException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.FileUtils;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * A {@link VFS} backed by a real filesystem directory, compressed into a zip archive when closed.
 */
public class ReadWriteZipFS implements VFS {

  @Nonnull
  private final VFSToVFSWrapper vfs;
  @Nonnull
  private final File dir;

  public ReadWriteZipFS(@Nonnull OutputZipFile file)
      throws NotDirectoryException,
      WrongPermissionException,
      CannotSetPermissionException,
      NoSuchFileException,
      FileAlreadyExistsException,
      CannotCreateFileException {
    int permissions = Permission.READ | Permission.WRITE;
    dir = Files.createTempDir();
    DirectFS workVFS = new DirectFS(new Directory(dir.getPath(), null, Existence.MUST_EXIST,
        permissions, ChangePermission.NOCHANGE), permissions);
    WriteZipFS finalVFS = new WriteZipFS(file);
    this.vfs = new VFSToVFSWrapper(workVFS, finalVFS);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return vfs.getLocation();
  }

  @Override
  public void close() throws IOException {
    vfs.close();
    if (dir.exists()) {
      FileUtils.deleteDir(dir);
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
  public VDir getRootDir() {
    return vfs.getRootDir();
  }

  @Override
  public boolean needsSequentialWriting() {
    return vfs.needsSequentialWriting();
  }
}
