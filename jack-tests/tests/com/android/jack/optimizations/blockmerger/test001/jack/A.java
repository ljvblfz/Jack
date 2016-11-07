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

package com.android.jack.optimizations.blockmerger.test001.jack;

public class A {
  private int testA(int s, int a, int b, int c, int d) {
    int result = 0;
    result += s;
    switch (s) {
      case 0: {
        int A = a, B = b, C = c, D = d;
        result += A * 10;
        result += B * 100;
        result += C * 1000;
        result += D * 10000;
        break;
      }
      case 1: {
        int A = a, B = b, C = c, D = d;
        result += B * 100;
        result += C * 1000;
        result += D * 10000;
        break;
      }
      case 2: {
        int A = a, B = b, C = c, D = d;
        result += C * 1000;
        result += D * 10000;
        break;
      }
      default: {
        int A = a, B = b, C = c, D = d;
        result += D * 10000;
        break;
      }
    }
    return result;
  }

  private int testB(int s, int a, int b, int c, int d) {
    int result = 0;
    result += s;
    if (s == 0) {
      int A = a * 2 - a;
      result += A * 10;
      int B = b * 2 - b;
      result += B * 100;
      int C = c * 2 - c;
      result += C * 1000;
      int D = d * 2 - d;
      result += D * 10000;
    } else if (s == 1) {
      int B = b * 2 - b;
      result += B * 100;
      int C = c * 2 - c;
      result += C * 1000;
      int D = d * 2 - d;
      result += D * 10000;
    } else if (s == 2) {
      int C = c * 2 - c;
      result += C * 1000;
      int D = d * 2 - d;
      result += D * 10000;
    } else {
      int D = d * 2 - d;
      result += D * 10000;
    }
    return result;
  }

  private int testC(int s, int a, int b, int c, int d) {
    int result = s;
    if (s == 0) {
      result += a;
      result += b;
      result += c;
      result += d;
    } else if (s == 1) {
      result += b;
      result += c;
      result += d;
    } else if (s == 2) {
      result += c;
      result += d;
    } else {
      result += d;
    }
    return result;
  }

  public String testA() {
    return "" +
        "|" + testA(0, 1, 2, 3, 4) +
        "|" + testA(1, 9, 2, 3, 4) +
        "|" + testA(2, 9, 9, 3, 4) +
        "|" + testA(3, 9, 9, 9, 4);
  }

  public String testB() {
    return "" +
        "|" + testB(0, 1, 2, 3, 4) +
        "|" + testB(1, 9, 2, 3, 4) +
        "|" + testB(2, 9, 9, 3, 4) +
        "|" + testB(3, 9, 9, 9, 4);
  }

  public String testC() {
    return "" +
        "|" + testC(0, 10, 200, 3000, 40000) +
        "|" + testC(1, 99, 200, 3000, 40000) +
        "|" + testC(2, 99, 999, 3000, 40000) +
        "|" + testC(3, 99, 999, 9999, 40000);
  }
}
