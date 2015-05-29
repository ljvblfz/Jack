/*
 * Copyright (C) 2015 The Android Open Source Project
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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {@link Toolchain} uses Jack through v01 API and perform incremental
 * compilation. Compilation are thus performed twice, while touching a source file
 * between the two calls.
 */
public class JackApiV01IncrementalToolchain
    extends JackApiV01Toolchain implements IncrementalToolchain {

  JackApiV01IncrementalToolchain(@Nonnull File jackPrebuilt) {
    super(jackPrebuilt);
  }

  @Override
  public void srcToExe(@Nonnull File out, boolean zipFile, @Nonnull File... sources)
      throws Exception {
    setIncrementalFolder(AbstractTestTools.createTempDir());
    super.srcToExe(out, zipFile, sources);
    Thread.sleep(1000);
    touchSourceFile(sources);
    super.srcToExe(out, zipFile, sources);
  }

  @Override
  public void srcToLib(@Nonnull File out, boolean zipFiles, @Nonnull File... sources)
      throws Exception {
    setIncrementalFolder(AbstractTestTools.createTempDir());
    super.srcToLib(out, zipFiles, sources);
    Thread.sleep(1000);
    touchSourceFile(sources);
    super.srcToLib(out, zipFiles, sources);
  }

  private void touchSourceFile(@Nonnull File... sources) throws Exception {
    List<File> files = new ArrayList<File>();
    for (File source : sources) {
      AbstractTestTools.getJavaFiles(source, files, /* mustExist = */ false);
    }
    if (files.size() > 0) {
      File fileToTouch = files.get(files.size() / 2);
      if (!fileToTouch.setLastModified(System.currentTimeMillis())) {
        throw new AssertionError("Could not touch file '" + fileToTouch.getPath() + "'");
      }
    }
  }

}
