/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jill.utils.enums;

import com.android.jill.frontend.java.RetentionPolicy;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A helper class to encode {@link RetentionPolicy} enum values in Jayce format.
 */
public class RetentionPolicyIdHelper {

  @Nonnull
  private static byte[] ids;

  static {
    ids = new byte[5];
    ids[RetentionPolicy.SOURCE.ordinal()]  = 0;
    ids[RetentionPolicy.CLASS.ordinal()]   = 1;
    ids[RetentionPolicy.RUNTIME.ordinal()] = 2;
    ids[RetentionPolicy.UNKNOWN.ordinal()] = 4;
  }

  @Nonnegative
  public static byte getId(@Nonnull Enum<?> enumValue) {
    return ids[enumValue.ordinal()];
  }
}
