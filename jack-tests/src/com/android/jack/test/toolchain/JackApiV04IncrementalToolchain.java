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

package com.android.jack.test.toolchain;

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {@link Toolchain} uses Jack through v04 API and perform incremental
 * compilation. Compilation are thus performed twice, while touching a source file
 * between the two calls.
 */
public class JackApiV04IncrementalToolchain
    extends JackApiV04Toolchain implements IncrementalToolchain {

  JackApiV04IncrementalToolchain(@CheckForNull File jackPrebuilt) {
    super(jackPrebuilt);
  }

  @Override
  public void srcToExe(@Nonnull File out, boolean zipFile, @Nonnull File... sources)
      throws Exception {
    setIncrementalFolder(AbstractTestTools.createTempDir());
    super.srcToExe(out, zipFile, sources);
    IncrementalToolchainUtils.touchSourceFile(sources);
    super.srcToExe(out, zipFile, sources);
  }

  @Override
  public void srcToLib(@Nonnull File out, boolean zipFiles, @Nonnull File... sources)
      throws Exception {
    setIncrementalFolder(AbstractTestTools.createTempDir());
    super.srcToLib(out, zipFiles, sources);
    IncrementalToolchainUtils.touchSourceFile(sources);
    super.srcToLib(out, zipFiles, sources);
  }

}
