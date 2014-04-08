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
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class describing a file or a directory location.
 */
public abstract class FileOrDirLocation extends Location {
  @Nonnull
  private final String path;
  @CheckForNull
  private String normalizedPath;

  public FileOrDirLocation(@Nonnull File file) {
    this.path = file.getPath();
  }

  public FileOrDirLocation(@Nonnull String path) {
    this.path = path;
  }

  @Nonnull
  public String getPath() {
    return path;
  }


  @Override
  public final boolean equals(@CheckForNull Object obj) {
    if (!(obj instanceof FileOrDirLocation)) {
      return false;
    }

    FileOrDirLocation location = (FileOrDirLocation) obj;
    this.ensureNormalized();
    location.ensureNormalized();
    assert this.normalizedPath != null;
    assert location.normalizedPath != null;

    return this.normalizedPath.equals(location.normalizedPath);
  }

  @Override
  public final int hashCode() {
    ensureNormalized();
    assert normalizedPath != null;

    return normalizedPath.hashCode();
  }

  private void ensureNormalized() {
    if (normalizedPath == null) {
      File file = new File(path);

      try {
        normalizedPath = file.getCanonicalPath();
      } catch (IOException e) {
        normalizedPath = file.getAbsolutePath();
      }
    }
  }
}
