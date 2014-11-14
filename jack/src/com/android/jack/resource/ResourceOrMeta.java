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

package com.android.jack.resource;

import com.android.sched.util.location.HasLocation;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import javax.annotation.Nonnull;

/**
 * Represents a resource or a meta.
 */
public abstract class ResourceOrMeta implements HasLocation {

  @Nonnull
  private VPath path;

  @Nonnull
  private InputVFile vFile;

  public ResourceOrMeta(@Nonnull VPath path, @Nonnull InputVFile vFile) {
    this.vFile = vFile;
    this.path = path;
  }

  @Nonnull
  public InputVFile getVFile() {
    return vFile;
  }

  public void setVFile(@Nonnull InputVFile vFile) {
    this.vFile = vFile;
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return vFile.getLocation();
  }

  @Nonnull
  public VPath getPath() {
    return path;
  }

  public void setPath(@Nonnull VPath path) {
    this.path = path;
  }
}
