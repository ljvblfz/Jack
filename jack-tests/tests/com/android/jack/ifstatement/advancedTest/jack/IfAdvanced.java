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

package com.android.jack.ifstatement.advancedTest.jack;

public class IfAdvanced {

  public static int testOr(boolean a, boolean b) {
    if (a || b) {
      return 1;
    } else {
      return 0;
    }
  }

  public static int testAnd(boolean a, boolean b) {
    if (a && b) {
      return 1;
    } else {
      return 0;
    }
  }

  public static int testMix(int a, int b) {
    if (a == 2 || b == 5) {
      return 1;
    } else {
      return 0;
    }
  }

  public static int testConditional(boolean a) {
    if (a ? true : false) {
      return 1;
    } else {
      return 0;
    }
  }

  public static int testElseIf(int a) {
    if (a == 0) {
      return 0;
    } else if (a == 1) {
      return 1;
    } else {
      return -1;
    }
  }

  @SuppressWarnings("unused")
  public static boolean testBraces() {
    if (7 == 9)
      return true;
    else
      return false;
  }

  public static int testNoReturnInBranch(int a) {
    int b;
    if (a > 0) {
      b = a;
    } else {
      b = -a;
    }
    return b;
  }

  private static final boolean DEBUG = false;
  private static final boolean NOT_DEBUG = true;
  private boolean value = true;

  public boolean getMethod() {
    return false;
  }

  @SuppressWarnings("unused")
  public int testIfFalse1() {
    if (DEBUG && value) {
      return 1;
    }
    return 2;
  }

  @SuppressWarnings("unused")
  public int testIfFalse3() {
    if (DEBUG && getMethod()) {
      return 1;
    }
    return 2;
  }

  @SuppressWarnings("unused")
  public int testIfFalse2() {
    if (DEBUG && value) {
      return 1;
    } else {
      return 2;
    }
  }

  @SuppressWarnings("unused")
  public int testIfTrue1() {
    if (true || value) {
      return 1;
    } else {
      return 2;
    }
  }

  @SuppressWarnings("unused")
  public int testIfTrue2() {
    if (NOT_DEBUG || value) {
      return 1;
    } else {
      return 2;
    }
  }

  @SuppressWarnings("unused")
  public int testIfTrue3() {
    if (NOT_DEBUG || value) {
      return 1;
    }
    return 2;
  }

  public int emptyIfThen(boolean val) {
    if (val);
    else {
      return 2;
    }
    return 1;
  }

  public int emptyIfElse(boolean val) {
    if (val) {
      return 2;
    } else ;
    return 1;
  }
}
