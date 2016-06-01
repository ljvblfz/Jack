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

package com.android.jack.optimizations.inlinemethod.test001.jack;

import com.android.jack.annotations.ForceInline;

public class TestCase {
  // ========================= Related to inlineMe01 =========================
  public int calledInlineMe01 = 0;

  @ForceInline
  public void inlineMe01() {
    calledInlineMe01++;
  }

  public void callInlineMe01Once() {
    inlineMe01();
  }

  public void callInlineMe01Twice() {
    inlineMe01();
    inlineMe01();
  }

  public void callInlineMe01Loop100x() {
    for (int i = 0; i < 100; i++) {
      inlineMe01();
    }
  }

  // ========================= Related to inlineMe02 =========================
  public int calledInlineMe02 = 0;

  @ForceInline
  public int inlineMe02(int x) {
    calledInlineMe02++;
    return x + x;
  }

  public void callInlineMe02IgnoreReturn() {
    inlineMe02(99);
  }

  public int callInlineMe02UseReturn() {
    return inlineMe02(99) + 2;
  }

  public int callBothInlineMe01andInlineMe02() {
    inlineMe01();
    return inlineMe02(99) + 2;
  }

  public int callInlineMe02Nested(int i) {
    return inlineMe02(inlineMe02(inlineMe02(inlineMe02(i))));
  }

  // ========================= Related to inlineMe03 =========================
  public int calledInlineMe03 = 0;

  @ForceInline
  public int inlineMe03(int x, int y, int z) {
    calledInlineMe03++;
    if (x == 1) {
      return y + z;
    } else if (x == 2) {
      return y - z;
    }
    if (x == 3) {
      switch (y) {
        case 1:
          return inlineMe02(z * z);
        case 2:
        case 3:
          return inlineMe02(inlineMe02(z * z * z));
        case 4:
          return inlineMe02(z * 3 * inlineMe02(z));
      }
    }
    return -1;
  }

  public int callInlineMe03(int x, int y, int z) {
    return inlineMe03(x, y, z);
  }

  // ========================= Related to inlineMe04 =========================
  public static String inlineMe04State = "Not called";

  /**
   * Testing static functions with void return type.
   */
  @ForceInline
  public static void inlineMe04(boolean x) {
    if (x) {
      inlineMe04State = "Called, x == true";
      return;
    }
    inlineMe04State = "Called, x != true";
  }

  public static String callInlineMe04(boolean x) {
    inlineMe04State = "Not called";
    inlineMe04(x);
    return inlineMe04State;
  }
}
