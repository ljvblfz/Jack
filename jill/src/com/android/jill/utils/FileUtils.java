/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jill.utils;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Utilities related to file support.
 */
public class FileUtils {

  @Nonnull
  public static final String JAVA_BINARY_FILE_EXTENSION = ".class";

  @Nonnull
  public static final String JAR_FILE_EXTENSION = ".jar";

  public static void getJavaBinaryFiles(@Nonnull File file, @Nonnull List<File> binaryFiles) {
    if (file.isDirectory()) {
      File allFiles[] = file.listFiles();
      for (File aFile : allFiles) {
        getJavaBinaryFiles(aFile, binaryFiles);
      }
    } else if (isJavaBinaryFile(file)) {
      binaryFiles.add(file.getAbsoluteFile());
    }
  }

  public static boolean isJavaBinaryFile(@Nonnull File file) {
    return isJavaBinaryFile(file.getName());
  }

  public static boolean isJavaBinaryFile(@Nonnull String fileName) {
    return fileName.endsWith(FileUtils.JAVA_BINARY_FILE_EXTENSION);
  }

  public static boolean isJarFile(@Nonnull File file) {
    return file.getName().endsWith(FileUtils.JAR_FILE_EXTENSION);
  }
}
