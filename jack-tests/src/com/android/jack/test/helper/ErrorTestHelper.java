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

package com.android.jack.test.helper;

import com.android.jack.test.toolchain.AbstractTestTools;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This class is used by error tests, it basically handles the directory structure.
 */
public class ErrorTestHelper {

  @Nonnull
  private final File testingFolder;
  @Nonnull
  private final File sourceFolder;
  @Nonnull
  private final File jackFolder;
  @Nonnull
  private final File outputDexFolder;

  public ErrorTestHelper() throws IOException {
    this.testingFolder = AbstractTestTools.createTempDir();
    this.sourceFolder = new File(testingFolder, "src");
    if (!this.sourceFolder.mkdirs()) {
      throw new IOException("Failed to create folder " + this.sourceFolder.getPath());
    }
    this.jackFolder = new File(testingFolder, "jack");
    if (!this.jackFolder.mkdirs()) {
      throw new IOException("Failed to create folder " + this.jackFolder.getPath());
    }
    this.outputDexFolder = new File(this.testingFolder, "dex");
    if (!this.outputDexFolder.mkdirs()) {
      throw new IOException("Failed to create folder " + this.outputDexFolder.getPath());
    }
  }

  @Nonnull
  public File getSourceFolder() {
    return sourceFolder;
  }

  @Nonnull
  public File getJackFolder() {
    return jackFolder;
  }

  @Nonnull
  public File getTestingFolder() {
    return testingFolder;
  }

  @Nonnull
  public File getOutputDexFolder() {
    return outputDexFolder;
  }
}
