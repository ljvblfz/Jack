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

package com.android.sched.vfs;

import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

/**
 * Utilities for Zip VFS implementations.
 */
public final class ZipUtils {

  public static final char ZIP_SEPARATOR = '/';

  static final String ZIP_SEPARATOR_STRING = "/";

  static final String ROOT_ENTRY_NAME = "";

  private ZipUtils() {
    // do not instantiate
  }

  @Nonnull
  static String getFileSimpleName(@Nonnull ZipEntry entry) {
    String name = entry.getName();
    assert !name.endsWith(ZIP_SEPARATOR_STRING);
    int index = name.lastIndexOf(ZIP_SEPARATOR);
    if (index < 0) {
      return name;
    } else {
      return name.substring(index + 1);
    }
  }

  @Nonnull
  static String getDirSimpleName(@Nonnull ZipEntry entry) {
    String name = entry.getName();
    if (name.equals(ROOT_ENTRY_NAME)) { // necessary special case for root zip entry
      return "";
    }
    assert name.endsWith(ZIP_SEPARATOR_STRING);
    int index = name.lastIndexOf(ZIP_SEPARATOR, name.length() - 2);
    int startIndex = index + 1; // if '/' was not found, startIndex will be 0
    return name.substring(startIndex, name.length() - 1);
  }

}
