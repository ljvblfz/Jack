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

package com.android.jack.dx.io;

import com.android.jack.dx.util.ByteInput;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;


public class EncodedValueCodec {

  private EncodedValueCodec() {}

  /**
   * Read a signed integer.
   *
   * @param zwidth byte count minus one
   */
  public static int readSignedInt(@Nonnull ByteInput in, @Nonnegative int zwidth) {
    int result = 0;
    for (int i = zwidth; i >= 0; i--) {
      result = (result >>> 8) | ((in.readByte() & 0xff) << 24);
    }
    result >>= (3 - zwidth) * 8;
    return result;
  }

  /**
   * Read an unsigned integer.
   *
   * @param zwidth byte count minus one
   * @param fillOnRight true to zero fill on the right; false on the left
   */
  public static int readUnsignedInt(@Nonnull ByteInput in, @Nonnegative int zwidth,
      boolean fillOnRight) {
    int result = 0;
    if (!fillOnRight) {
      for (int i = zwidth; i >= 0; i--) {
        result = (result >>> 8) | ((in.readByte() & 0xff) << 24);
      }
      result >>>= (3 - zwidth) * 8;
    } else {
      for (int i = zwidth; i >= 0; i--) {
        result = (result >>> 8) | ((in.readByte() & 0xff) << 24);
      }
    }
    return result;
  }

  /**
   * Read a signed long.
   *
   * @param zwidth byte count minus one
   */
  public static long readSignedLong(@Nonnull ByteInput in, @Nonnegative int zwidth) {
    long result = 0;
    for (int i = zwidth; i >= 0; i--) {
      result = (result >>> 8) | ((in.readByte() & 0xffL) << 56);
    }
    result >>= (7 - zwidth) * 8;
    return result;
  }

  /**
   * Read an unsigned long.
   *
   * @param zwidth byte count minus one
   * @param fillOnRight true to zero fill on the right; false on the left
   */
  public static long readUnsignedLong(@Nonnull ByteInput in, @Nonnegative int zwidth,
      boolean fillOnRight) {
    long result = 0;
    if (!fillOnRight) {
      for (int i = zwidth; i >= 0; i--) {
        result = (result >>> 8) | ((in.readByte() & 0xffL) << 56);
      }
      result >>>= (7 - zwidth) * 8;
    } else {
      for (int i = zwidth; i >= 0; i--) {
        result = (result >>> 8) | ((in.readByte() & 0xffL) << 56);
      }
    }
    return result;
  }
}