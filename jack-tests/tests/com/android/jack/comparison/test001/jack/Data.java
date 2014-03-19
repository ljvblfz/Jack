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

package com.android.jack.comparison.test001.jack;

import junit.framework.Assert;

/**
 * Comparison test.
 */
public class Data {

  public static boolean test001(int a) {
    boolean b = a > 0;
    return b;
  }

  public static boolean test002(int a) {
    boolean b;
    b = a >= 0;
    return b;
  }

  public static int test003(int a) {
    if (a < 0) {
      a = 1;
    }
    return a;
  }

  public static int test004(int a) {
    boolean b = (a > 0 && a < 10) || (a > 2 && a < 8);
    if (b) {
      a = 1;
    }
    return a;
  }

  public static boolean test005(int a) {
    return m((a > 0 || a < 10) && (a > 2 || a < 8));
  }

  private static boolean m(boolean b){
    return b;
  }

  public static boolean test006(Object o1, Object o2) {
    return o1 == o2;
  }

  public static int test007(int a, byte b) {
    if (a < b) {
      return b;
    }
    return a;
  }

  public static int test008(int count) {
    boolean allowFrontSur = false;
    boolean allowEndSur = true;
    if ((count & 1) == 1 && (!allowFrontSur || !allowEndSur)) {
      return 2;
    }
    return 3;
  }

  public static int test009() {
    boolean allowFrontSur = false;
    if (!allowFrontSur) {
      return 2;
    }
    return 3;
  }

  public static int test010() {
    boolean allowFrontSur = false;
    return !allowFrontSur ? 2 : 3;
  }

  public static int test011() {
    int value = 1;
    if (!(3 <= value)) {
      return 1;
    }
    return 2;
  }

  public static void testFloatCompare(float minus, float plus, float plus2, float nan) {

      if (minus > plus)
          Assert.assertTrue(false);
      if (plus < minus)
        Assert.assertTrue(false);
      if (plus == minus)
        Assert.assertTrue(false);
      if (plus != plus2)
        Assert.assertTrue(false);

      if (plus <= nan)
        Assert.assertTrue(false);
      if (plus >= nan)
        Assert.assertTrue(false);
      if (minus <= nan)
        Assert.assertTrue(false);
      if (minus >= nan)
        Assert.assertTrue(false);
      if (nan >= plus)
        Assert.assertTrue(false);
      if (nan <= plus)
        Assert.assertTrue(false);

      if (nan == nan)
        Assert.assertTrue(false);
  }
}
