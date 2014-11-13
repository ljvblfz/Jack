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
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * A VFS directory backed by a real filesystem directory.
 */
public class DirectVFS extends AbstractInputOutputVFS implements ParallelOutputVFS {
  @Nonnull
  private final Directory dir;

  public DirectVFS(@Nonnull Directory dir) {
    this.dir = dir;

    try {
      setRootDir(new DirectDir(dir.getFile(), this));
    } catch (NotFileOrDirectoryException e) {
      throw new ConcurrentIOException(e);
    }
  }

  @Override
  public void close() {
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return dir.getLocation();
  }
}
