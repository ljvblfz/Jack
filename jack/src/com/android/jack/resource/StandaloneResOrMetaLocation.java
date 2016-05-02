/*
 * Copyright (C) 2016 The Android Open Source Project
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

import com.android.sched.util.location.Location;
import com.android.sched.vfs.VPath;

import javax.annotation.Nonnull;

/**
 * The {@link Location} of a standalone resource or meta file, i.e. that is imported from a
 * dedicated directory, not from a library.
 */
public abstract class StandaloneResOrMetaLocation implements Location {
  @Nonnull
  protected final Location baseLocation;
  @Nonnull
  protected final VPath path;

  public StandaloneResOrMetaLocation(@Nonnull Location baseLocation, @Nonnull VPath path) {
    this.baseLocation = baseLocation;
    this.path = path;
  }

  @Nonnull
  public Location getBaseLocation() {
    return baseLocation;
  }
}
