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

import javax.annotation.Nonnull;

/**
 * A {@link Toolchain} to produce libs and executable for the Android platform.
 */
public abstract class AndroidToolchain extends Toolchain {

  @Override
  @Nonnull
  public final String getExeExtension() {
    return ".dex";
  }

  @Override
  @Nonnull
  public final String getLibraryExtension() {
    return ".jar";
  }

  @Nonnull
  public final String getBinaryFileName() {
    return "classes.dex";
  }

  @Nonnull
  public abstract AndroidToolchain disableDxOptimizations();

  @Nonnull
  public abstract AndroidToolchain enableDxOptimizations();

}
