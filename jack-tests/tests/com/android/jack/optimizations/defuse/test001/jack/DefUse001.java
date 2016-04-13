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

package com.android.jack.optimizations.defuse.test001.jack;

class C {
}

class A {
  static A mA(A a) {
    return null;
  }
}

class D {
  private String s;
  D mD(String s, C c) {
    this.s = s;
    return this;
  }
  long size() {
    return s.length();
  }
}

public class DefUse001 {

  static C staticField = null;

  public static B create(A a, long l, D b) {
    return new B(l);
  }

  public static B create(A a, String s) {
    C c = staticField;
    if (a != null) {
       a = A.mA(a);
    }
    D d = new D().mD(s, c);
    return create(a, d.size(), d);
  }

  public static int test002(boolean a, boolean b, boolean c) {
    int x = a ? 1 : 2;
    int y = b ? 3 : 4;
    int z = c ? x : y;
    return z;
  }
}
