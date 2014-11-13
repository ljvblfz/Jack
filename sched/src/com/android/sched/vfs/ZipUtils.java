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
final class ZipUtils {

  static final char IN_ZIP_SEPARATOR = '/';

  private ZipUtils() {
    // do not instantiate
  }

  @Nonnull
  static String getSimpleName(@Nonnull ZipEntry entry) {
    String name = entry.getName();
    int index = name.lastIndexOf(IN_ZIP_SEPARATOR);
    if (index < 0) {
      return name;
    } else {
      return name.substring(index + 1);
    }
  }

}
