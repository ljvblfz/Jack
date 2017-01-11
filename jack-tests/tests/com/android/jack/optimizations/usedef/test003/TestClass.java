/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.optimizations.usedef.test003;

public class TestClass {
  public static long callAndReturnConstLong() {
    emptyMethod();
    // Do NOT put '0L' below, otherwise we do not test the pattern exposing the bug that this
    // regression test has been written for.
    // Indeed, using int '0' forces Jack to generate an explicit cast (int to long) in the IR.
    // The result of this cast will be stored in an intermediate synthetic variable when moving to
    // three-address-code form. This variable will eventually be optimized away by our use/def
    // optimizations.
    return 0;
  }

  private static void emptyMethod() {
  }
}
