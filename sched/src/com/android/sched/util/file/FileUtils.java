/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.sched.util.file;

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.findbugs.SuppressFBWarnings;
import com.android.sched.util.location.DirectoryLocation;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Utility class for file operations.
 */
public final class FileUtils {

  /**
   * Hidden constructor
   */
  private FileUtils() {

  }

  public static void deleteDir(@Nonnull File dir) throws IOException {
    if (!dir.isDirectory()) {
      throw new AssertionError();
    }
    File[] fileList = dir.listFiles();
    if (fileList == null) {
      throw new ConcurrentIOException(new CannotListDirException(new DirectoryLocation(dir)));
    }
    for (File sub : fileList) {
      deleteSubElement(sub);
    }
    if (!dir.delete()) {
      throw new IOException("Failed to delete directory '" + dir.getPath() + "'");
    }
  }

  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  //Ignore delete return value: best effort
  private static void deleteSubElement(@Nonnull File dir) {
    if (dir.isDirectory()) {
      File[] fileList = dir.listFiles();
      if (fileList == null) {
        throw new ConcurrentIOException(new CannotListDirException(new DirectoryLocation(dir)));
      }
      for (File sub : fileList) {
        deleteSubElement(sub);
      }
    }
    dir.delete();
  }
}
