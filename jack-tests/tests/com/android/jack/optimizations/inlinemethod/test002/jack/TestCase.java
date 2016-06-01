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

package com.android.jack.optimizations.inlinemethod.test002.jack;

import com.android.jack.annotations.ForceInline;

public class TestCase {
  public int calledInlineMe04 = 0;
  public int calledInlineMe01CatchNpe = 0;

  @SuppressWarnings("null")
  @ForceInline
  public int inlineMe01(int x) {
    calledInlineMe04++;
    if (x == -1) {
      throw new RuntimeException("Calling inlineMe01 with x = -1");
    } else if (x == -2) {
      Object o = null;
      return o.toString().length();
    }
    return x * x;
  }

  public int callInlineMe01NoCatch(int x) {
    return inlineMe01(x);
  }

  public int callInlineMe01WithCatchNpe(int x) {
    try {
      return inlineMe01(x);
    } catch (NullPointerException npe) {
      calledInlineMe01CatchNpe++;
    }
    return -1;
  }
}
