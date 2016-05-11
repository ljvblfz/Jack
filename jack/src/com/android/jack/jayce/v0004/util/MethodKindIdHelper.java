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

package com.android.jack.jayce.v0004.util;

import com.android.jack.ir.ast.MethodKind;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A helper class to encode {@link MethodKind} enum values in Jayce format.
 */
public class MethodKindIdHelper {

  @Nonnull
  private static MethodKind[] values;

  @Nonnull
  private static byte[] ids;

  static {
    assert MethodKind.values().length == 3;

    values = new MethodKind[3];
    values[0] = MethodKind.STATIC;
    values[1] = MethodKind.INSTANCE_NON_VIRTUAL;
    values[2] = MethodKind.INSTANCE_VIRTUAL;

    ids = new byte[3];
    ids[MethodKind.STATIC.ordinal()]               = 0;
    ids[MethodKind.INSTANCE_NON_VIRTUAL.ordinal()] = 1;
    ids[MethodKind.INSTANCE_VIRTUAL.ordinal()]     = 2;
  }

  @Nonnegative
  public static byte getId(@Nonnull MethodKind enumValue) {
    return ids[enumValue.ordinal()];
  }

  @Nonnull
  public static MethodKind getValue(@Nonnegative byte id) {
    return values[id];
  }
}
