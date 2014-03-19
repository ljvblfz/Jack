/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.cast.implicit002.jack;

public class Data {

  public static int conditonalIB(boolean cond, int a, byte b) {
    return cond ? a : b;
  }

  public static byte conditonalBB(boolean cond, byte a, byte b) {
    return cond ? a : b;
  }

  public static short conditonalBS(boolean cond, byte a, short b) {
    return cond ? a : b;
  }

  public static long conditonalLB(boolean cond, long a, byte b) {
    return cond ? a : b;
  }

  public static float conditonalIF(boolean cond, int a, float b) {
    return cond ? a : b;
  }

  public static double conditonalID(boolean cond, int a, double b) {
    return cond ? a : b;
  }

  public static double conditonalBD(boolean cond, byte a, double b) {
    return cond ? a : b;
  }

  public static double conditonalLD(boolean cond, long a, double b) {
    return cond ? a : b;
  }
}
