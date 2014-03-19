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

package com.android.jack.optimizations.notsimplifier.test001.jack;

public class NotSimplifier {

  public static boolean test001(int i1, int i2) {
    // Check that expression is replaced by i1 != i2
    if (!(i1 == i2)) {
        return false;
    } else {
      return true;
    }
  }

  public static boolean test002(int i1, int i2) {
    // Check that expression is replaced by i1 >= i2
    if (! (i1 < i2)) {
        return true;
    } else {
      return false;
    }
  }

  public static boolean test003(int i1, int i2, int i3, int i4) {
    // Check that expression is replaced by i1 >= i2 || i3 == i4
    if (!(i1 < i2 && i3 != i4)) {
        return true;
    } else {
      return false;
    }
  }

  public static boolean test004(int i1, int i2, boolean b1) {
    // Check that expression is replaced by i1 >= i2 || b
    if (!(i1 < i2 && !b1)) {
        return true;
    } else {
      return false;
    }
  }

  public static boolean test005(int i1, int i2, int i3, int i4) {
    // Check that expression is replaced by i1 >= i2 || i3 != i4
    if (!(i1 < i2 && !(i3 != i4))) {
        return true;
    } else {
      return false;
    }
  }

  private static boolean getBoolean() {
    return true;
  }

  public static boolean test006(int i1, int i2) {
    // Check that expression is not replaced by !getBoolean() || i1 != i2
    if (!(getBoolean() && i1 == i2)) {
        return true;
    } else {
      return false;
    }
  }

  public static boolean test007(int i1, int i2) {
    // Check that expression is replaced by getBoolean() && i1 != i2
    if (getBoolean() && !(i1 == i2)) {
        return true;
    } else {
      return false;
    }
  }

  public static boolean test008(int i1, int i2) {
    // Check that expression is replaced by getBoolean() || getBoolean() || i1 == i2
    if (!(!getBoolean() && !getBoolean() && !(i1 == i2))) {
        return true;
    } else {
      return false;
    }
  }

  public static boolean test009(int i1, int i2, int i3, int i4) {
    // Check that expression is replaced by !getBoolean() || i1 == i2 || i3 == i4
    if (!(getBoolean() && !(i1 == i2) && !(i3 == i4))) {
        return true;
    } else {
      return false;
    }
  }

  public static boolean test010(boolean b) {
    // Check that expression is not replaced
    boolean a;
    return !(a = b);
  }

  public static boolean test011(boolean b1, boolean b2) {
    // Check that expression !(b1 | b2) is not replaced
    return !(b1 | b2);
  }

  public static boolean test012(boolean b1, boolean b2) {
    // Check that expression !(b1 & b2) is not replaced
    return !(b1 & b2);
  }

  public static boolean test013(boolean b1, boolean b2) {
    // Check that expression !(b1 ^ b2) is not replaced
    return !(b1 ^ b2);
  }

  public static boolean test014(boolean b1, boolean b2) {
    // Check that expression is replaced by !b1 | b2
    return !(b1 & !b2);
  }

  public static boolean test015(boolean b1, boolean b2) {
    // Check that expression is replaced by b1 & !b2
    return !(!b1 | b2);
  }
}
