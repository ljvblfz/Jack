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

package com.android.jack.conditional.test001.jack;

public class Conditional {

  public static int test_conditionalCode001(int a) {
    int b = (a > 0) ? 1 : -1;
    return b;
  }

  public static int test_conditionalCode002(int a, int b) {
    int c = (a > 0) ? ((b > a) ? b : a) : -1;
    return c;
  }

  public static int test_conditionalCode003(int a, int b) {
    int c;
    if (((a > 0) ? ((b > a) ? b : a) : -1) > 0) {
      c = 1;
    } else {
      c = 3;
    }
    return c;
  }

  public static int test_conditionalCode004(int a, int b, int c, int d, int e, int f, int g) {
    int i =
        ((c > d) ? (e == f) : (g == a)) ? ((((b == f) ? e : g) > a) ? ((a < c) ? b : g) : a) : -1;
    return i;
  }

  public static int test_conditionalCode005(int a, int b) {
    int x = (a < b) ? (1) : (getValue((b > 0) ? 2 : 3 ));
    return x;
  }

  public static int test_conditionalCode006(boolean a) {
    int b;
    b = a ? 1 : -1;
    return b;
  }


  public static int test_conditionalCode007(boolean a) {
    byte b = 1;
    char c = 2;
    return m(a ? b : c);
  }

  public static int test_conditionalCode008(boolean a) {
    byte b = 1;
    return m(a ? b : null);
  }

  public static int test_conditionalCode009(boolean a) {
    byte b = 1;
    return m(a ? null : b);
  }

  public static int test_conditionalCode010(boolean a) {
    byte b = 1;
    short c = 1;
    return m(a ? c : b);
  }

  public static int test_conditionalCode011(boolean a) {
    byte b = 1;
   return m(a ? 256 : b);
  }

  public static int test_conditionalCode012(boolean a) {
    byte b = 1;
    return m(a ? 12 : b);
  }

  public static int test_conditionalCode013(boolean a) {
    char b = 1;
    return m(a ? 12 : b);
  }

  public static int test_conditionalCode014(boolean a) {
    char b = 1;
    return m(a ? -1 : b);
  }

  public static int test_conditionalCode015(boolean a) {
    short b = 1;
    return m(a ? -1 : b);
  }

  public static int test_conditionalCode016(boolean a) {
    short b = 1;
    return m(a ? 1234567890 : b);
  }

  public static int test_conditionalCode017(boolean a) {
    long b = 1;
    return m(a ? 1234567890L : b);
  }

  public static int test_conditionalCode018(boolean a) {
    long b = 1;
    return m(a ? 1234567890 : b);
  }

  public static int test_conditionalCode019() {
    int b = 1;
    return m(false ? 1234567890L : b);
  }

  public static int test_conditionalCode020(boolean a) {
    Object b = 1;
    return m(a ? "" : b);
  }

  public static int test_conditionalCode021(boolean a) {
    Object b = 1;
    return m(false ? "" : b);
  }

  public static int test_conditionalCode022(boolean a) {
    String b = "1";
    return m(a ? "" : b);
  }

  public static int test_conditionalCode023(boolean a) {
    return m(a ? null : null);
  }

  public static int test_conditionalCode024() {
    return m(false ? "" : null);
  }

  public static int test_conditionalCode025(boolean a) {
    float b = 1;
    return m(a ? 1234567890 : b);
  }

  public static int test_conditionalCode026(boolean a) {
    float b = 1;
    return m(a ? 1234567890f : b);
  }

  public static int test_conditionalCode027(boolean a) {
    float b = 1;
    return m(a ? 1234567890d : b);
  }

  public static int test_conditionalCode028(boolean a) {
    return m(a ? 1234567890d : 1.0f);
  }

  public static int test_conditionalCode029() {
    return m(false ? 1234567890d : 1.0f);
  }

  public static int test_conditionalCode030(boolean a) {
    double b = 1;
    return m(a ? 1234567890d : b);
  }

  public static int m(byte a) {
    return 1;
  }

  public static int m(char a) {
    return 2;
  }

  public static int m(short a) {
    return 3;
  }

  public static int m(int a) {
    return 4;
  }

  public static int m(Object a) {
    return 5;
  }

  public static int m(long a) {
    return 6;
  }

  public static int m(float a) {
    return 7;
  }

  public static int m(double a) {
    return 8;
  }

  public static int m(String a) {
    return 9;
  }

  public static int getValue(int c) {
    return 1;
  }

}
