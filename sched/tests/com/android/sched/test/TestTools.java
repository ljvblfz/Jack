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

package com.android.sched.test;

import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.FileUtils;
import com.android.sched.util.file.Files;
import com.android.sched.util.location.FileLocation;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Common tools for Schedlib tests.
 */
public class TestTools {

  @Nonnull
  private static final String TMP_PREFIX = "test-sched-";

  @Nonnull
  public static File createTempFile(@Nonnull String prefix, @Nonnull String suffix)
      throws CannotCreateFileException, CannotChangePermissionException {
    File tmp = Files.createTempFile(TMP_PREFIX + prefix, suffix);
    tmp.deleteOnExit();
    return tmp;
  }

  @Nonnull
  public static File createTempDir() throws CannotCreateFileException,
      CannotChangePermissionException, IOException {
    try {
      final File tmpDir = Files.createTempDir(TMP_PREFIX);
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          try {
            FileUtils.deleteDir(tmpDir);
          } catch (IOException e) {
            throw new RuntimeException(new CannotDeleteFileException(new FileLocation(tmpDir)));
          }
        }
      });
      return tmpDir;
    } catch (IllegalStateException e) {
      throw new IOException(e);
    }
  }

}
