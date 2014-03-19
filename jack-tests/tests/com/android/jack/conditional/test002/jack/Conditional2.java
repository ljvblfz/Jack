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

package com.android.jack.conditional.test002.jack;

public class Conditional2 {
  public static int test1(boolean cond) {
    return m(cond ? new C1() : new C2());
  }

  private static int m(I1 i1) {
    if (i1 instanceof C2) {
      return 2;
    } else if (i1 instanceof C1) {
      return 1;
    } else {
      return 0;
    }
  }
  public static int test2(boolean cond) {
    return m2(cond ? new C1[2] : new C[0]);
  }

  private static int m2(C[] cs) {
    return cs.length;
  }
}
