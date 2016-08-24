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

package com.android.sched.util.location;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * Class describing a directory location.
 */
public class DirectoryLocation extends FileOrDirLocation {

  /**
   * Creates a {@link DirectoryLocation} using a {@link String} path.
   */
  public DirectoryLocation(@Nonnull String path) {
    super(path);
  }

  /**
   * Creates a {@link DirectoryLocation} using a {@link File}.
   * The constructor {@link #DirectoryLocation(String)} should be preferred when the original path
   * String is available. Using a {@link File} may transform the original path (e.g. replacing '/'
   * in the input by '\' on Windows).
   */
  public DirectoryLocation(@Nonnull File file) {
    super(file);
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "directory '" + getPath() + "'";
  }
}
