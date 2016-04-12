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

package com.android.jack.verify.test002.jack;

public class CombinedCondition {
  public static int get(boolean isA, int b, int c) {

    int z = 0;
    if (isA) {
      if ((z = b) == c || (z != 0 && b == c)) {
        return 1;
      }
    } else {
      z = 5;
    }
    return z;
  }
  public static int get2(boolean isA, int b, int c) {

    for (int i = 0; i < 5; i++) {
      c += b;
      if (c == b) {
        break;
      }
    }
    return isA && b < c ? b : c;
  }
}