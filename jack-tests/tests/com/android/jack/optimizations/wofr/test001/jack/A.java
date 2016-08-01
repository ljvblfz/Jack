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

package com.android.jack.optimizations.wofr.test001;

// General test
class A {
  static int _0_read_0_writes;
  static int _1_read_0_writes;
  static int _1_read_1_writes;
  static int _0_read_1_writes;
  static int _1_read_1_writes_init = -1;
  static int _0_read_1_writes_init = -2; // assignment removed in <clinit>

  static volatile int _0_read_1_writes_vol; // assignment NOT removed

  void writes() {
    A._0_read_1_writes = 123; // assignment removed
    A._1_read_1_writes = 321;
    A._0_read_1_writes_vol = 111;
  }

  int reads() {
    return _1_read_0_writes + _1_read_1_writes + _1_read_1_writes_init;
  }

  void btest(B b) {
    B.fld = 123;
    b.fld = 123;
  }
}

// Static field not removed outside the
// type if preserve JLS is set
class B {
  static int fld;

  void btest(B b) {
    B.fld = 123;
    b.fld = 123;
  }
}

// Object lifetime
class C {
  int iF0;
  String sF1;
  String sF2;
  String sF2a;
  Object sF3;
  Object sF4;

  void test(String s, int i) {
    iF0 = 0;
    iF0 = 123;
    iF0 = i;

    sF1 = null;
    sF2 = null;
    sF2 = "Blah";
    sF2a = null;
    sF2a = "Blah";
    sF2a = s;

    sF3 = null;
    sF4 = null;
    sF4 = new Object();
  }
}

