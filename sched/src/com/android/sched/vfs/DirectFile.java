/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.InputStreamFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.OutputStreamFile;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * A {@code VFile} directly backed by a {@code java.io.File}.
 */
public class DirectFile extends AbstractVElement implements InputOutputVFile {

  @Nonnull
  private final File file;
  @Nonnull
  private final InputOutputVFS vfs;

  DirectFile(@Nonnull File file, @Nonnull InputOutputVFS vfs) {
    this.file = file;
    this.vfs = vfs;
  }

  @Nonnull
  @Override
  public InputStream openRead() throws WrongPermissionException {
    try {
      return new InputStreamFile(file.getPath()).getInputStream();
    } catch (NoSuchFileException e) {
      // we have already checked that the file exists when creating the VFile in the VDir
      throw new ConcurrentIOException(e);
    } catch (NotFileException e) {
      // we have already checked that this is not a directory when creating the VFile in the VDir
      throw new ConcurrentIOException(e);
    }
  }

  @Nonnull
  @Override
  public OutputStream openWrite() throws CannotCreateFileException, WrongPermissionException,
      NotFileException {
    return new OutputStreamFile(file.getPath(), null).getOutputStream();
  }

  @Nonnull
  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new FileLocation(file);
  }

  @Override
  public boolean isVDir() {
    return false;
  }

  @Override
  public void delete() throws CannotDeleteFileException {
    if (!file.delete()) {
      throw new CannotDeleteFileException(getLocation());
    }
  }
}
