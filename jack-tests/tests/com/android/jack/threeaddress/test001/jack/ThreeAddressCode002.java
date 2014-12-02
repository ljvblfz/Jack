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

package com.android.jack.threeaddress.test001.jack;

public class ThreeAddressCode002 {

  public static int threadAddressCode002() {
    int a, b, c;
    a = b = c = 4;
    return a;
  }

  public static void threadAddressWithLabel(int a, int b, int c) {
    label:
      while (a < (b + c)) {
        continue label;
      }
  }

  public static int threadAddressWithoutBlock(int a, int b, int c) {
    int d = 0;
    if (a < b)
      c = a + b +d;
    return c;
  }
}
