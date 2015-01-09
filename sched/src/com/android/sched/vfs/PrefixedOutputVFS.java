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

import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * An {@link OutputVFS} that is a part of another {@link OutputVFS}, prefixed by a path. The parent
 * {@link OutputVFS} is still the one that needs to be closed.
 */
public class PrefixedOutputVFS extends AbstractOutputVFS {

  public PrefixedOutputVFS(@Nonnull InputOutputVFS outputVFS, @Nonnull VPath path)
      throws NotFileOrDirectoryException, CannotCreateFileException {
    InputOutputVDir previousRootDir = outputVFS.getRootInputOutputVDir();
    OutputVDir newRootDir = null;
    try {
      newRootDir = previousRootDir.getInputVDir(path);
    } catch (NoSuchFileException e) {
      newRootDir = previousRootDir.createOutputVDir(path);
    }
    setRootDir(newRootDir);
  }

  @Override
  @Nonnull
  public String getPath() {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return getRootOutputVDir().getLocation();
  }

  @Override
  public void close() {
    // do not actually close
  }
}
