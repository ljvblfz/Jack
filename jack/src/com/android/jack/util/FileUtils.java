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

package com.android.jack.util;

import com.android.sched.util.findbugs.SuppressFBWarnings;

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

  /**
   * Returns the file separator.
   */
  @Nonnull
  public static String getFileSeparator() {
    String fileSeparator = System.getProperty("file.separator", "/");
    return fileSeparator;
  }

  /**
   * Returns the working directory as a {@code File}.
   */
  @Nonnull
  public static File getWorkingDirectory() {
    String workingDirectoryPath = System.getProperty("user.dir");
    return new File(workingDirectoryPath);
  }

  /**
   * Creates the given {@code directory} if it does not exist.
   *
   * @param directory the non-null directory to create if it does not exist.
   * @throws NullPointerException if {@code directory} is null.
   * @throws IOException if the {@code directory} cannot be created.
   */
  public static void createIfNotExists(@Nonnull File directory) throws IOException {
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new IOException("Could not create directory \"" +
            directory.getPath() + "\"");
      }
    }
  }

  public static void deleteDir(@Nonnull File dir) throws IOException {
    if (!dir.isDirectory()) {
      throw new AssertionError();
    }
    for (File sub : dir.listFiles()) {
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
      for (File sub : dir.listFiles()) {
        deleteSubElement(sub);
      }
    }
    dir.delete();
  }
}
