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

package com.android.jack.ifstatement.simpleTest.jack;

/**
 * Tests about if statement.
 */
public class If {
  @SuppressWarnings("unused")
  public static int testTrue() {
    if (true) {
      return 1;
    } else {
      return 2;
    }
  }

  public static int testTrue2() {
    if (true);
    return -1;
  }

  @SuppressWarnings("unused")
  public static int testFalse() {
    if (false) {
      return 1;
    } else {
      return 2;
    }
  }

  @SuppressWarnings("unused")
  public static int testFalse2() {
    if (false);
    return -1;
  }

  @SuppressWarnings("unused")
  public static int testFalseEmpty() {
    if (false) {
    }
    return 3;
  }

  public static int testParam(boolean b) {
    if (b) {
      return 3;
    } else {
      return 4;
    }
  }

  public static int testLt(int a, int b) {
    if (a < b) {
      return 1;
    } else {
      return 0;
    }
  }

  public static int testGt(int a, int b) {
    if (a > b) {
      return 1;
    } else {
      return 0;
    }
  }

  public static int testLte(int a, int b) {
    if (a <= b) {
      return 1;
    } else {
      return 0;
    }
  }

  public static int testGte(int a, int b) {
    if (a >= b) {
      return 1;
    } else {
      return 0;
    }
  }

  public static int testEq(int a, int b) {
    if (a == b) {
      return 1;
    } else {
      return 0;
    }
  }

  public static int testNeq(int a, int b) {
    if (a != b) {
      return 1;
    } else {
      return 0;
    }
  }

  public static boolean testCst(int a) {
    if (a == 5) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean testNested() {
    if (true) {
      if (false) {
        return false;
      }
      else {
        return true;
      }
    }
    return false;
  }
}
