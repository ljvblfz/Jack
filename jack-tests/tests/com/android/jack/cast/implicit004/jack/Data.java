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

package com.android.jack.cast.implicit004.jack;

public class Data {

  public static byte m1(byte[] array, int index, byte or) {
    array[index] |= or;
    return array[index];
  }

  public static double m2(double[] array, int index, byte or1, byte or2) {
    array[index] = or1 | or2;
    return array[index];
  }

  public static char m3(char[] array, int index, byte or) {
    array[index] |= or;
    return array[index];
  }

  public static int m3(byte b2) {
    byte b1;
    byte b3;
    char c;
    int i;
    i = b1 = b3 = b2;
    return b1 + i;
  }

  public static byte m4(byte[] array, int index, byte b) {
    array[index] = (byte) ~b;
    return array[index];
  }

  public static byte m5(byte[] array, int index, byte b) {
    array[index] = ++b;
    return array[index];
  }

  public static int m6(byte b) {
    int i = -b;
    return i;
  }
}
