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

package com.android.jack.arithmetic.test003.jack;

/**
 * Division by zero test.
 */
public class ZeroUsage {

  public static int divByZeroInt() {
    return 1 / 0;
  }

  public static int modByZeroInt() {
    return 1 % 0;
  }

  public static long divByZeroLong() {
    return 1L / 0L;
  }

  public static long modByZeroLong() {
    return 1L % 0L;
  }

  public static float divByZeroFloat() {
    return 1f / 0f;
  }

  public static float modByZeroFloat() {
    return 1f % 0f;
  }

  public static double divByZeroDouble() {
    return 1d / 0d;
  }

  public static double modByZeroDouble() {
    return 1d % 0d;
  }
}
