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

package com.android.jack.cast.implicit001.jack;

public class Data {

  public static float returnFloat(int a) {
    return a;
  }

  public static double assignDouble(int a) {
    double d = a;
    return addDouble(a, d);
  }

  public static double addDouble(int a, int b) {
    return a + b;
  }

  public static double addDouble(int a, double b) {
    return a + b;
  }

  public static float addFloat(int a, long b) {
    return a + b;
  }

  public static long addIntWithCall(int a, int b) {
    return addLong(a, b);
  }

  public static long addByteWithCall(byte a, byte b) {
    return addLong(a, b);
  }

  public static long addLong(int a, long b) {
    return a + b;
  }

  public static byte addByte(byte a, byte b) {
    byte c = (byte) (a + b);
    return c;
  }


  public static byte addByte() {
    byte a = 1;
    byte b = 2;
    a += b;
    return a;
  }

  public static int not(byte value) {
    return ~value;
  }

  public static long not(long value) {
    return ~value;
  }

  public static int minus(byte value) {
    return -value;
  }

  public static long minus(long value) {
    return -value;
  }

  public static double minus(double value) {
    return -value;
  }

  public static int minus(char value) {
    return -value;
  }

  public static double minusDouble(char value) {
    return -value;
  }

  public static double inc(double value) {
    return value++;
  }

  public static byte incPost(byte value) {
    return value++;
  }

  public static byte incPre(byte value) {
    return ++value;
  }

  public static long shr(long a, long b) {
    return a >> b;
  }
  public static long shr(long a, byte b) {
    return a >> b;
  }

  public static int arrayAccess(int[] a, byte b) {
    return a[b];
  }

  public static Object getObjectFromString(String s) {
    return getObject(s);
  }

  public static Object getObject(Object o) {
    return o;
  }

  private static byte[] arrayByte = new byte[10];

  public static byte setByteArray() {
    arrayByte[1] = (byte) -1;
    return arrayByte[1];
  }
}
