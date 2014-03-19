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

package com.android.jack.threeaddress.jack;


public class ThreeAddressCode001 {

  public static int threadAddressCode001() {
    int a = 1;
    int b = 2;
    int c = 3;
    int d;
    d = a + b + c;
    return d;
  }

  public static int threadAddressCode002(int a, int b, int c) {
    int t = a + b + c;
    return t;
  }

  public static int threadAddressCode003(int[] a, int[] b, int[] c) {
    int t = a[0] + b[1] + c[2];
    return t;
  }

  public static int threadAddressCode004(int c) {
    int t = ThreeAddressCode001.getValue(c + 1);
    return t;
  }

  public static int threeAddressCode005(int a, int b) {
    int c;
    c = a + b + new ThreeAddressCodeUtil001() {

      @Override
      public int f(int a, int b) {
        return a + b + 1;
      }
    }.f(a, b);
    return c;
  }

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
    int x = (a < b) ? (1) : (getValue((b > 0) ? 2 : 3));
    return x;
  }

  public static int getValue(int c) {
    return 1;
  }
}
