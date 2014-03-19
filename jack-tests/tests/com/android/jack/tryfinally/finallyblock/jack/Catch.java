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

public class Catch {

  public static void throwException() {
      throw new RuntimeException();
    }

  public static void doNotThrowException() {
    return;
  }

  public static int tryCatchFinally1() {
    int a = 1;
    try {
      a = a * 2;
      throwException();
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
    }
    return a;
  }

  public static int tryCatchFinally2() {
    int a = 1;
    try {
      a = a * 2;
      doNotThrowException();
    } catch (Exception e) {
      a = a * 3;
    } finally {
      a = a * 5;
    }
    return a;
  }

  public static int shouldNotCatch1() {
    int a = 1;
    try {
      try {
        a = a * 2;
        doNotThrowException();
        a = a * 3;
      } catch (RuntimeException e) {
        a = a * 5;
      } finally {
        a = a * 7;
        throwException();
        a = a * 11;
      }
    } catch (RuntimeException e2) {
      a = a * 13;
    }
    return a;
  }

  public static int shouldNotCatch2() {
    int a = 1;
    try {
      try {
        a = a * 2;
        doNotThrowException();
        a = a * 3;
      } catch (RuntimeException e) {
        try {
          a = a * 5;
          doNotThrowException();
          a = a * 17;
        } catch (RuntimeException e2) {
          a = a * 13;
        }
      } finally {
        a = a * 7;
        throwException();
        a = a * 11;
      }
    } catch (RuntimeException e2) {
      a = a * 23;
    }
    return a;
  }

  public static int finallyWithNew() {
    Integer i = null;
    try {
      i = new Integer(1);
    }
    finally {
      i = new Integer(2);
    }
    return (i.intValue());
  }

  public static int value = 0;

  @SuppressWarnings("finally")
  public static int shouldNotCatch3() {
    value = 0;
    try {
      try {
        try {
          return -1;
        } catch (NullPointerException e) {
          value += 5;
        }
      }
      finally {
        throw new NullPointerException();
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      value += 3;
    }
    return value;
  }
}
