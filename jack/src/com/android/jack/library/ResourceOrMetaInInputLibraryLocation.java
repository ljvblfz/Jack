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

package com.android.jack.library;

import com.android.sched.util.location.Location;
import com.android.sched.vfs.VPath;

import javax.annotation.Nonnull;

/**
 * Location describing a resource or a meta in an input library.
 */
public abstract class ResourceOrMetaInInputLibraryLocation implements Location {

  @Nonnull
  protected final InputLibrary inputLib;

  @Nonnull
  protected final VPath path;

  public ResourceOrMetaInInputLibraryLocation(@Nonnull InputLibrary inputLib,
      @Nonnull VPath path) {
    this.inputLib = inputLib;
    this.path = path;
  }

  @Nonnull
  public InputLibrary getInputLibrary() {
    return inputLib;
  }
}
