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

package com.android.jack.box.test001.jack;

public class Box001 {

  @SuppressWarnings("boxing")
  public static Long get1(Long initial, int inc) {
    initial += inc;
    return initial;
  }

  @SuppressWarnings("boxing")
  public static Long get2(Long initial, int inc) {
    initial = inc + initial;
    return initial;
  }

  @SuppressWarnings("boxing")
  public static Boolean get3(Boolean b1, Boolean b2) {
    Boolean b = null;
    b = b1 & b2;
    return b;
  }

  @SuppressWarnings("boxing")
  public static Boolean get4(Boolean b1, Boolean b2) {
    Boolean b = null;
    b = b1 && b2;
    return b;
  }

  @SuppressWarnings("boxing")
  public static boolean get5(Boolean b1) {
    boolean b;
    b = b1;
    return b;
  }

  @SuppressWarnings("boxing")
  public static Long get6(Long initial, int inc) {
    initial = initial << inc;
    return initial;
  }

  @SuppressWarnings("boxing")
  public static Boolean get7(Boolean b1, Boolean b2) {
    Boolean b = b1 && b2;
    return b;
  }

  @SuppressWarnings("boxing")
  public static boolean get8() {
    boolean b = new Boolean(true);
    return b;
  }

  @SuppressWarnings("boxing")
  public static Boolean get9() {
    return true;
  }

  @SuppressWarnings("boxing")
  public static boolean get10() {
    return new Boolean(true);
  }

  @SuppressWarnings("boxing")
  public static Integer get11(int[] a) {
    return a[0];
  }

  @SuppressWarnings("boxing")
  public static int get12(Integer[] a) {
    return a[0];
  }

  @SuppressWarnings("boxing")
  public static int get13(int[] a, Integer index) {
    return a[index];
  }

  @SuppressWarnings("boxing")
  public static int[][] get14(Integer size1, Integer size2) {
    return new int[size1][size2];
  }

  @SuppressWarnings("boxing")
  private static int getP0(Integer a, Float b, Boolean c) {
    if (c) {
      return a + b.intValue();
    }
    return 0;
  }

  @SuppressWarnings("boxing")
  public static int get15(int a, float b) {
    return Box001.getP0(a, b, true);
  }

  private static int getP1(int a, float b, boolean c) {
    if (c) {
      return a + (int) b;
    }
    return 0;
  }

  @SuppressWarnings("boxing")
  public static int get16(Integer a, Float b) {
    return Box001.getP1(a, b, new Boolean(true));
  }

  @SuppressWarnings("boxing")
  public static int get17(Integer a) {
    return -a;
  }

  @SuppressWarnings("boxing")
  public static int get18(Integer a) {
    return --a;
  }

  @SuppressWarnings("boxing")
  public static boolean get19(Boolean a) {
    return !a;
  }

  @SuppressWarnings("boxing")
  public static int get20(Integer a) {
    int b = 0;
    return b += a ;
  }

  @SuppressWarnings("boxing")
  public static int get21(Short a) {
    int b = 0;
    return b += a ;
  }

  @SuppressWarnings("boxing")
  public static int get22(Integer a) {
    return ~a;
  }

  public static boolean get23(Integer a) {
    return a != null;
  }

  @SuppressWarnings("boxing")
  public static boolean get24(Boolean result) {
    return result != null ? result : false;
  }

  @SuppressWarnings("boxing")
  public static boolean get25(Boolean result) {
    return result == null ? false : result;
  }

  @SuppressWarnings("boxing")
  public static Object get26(long longValue) {
    return (int) longValue;
  }

  @SuppressWarnings({"boxing", "cast"})
  public static int get27(Integer longValue) {
    return (int) longValue;
  }

  @SuppressWarnings("boxing")
  public static String get28(short[] types, boolean value) {
    StringBuilder result = new StringBuilder();
    result.append(value ? "" : types[0]);
    result.append(!value ? "" : types[1]);
    return result.toString();
  }

  @SuppressWarnings("boxing")
  public static String get29(int a, boolean b) {
    return String.format("%d,%b", a, b);
  }

  @SuppressWarnings("boxing")
  public static Integer[] get30() {
    return new Integer[]{1, 2, 3};
  }

  @SuppressWarnings("boxing")
  public static int[] get31() {
    return new int[]{new Integer(1), new Integer(2), new Integer(3)};
  }

  @SuppressWarnings("boxing")
  public static int get32(Integer i) {
    switch (i) {
      case 1:
        return 1;
      case 2:
        return 2;
      default:
        return 3;
    }
  }

  @SuppressWarnings("boxing")
  public static int get33(Integer i) {
    if (i == 1) {
      return 1;
    }
    return 0;
  }

  @SuppressWarnings("boxing")
  public static int get34(Boolean b) {
    if (b) {
      return 1;
    }
    return 0;
  }

  @SuppressWarnings("boxing")
  public static int get35(Boolean b) {
    return (b ? 1 : 0);
  }

  @SuppressWarnings("boxing")
  public static int get36(Boolean b) {
    int result = 0;
    while (b) {
      result++;
      break;
    }
    return result;
  }

  @SuppressWarnings("boxing")
  public static int get37(Boolean b) {
    int result = 0;
    do {
      if (result == 1) {
        return 2;
      }
      result++;
    } while (b);
    return result;
  }

  @SuppressWarnings("boxing")
  public static int get38(Boolean b) {
    int result = 0;
    for (int i =0; b; i++) {
      if (result == 1) {
        return 2;
      }
      result++;
    }
    return result;
  }

  public static boolean get39(String a) {
    return "aa" == a;
  }

  @SuppressWarnings("boxing")
  public static int get41(Integer a, Integer b) {
    return a + b ;
  }

  @SuppressWarnings("boxing")
  public static double get42(Double a, Double b) {
    return a + b ;
  }

  public static final int NONE = 4;
  @SuppressWarnings({"boxing", "unused"})
  public static byte get43() {
    byte val = 1;
    Byte b = new Byte(val);
    if (b == null) {
      b = NONE;
    }
    return b;
  }

  @SuppressWarnings("boxing")
  public static int getValueFromObject(Object values) {
     return (Integer)values;
  }

  @SuppressWarnings("boxing")
  public static int get44() {
    int i = 10;
    return getValueFromObject(i == 10 ? i : "N/A");
  }

  @SuppressWarnings("boxing")
  public static Integer get45(Integer i1, Integer i2) {
    Integer i = null;
    i = i1 & i2;
    return i;
  }

  @SuppressWarnings("boxing")
  public static int get46(boolean b1, Boolean b2) {
    if (b1 == b2) {
      return 1;
    }
    return 0;
  }

  public static int get47(int val) {
    Integer val1 = new Integer(val);
    Integer val2 = new Integer(val);
    if (val1 == val2) {
      return 1;
    }
    return 2;
  }

  @SuppressWarnings({"boxing", "null"})
  public static int get48() {
    Integer integer = null;
    return integer;
  }

  public static long get49(boolean b) {
    Number result = (b) ? (Number)Integer.valueOf(1) : (Number)Long.valueOf(2);

    return result.longValue();
  }
}
