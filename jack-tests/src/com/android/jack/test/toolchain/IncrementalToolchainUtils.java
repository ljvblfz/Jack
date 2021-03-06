/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.test.toolchain;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Utilities methods for incremental toolchains
 */
class IncrementalToolchainUtils {

  @CheckForNull
  private static File getFileToTouch(@Nonnull File... sources) throws Exception  {
    List<File> files = new ArrayList<File>();
    for (File source : sources) {
      AbstractTestTools.getJavaFiles(source, files, /* mustExist = */ false);
    }
    if (files.size() > 0) {
      return files.get(files.size() / 2);
    }
    return null;
  }

  public static void touchSourceFile(@Nonnull File... sources) throws Exception {

    File fileToTouch = getFileToTouch(sources);

    if (fileToTouch != null) {

      FileTime fileTime = Files.getLastModifiedTime(fileToTouch.toPath());

      Files.setLastModifiedTime(
          fileToTouch.toPath(), FileTime.fromMillis(System.currentTimeMillis()));

      if (Files.getLastModifiedTime(fileToTouch.toPath()).equals(fileTime)) {
        Thread.sleep(1000);
        Files.setLastModifiedTime(
            fileToTouch.toPath(), FileTime.fromMillis(System.currentTimeMillis()));
      }

      assert !fileTime.equals(Files.getLastModifiedTime(fileToTouch.toPath()));
    }
  }

}
