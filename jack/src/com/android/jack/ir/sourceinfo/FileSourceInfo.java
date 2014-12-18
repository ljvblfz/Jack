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

package com.android.jack.ir.sourceinfo;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Tracks file information for AST nodes.
 */
public class FileSourceInfo extends SourceInfo {

  /**
   * The original name of the file, which cannot be changed.
   */
  @Nonnull
  private final String originalFileName;

  /**
   * The current name of the file, which can be changed with the setter.
   */
  @Nonnull
  private String fileName;

  FileSourceInfo(@Nonnull String fileName) {
    this.originalFileName = fileName;
    this.fileName = fileName;
  }

  @Override
  @Nonnull
  public String getFileName() {
    return fileName;
  }

  public void setFileName(@Nonnull String fileName) {
    this.fileName = fileName;
  }

  /**
   * A hashcode method based on the original name of the file.
   */
  @Override
  public final int hashCode() {
    return originalFileName.hashCode();
  }

  /**
   * Two filenames are considered equals if their original names are equals.
   */
  @Override
  public final boolean equals(@CheckForNull Object o) {
    if (!(o instanceof FileSourceInfo)) {
      return false;
    }
    FileSourceInfo other = (FileSourceInfo) o;
    return originalFileName.equals(other.originalFileName);
  }

  @Override
  @Nonnull
  public FileSourceInfo getFileSourceInfo() {
    return this;
  }

  @Override
  @Nonnull
  public String toString() {
    return getFileName();
  }
}
