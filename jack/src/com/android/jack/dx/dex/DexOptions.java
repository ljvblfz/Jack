/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.jack.dx.dex;

import com.android.jack.util.AndroidApiLevel;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Container for options used to control details of dex file generation.
 */
public class DexOptions {

  /**
   * Enable alignment of 64-bit registers on Dalvik even registers.
   */
  public static final boolean ALIGN_64BIT_REGS = true;

  /** target API level */
  @Nonnull
  private final AndroidApiLevel androidApiLevel;

  /** force generation of jumbo opcodes */
  private final boolean forceJumbo;

  public DexOptions(@Nonnull AndroidApiLevel androidApiLevel, boolean forcejumbo) {
    this.androidApiLevel = androidApiLevel;
    this.forceJumbo = forcejumbo;
  }

  @Nonnegative
  public int getDexVersion() {
    return DexFormat.apiToDexVersion(androidApiLevel);
  }

  public boolean isForceJumbo() {
    return forceJumbo;
  }

  /**
   * Gets the dex file magic number corresponding to this instance.
   */
  @Nonnull
  public String getMagic() {
    return DexFormat.dexVersionToMagic(DexFormat.apiToDexVersion(androidApiLevel));
  }
}
