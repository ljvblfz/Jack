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

package com.android.jack.server;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.Nonnull;

/**
 * Thrown when a file is located on a file system without a supported access control.
 */
public class UnsupportedFileSystemAccessException extends IOException {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final Path path;

  public UnsupportedFileSystemAccessException(@Nonnull Path path) {
    this.path = path;
  }

  @Override
  public String getMessage() {
    return "'" + path.toString() + "' is not on a supported file system."
        + " Jack server must be installed on a filesystem allowing control of access"
        + " permissions";
  }
}
