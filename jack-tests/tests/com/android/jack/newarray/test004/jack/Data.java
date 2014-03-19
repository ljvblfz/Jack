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

package com.android.jack.newarray.test004.jack;

public class Data {

  public static byte[] getByteArray1() {
    byte[] b = new byte[]{1,-1};
    return b;
  }

  public static byte[] getByteArray() {
    byte[] b = new byte[]{(byte)1,2,3};
    return b;
  }

  public static char[] getCharArray() {
    char[] c = new char[]{166,21236,36666};
    return c;
  }

  public static short[] getShortArray() {
    short[] s = new short[]{11566,21236,3666};
    return s;
  }

  public static int[] getIntArray() {
    int[] i = new int[]{11566,21236,366666};
    return i;
  }

  public static float[] getFloatArray() {
    float[] f = new float[]{1.2f, 3, 1.9f};
    return f;
  }

  public static float[] getFloatArray2() {
    float[] f = new float[]{1l, 12345678945678978l, 1.9f};
    return f;
  }

  public static double[] getDoubleArray() {
    double[] d = new double[]{1.2f,2,3};
    return d;
  }

  public static double[] getDoubleArray2() {
    double[] d = new double[]{1l,2l,3};
    return d;
  }
}
