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

package com.android.jack.unary.test002.jack;

/**
 * Unary test.
 */
public class Unary {

  public static int prefixInc(int a) {
    return ++a;
  }

  public static int prefixDec(int a) {
    return --a;
  }

  public static int postfixInc(int a) {
    return a++;
  }

  public static int postfixDec(int a) {
    return a--;
  }

  public static int postfixInc2(int a) {
    a++;
    return a;
  }

  static int sfield = 3;

  public static int m(int i) {
    return sfield;
  }

  public static int postfixInc3() {
    int b = m(sfield++);
    return b;
  }

  int field = 3;

  public static Unary get(Unary u) {
    u.field++;
    return u;
  }

  public static int postfixInc4(Unary u) {
    get(u).field++;
    return u.field;
  }

  public static int[] postfixIncArray(int[] tab) {
    tab[1]++;
    return tab;
  }

  public static int sfield2 = 5;
  public static int[] tab;

  public static int[] m1() {
    sfield2++;
    return tab;
  }

  public static int m2() {
    sfield2 = sfield2 * 2;
    return 1;
  }

  public static int[] postfixIncArray2() {
    m1()[m2()]++;
    return tab;
  }

  private static boolean get(boolean val1, boolean val2) {
    return val1 & val2;
  }
  public static boolean notIntoMethodCall(boolean val1, boolean val2) {
    return get(!val1, !val2);
  }

  public static char next() {
    int offset = 1;
    String string = "abc";
    return string.charAt(++offset);
  }
}
