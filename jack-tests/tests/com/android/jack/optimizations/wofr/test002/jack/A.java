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

package com.android.jack.optimizations.wofr.test002.jack;

// Temp variables and null-check
public class A {
  public int SI = 123;
  public int fld;
  public static int sfld;

  A a() { return null; }
  int i() { return 0; }

  void test(A a, int i) {
    fld = i();
    fld = a.SI;
    fld = a().SI;
    fld = 1;
    fld = i;

    a.fld = 1;
    a().fld = 1;
    this.fld = 1;
    fld = 1;

    a.sfld = 101;
    a().sfld = 102;
    this.sfld = 103;
    A.sfld = 104;
    sfld = 105;
  }

  public static void touch() {
  }
}
