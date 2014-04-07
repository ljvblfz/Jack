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

package com.android.sched.vfs.direct;

import com.android.sched.util.config.FileLocation;
import com.android.sched.util.config.Location;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.vfs.AbstractVElement;
import com.android.sched.vfs.OutputVFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * An {@link OutputVFile} directly backed by a {@link File} directory.
 */
class OutputDirectFile extends AbstractVElement implements OutputVFile {

  private static boolean checkIfFileAlreadyExists = false;

  @Nonnull
  private final File file;

  public OutputDirectFile(@Nonnull File file) throws FileAlreadyExistsException {
    if (checkIfFileAlreadyExists && file.exists()) {
      throw new FileAlreadyExistsException(file.getAbsolutePath(), !file.isDirectory());
    }
    this.file = file;
  }

  @Nonnull
  @Override
  public OutputStream openWrite() throws FileNotFoundException {
    return new FileOutputStream(file);
  }

  @Nonnull
  @Override
  public String getName() {
    return file.getPath();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new FileLocation(file);
  }
}
