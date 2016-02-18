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

import com.android.sched.vfs.VPath;

import javax.annotation.Nonnull;

/**
 * Location describing a resource in an input library.
 */
public class ResourceInInputLibraryLocation extends ResourceOrMetaInInputLibraryLocation {

  public ResourceInInputLibraryLocation(@Nonnull InputLibraryLocation inputLibLoc,
      @Nonnull VPath path) {
    super(inputLibLoc, path);
  }

  @Override
  @Nonnull
  public String getDescription() {
    return inputLibLoc.getDescription() + ", resource '" + path.getPathAsString('/') + '\'';
  }
}
