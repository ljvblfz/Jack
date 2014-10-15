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

package com.android.jack.order.test001.jack;

/**
 * Evaluation order test.
 */
public class Order {

  public static int sum(int a, int b) {
    return a + b;
  }

  public static int sum(int a, int b, int c) {
    return a + b + c;
  }

  public static int getValue1(int a, int b) {
    return sum(a, a = b);
  }

  public static int getValue2(int a, int b) {
    if (a != (a = b)) {
      return 1;
    }
    return 2;
  }

  public static int getValue3() {
    int a = 5;
    int b = 6;
    return sum(a, a = b);
  }

  public static int getValue4() {
    int a = 5;
    int b = 6;
    if (a != (a = b)) {
      return 1;
    }
    return 2;
  }

  public static int getValue5(int a) {
    int result = 0;
    for (int i = 0; i < (i = a); i++) {
      result++;
    }
    return result;
  }

  public static int getValue6() {
    int a = 5;
    int b = 6;
    int c = 7;
    return sum(a, a = b, b = c);
  }
}
