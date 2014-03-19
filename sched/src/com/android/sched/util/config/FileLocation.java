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

package com.android.sched.util.config;

import com.android.sched.util.log.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.annotation.Nonnull;

/**
 * Class describing a file.
 */
public class FileLocation extends Location {
  @Nonnull
  private final File file;

  public FileLocation(@Nonnull File file) {
    this.file = file;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return "file '" + file.getName() + "'";
  }

  @Nonnull
  public File getFile() {
    return file;
  }

  @Override
  public final boolean equals(Object obj) {
    try {
      return obj instanceof FileLocation &&
          ((FileLocation) obj).getFile().getCanonicalFile().equals(file.getCanonicalFile());
    } catch (IOException e) {
      LoggerFactory.getLogger().log(
          Level.WARNING, "Failed to get canonical path of the files to compare.", e);
      return false;
    }
  }

  @Override
  public final int hashCode() {
    try {
      return file.getCanonicalFile().hashCode();
    } catch (IOException e) {
      LoggerFactory.getLogger().log(
          Level.WARNING, "Failed to get canonical path of '" + file.getPath() + "'.", e);
      return -1;
    }
  }
}
