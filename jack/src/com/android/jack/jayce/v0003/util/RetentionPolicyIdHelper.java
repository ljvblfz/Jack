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

package com.android.jack.jayce.v0003.util;

import com.android.jack.ir.ast.JRetentionPolicy;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A helper class to encode {@link JRetentionPolicy} enum values in Jayce format.
 */
public class RetentionPolicyIdHelper {

  @Nonnull
  private static JRetentionPolicy[] values;

  @Nonnull
  private static byte[] ids;

  static {
    values = new JRetentionPolicy[5];
    values[0] = JRetentionPolicy.SOURCE;
    values[1] = JRetentionPolicy.CLASS;
    values[2] = JRetentionPolicy.RUNTIME;
    values[3] = JRetentionPolicy.SYSTEM;
    values[4] = JRetentionPolicy.UNKNOWN;

    ids = new byte[5];
    ids[JRetentionPolicy.SOURCE.ordinal()]  = 0;
    ids[JRetentionPolicy.CLASS.ordinal()]   = 1;
    ids[JRetentionPolicy.RUNTIME.ordinal()] = 2;
    ids[JRetentionPolicy.SYSTEM.ordinal()]  = 3;
    ids[JRetentionPolicy.UNKNOWN.ordinal()] = 4;
  }

  @Nonnegative
  public static byte getId(@Nonnull JRetentionPolicy enumValue) {
    return ids[enumValue.ordinal()];
  }

  @Nonnull
  public static JRetentionPolicy getValue(@Nonnegative byte id) {
    return values[id];
  }
}
