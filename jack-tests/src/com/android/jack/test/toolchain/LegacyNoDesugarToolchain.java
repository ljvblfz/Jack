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
import java.util.List;

import javax.annotation.Nonnull;

/**
 * The legacy android toolchain without desugar.
 */
public class LegacyNoDesugarToolchain extends LegacyToolchain {


  LegacyNoDesugarToolchain(@Nonnull File legacyCompilerPrebuilt,
      @Nonnull List<File> legacyCompilerBootclasspath,
      @Nonnull File jarjarPrebuilt,
      @Nonnull File proguardPrebuilt, @Nonnull File dxPrebuilt) {
    super(legacyCompilerPrebuilt, legacyCompilerBootclasspath, jarjarPrebuilt, proguardPrebuilt,
        dxPrebuilt);
  }

  @Override
  boolean isDesugarEnabled() {
    return false;
  }
}
