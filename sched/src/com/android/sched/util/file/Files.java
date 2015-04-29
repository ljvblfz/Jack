/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sched.util.file;

import com.android.sched.util.location.DirectoryLocation;

import java.io.File;

/**
 * Provides utility methods for working with files.
 */
public class Files {

  /** Maximum loop count when creating temp directories. */
  private static final int TEMP_DIR_ATTEMPTS = 10000;

  private Files() {}

  public static File createTempDir() throws CannotCreateFileException {
    File baseDir = new File(System.getProperty("java.io.tmpdir"));
    String baseName = System.currentTimeMillis() + "-";
    File tempDir = null;

    for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
      tempDir = new File(baseDir, baseName + counter);
      if (tempDir.mkdir()) {
        return tempDir;
      }
    }

    assert tempDir != null;
    throw new CannotCreateFileException(new DirectoryLocation(tempDir));
  }
}
