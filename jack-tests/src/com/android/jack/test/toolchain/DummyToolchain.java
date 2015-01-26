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

package com.android.jack.test.toolchain;

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This {link AndroidToolchain} actually does nothing. It can be used in a comparison
 * test to simulate one of the toolchain whereas the data to compare is already
 * available, such as an expected result in a text file for instance.
 */
public class DummyToolchain extends AndroidToolchain {

  @Nonnull
  private final File[] dummyBootclasspath = new File[0];

  public DummyToolchain() {}

  @Override
  public void srcToExe(@Nonnull File out, boolean zipFile,
      @Nonnull File... sources) throws Exception {}

  @Override
  public void srcToLib(@Nonnull File out, boolean zipFiles,
      @Nonnull File... sources) throws Exception {}

  @Override
  public void libToExe(@Nonnull File[] in, @Nonnull File out, boolean zipFile) throws Exception {}

  @Override
  public void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception {}

  @Override
  @Nonnull
  public File[] getDefaultBootClasspath() {
    return dummyBootclasspath;
  }

  @Override
  @Nonnull
  public DummyToolchain disableDxOptimizations() {
    // Do nothing
    return this;
  }

  @Override
  @Nonnull
  public DummyToolchain enableDxOptimizations() {
    // Do nothing
    return this;
  }

  @Override
  @Nonnull
  public String getLibraryExtension() {
    return ".dummy";
  }

}
