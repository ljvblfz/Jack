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

package com.android.jack.optimizations.ifwithconstantsimplifier.test002;

public class TestFuzz {

  static final int ITERATIONS_COUNT = 10;

  public int mI = 0;

  public void testThenBlockOptimization() {
    mI = 0;
    boolean lZ0 = true;
    for (int i = 0; i < ITERATIONS_COUNT; ++i) {
      if (false ? false : lZ0) {
        ++mI;
      } else {
        --mI;
      }
    }
  }

  public void testElseBlockOptimization() {
    mI = 0;
    boolean lZ0 = false;
    for (int i = 0; i < ITERATIONS_COUNT; ++i) {
      if (false ? false : lZ0) {
        ++mI;
      } else {
        --mI;
      }
    }
  }

  public void testThenBlockOptimization2() {
    mI = 0;
    for (int i = 0; i < ITERATIONS_COUNT; ++i) {
      boolean b;
      if (b = true) {
        ++mI;
      } else {
        --mI;
      }
    }
  }

  public void testElseBlockOptimization2() {
    mI = 0;
    for (int i = 0; i < ITERATIONS_COUNT; ++i) {
      boolean b;
      if (b = false) {
        ++mI;
      } else {
        --mI;
      }
    }
  }
}
