/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.optimizations.blockmerger.test002.jack;

public class A {
  private int testA(int a) {
    if (a > 0) {
      a--;
    } else if (a < 0) {
      a++;
    }
    if (a > 0) {
      return a;
    } else if (a < 0) {
      return a;
    } else {
      return a;
    }
  }

  private int testB(int a, int b, int c) {
    if (a > 0) {
      a--;
    } else if (a < 0) {
      a++;
    }
    if (a > 0) {
      return a + b * c * 10;
    } else if (a < 0) {
      return a + b * c * 10;
    } else {
      return a + b * c * 10;
    }
  }

  private int testC(int a) {
    if (a > 0) {
      String x = "a > 0";
      return a;
    } else if (a < 0) {
      String x = "a < 0";
      return a;
    } else {
      String x = "a == 0";
      return a;
    }
  }

  public String testA() {
    return testA(2) + "|" + testA(1) + "|" +
        testA(0) + "|" + testA(-1) + "|" + testA(-2);
  }

  public String testB() {
    return testB(2, 1, 1) + "|" + testB(1, 1, 2) + "|" +
        testB(0, 1, 3) + "|" + testB(-1, -1, 4) + "|" + testB(-2, -1, 5);
  }

  public String testC() {
    return testC(1) + "|" + testC(0) + "|" + testC(-1);
  }
}
