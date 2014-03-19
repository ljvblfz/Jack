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

package com.android.jack.tryfinally.finallyblock.jack;

@SuppressWarnings("finally")
public class Branching {

  public static void throwException() {
    throw new RuntimeException();
  }

  public static void doNotThrowException() {
    return;
  }

  public static int returnInFinally1() {
    int a = 1;
    try {
      a = a * 2;
      throwException();
      a = a * 3;
    } finally {
      a = a * 5;
      return a;
    }
  }

  public static int returnInFinally2() {
    int a = 1;
    try {
      a = a * 2;
      throwException();
      a = a * 3;
      return a;
    } finally {
      a = a * 5;
      return a;
    }
  }

  public static int returnInFinally3() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 3;
      return a;
    } finally {
      a = a * 5;
      return a;
    }
  }

  public static int returnInTry1() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
      a = a * 3;
      return a;
    } finally {
      a = a * 5;
    }
  }

  public static int returnInCatch1() {
    int a = 1;
    try {
      a = a * 2;
      throwException();
      a = a * 3;
      return a;
    } catch (Exception e) {
      a = a * 7;
      return a;
    } finally {
      a = a * 5;
    }
  }

  public static String returnInTry2() {
    String a = "A";
    try {
      doNotThrowException();
      return a;
    } finally {
      a = "B";
    }
  }

  public static B returnInTry3(B b) {
    b.field = 1;
    try {
      b.field = b.field * 2;
      return b;
    } finally {
      b.field = b.field * 3;
    }
  }

  public static int breakInFinally1() {
    int a = 1;
    while (true) {
      try {
        a = a * 2;
        return a;
      } finally {
        a = a * 3;
        break;
      }
    }
    return -1;
  }

  public static int continueInFinally1() {
    int a = 5;
    int i = 0;
    while (i<4) {
      try {
        a = a * 2;
        i++;
        return a;
      } finally {
        a = a * 3;
        continue;
      }
    }
    return i;
  }

  public static int loopIntoTryFinally() {
    int i = 0;
    try {
      while (i<5) {
        i++;
      }
    }
    finally {
      i++;
    }
     return i;
  }

  public static int returnInNestedTry(boolean b1) {
    int a = 1;
    try {
      try {
          if (b1) {
            return 27;
          }
      } finally {
        a = a * 3;
      }
    } finally {
      a = a * 11;
    }
    return a;
  }
}
