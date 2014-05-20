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

import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.AbstractVElement;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * An {@link OutputVDir} using a real directory.
 */
public class OutputDirectDir extends AbstractVElement implements OutputVDir {

  @Nonnull
  private final File dir;

  public OutputDirectDir(@Nonnull Directory dir) {
    this.dir = dir.getFile();
  }

  @Nonnull
  @Override
  public String getName() {
    return dir.getName();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new FileLocation(dir);
  }

  @Override
  @Nonnull
  public OutputVFile createOutputVFile(@Nonnull VPath path) throws CannotCreateFileException,
      FileAlreadyExistsException {
    File file = new File(dir, path.getPathAsString(getSeparator()));
    if (!file.getParentFile().mkdirs() && !file.getParentFile().isDirectory()) {
      throw new CannotCreateFileException(new DirectoryLocation(file.getParentFile()));
    }
    return new OutputDirectFile(file);
  }

  @Override
  public char getSeparator() {
    return File.separatorChar;
  }

}
