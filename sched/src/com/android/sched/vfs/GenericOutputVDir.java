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
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * An {@link OutputVDir} implementation for a {@link GenericOutputVFS}.
 */
public class GenericOutputVDir implements OutputVDir {
  @Nonnull
  private final VDir dir;

  GenericOutputVDir(@Nonnull VDir dir) {
    this.dir = dir;
  }

  @Override
  public boolean isVDir() {
    return true;
  }

  @Override
  @Nonnull
  public String getName() {
    return dir.getName();
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return dir.getLocation();
  }

  @Override
  @Nonnull
  public OutputVFile createOutputVFile(@Nonnull VPath path) throws CannotCreateFileException {
    return new GenericOutputVFile(dir.createVFile(path));
  }
}