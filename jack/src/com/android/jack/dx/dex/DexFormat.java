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

import com.android.jack.dx.util.DexException;
import com.android.jack.util.AndroidApiLevel;

import java.util.Arrays;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Constants that show up in and are otherwise related to {@code .dex}
 * files, and helper methods for same.
 */
public final class DexFormat {
  private DexFormat() {}

  /**
   * API level to target in order to produce file format for N
   */
  @Nonnegative
  private static final int API_ANDROID_N = 24;

  /**
   * file name of the primary {@code .dex} file inside an
   * application or library {@code .jar} file
   */
  @Nonnull
  public static final String DEX_IN_JAR_NAME = "classes.dex";

  /** common prefix for all dex file "magic numbers" */
  @Nonnull
  public static final String MAGIC_PREFIX = "dex\n";

  /** common suffix for all dex file "magic numbers" */
  @Nonnull
  public static final String MAGIC_SUFFIX = "\0";

  @Nonnull
  private static final String DEX_VERSION_PREFIX = "0";

  @Nonnegative
  public static final int O_BETA2_DEX_VERSION = 38;

  @Nonnegative
  public static final int ANDROID_N_DEX_VERSION = 37;

  @Nonnegative
  public static final int ANDROID_PRE_N_DEX_VERSION = 35;

  /**
   * value used to indicate endianness of file contents
   */
  @Nonnegative
  public static final int ENDIAN_TAG = 0x12345678;

  /**
   * Maximum addressable field or method index.
   * The largest addressable member is 0xffff, in the "instruction formats" spec as field@CCCC or
   * meth@CCCC.
   */
  @Nonnegative
  public static final int MAX_MEMBER_IDX = 0xFFFF;

  /**
   * Maximum addressable type index.
   * The largest addressable type is 0xffff, in the "instruction formats" spec as type@CCCC.
   */
  @Nonnegative
  public static final int MAX_TYPE_IDX = 0xFFFF;

  /**
   * Maximum addressable prototype index. The largest addressable prototype is 0xffff, in the
   * "instruction formats" spec as prototype@CCCC.
   */
  @Nonnegative
  public static final int MAX_PROTOTYPE_IDX = 0xFFFF;

  /**
   * Returns the dex version corresponding to the given magic number,
   * or {@code -1} if the given array is not a well-formed dex file
   * magic number.
   */
  @Nonnegative
  public static int getDexVersion(@Nonnull byte[] magic) {
    if (magic.length != 8) {
      return -1;
    }

    if ((magic[0] != 'd') || (magic[1] != 'e') || (magic[2] != 'x') || (magic[3] != '\n')
        || (magic[7] != '\0')) {
      return -1;
    }

    String version = "" + ((char) magic[4]) + ((char) magic[5]) + ((char) magic[6]);

    if (version.equals(DEX_VERSION_PREFIX + O_BETA2_DEX_VERSION)) {
      return O_BETA2_DEX_VERSION;
    } else if (version.equals(DEX_VERSION_PREFIX + ANDROID_N_DEX_VERSION)) {
      return ANDROID_N_DEX_VERSION;
    } else if (version.equals(DEX_VERSION_PREFIX + ANDROID_PRE_N_DEX_VERSION)) {
      return ANDROID_PRE_N_DEX_VERSION;
    }

    throw new DexException("Unexpected magic: " + Arrays.toString(magic));
  }

  /**
   * Returns the magic number corresponding to the given dex version.
   */
  @Nonnull
  public static String dexVersionToMagic(@Nonnegative int dexVersion) {
    return MAGIC_PREFIX + DEX_VERSION_PREFIX + dexVersion + MAGIC_SUFFIX;
  }


  /**
   * Returns the dex version for the given target API level.
   */
  @Nonnegative
  public static int apiToDexVersion(@Nonnull AndroidApiLevel targetApiLevel) {
    if (targetApiLevel.isReleasedLevel()) {
      switch (targetApiLevel.getReleasedLevel()) {
        case API_ANDROID_N: {
          return ANDROID_N_DEX_VERSION;
        }
        default: {
          return ANDROID_PRE_N_DEX_VERSION;
        }
      }
    } else {
      switch (targetApiLevel.getProvisionalLevel()) {
        case O_BETA1: {
          return ANDROID_N_DEX_VERSION;
        }
        case O_BETA2: {
          return O_BETA2_DEX_VERSION;
        }
        default: {
          throw new UnsupportedOperationException();
        }
      }
    }
  }
}
