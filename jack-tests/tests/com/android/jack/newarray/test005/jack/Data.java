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

package com.android.jack.newarray.test005.jack;

/**
 * Tests should work with fill-new-array instruction.
 */
public class Data {

  public static int[][] getArray1() {
    int[][] a = new int[1][1];
    return a;
  }

  public static int[] getArray2() {
    int[] a = new int[] {1,2,3,4,5};
    return a;
  }

  public static int[] getArray3() {
    int[] a = new int[] {1,2,3,4,5,6};
    return a;
  }

  public static byte[] getArray4() {
    byte[] a = new byte[] {-1,2};
    return a;
  }

  public static int[][][][] getArray5() {
    int[][][][] a = new int[1][2][3][4];
    a[0][1][2][3] = 10;
    return a;
  }

  public static Object[] getArray6() {
    Object[] a = new Object[] {null, null};
    return a;
  }
}
