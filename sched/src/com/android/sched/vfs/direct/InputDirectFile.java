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
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.vfs.AbstractVElement;
import com.android.sched.vfs.InputVFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.annotation.Nonnull;

/**
 * A {@code VFile} directly backed but a {@code java.io.File}.
 */
public class InputDirectFile extends AbstractVElement implements InputVFile {

  @Nonnull
  private final File file;

  public InputDirectFile(@Nonnull File file) throws NotFileOrDirectoryException {
    if (!file.isFile()) {
      throw new NotFileOrDirectoryException(file.getAbsolutePath(), true);
    }
    this.file = file;
  }

  @Nonnull
  @Override
  public InputStream openRead() throws FileNotFoundException {
    return new FileInputStream(file);
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
}
