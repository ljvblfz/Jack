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

package com.android.jack.optimizations.valuepropagation.test101;

interface I {
  int interface_(int a, int b, int c);
}

class A implements I {
  int virtual(int a, int b) {
    return a + b; // 'a' to be replaced with 100
  }
  private int private_(int a, int b) {
    return a + b; // 'a' to be replaced with 2
  }
  static int static_(int a, int b) {
    return a + b; // 'a' to be replaced with 300
  }
  public int interface_(int a, int b, int c) {
    return a + b + c; // 'b' to be replaced with 200
  }
  void check01(A a, I i) {
    a.virtual(100, 2);
    a.private_(2, 3);
    a.static_(300, 3);
    a.interface_(1, 200, 3);
    i.interface_(10, 200, 3);
  }
  void check02(A a, I i) {
    a.virtual(10 + 90, 20);
    a.private_(1+1, 30);
    a.static_(100+200, 30);
    a.interface_(1, 200, 30);
    i.interface_(10, 200, 30);
  }
}

class B implements I {
  public int interface_(int a, int b, int c) {
    return a + b + c; // 'a' to be replaced with 10 and 'b' with 200
  }
}

class C {
  static int foo(int a, int b, int c, int d) {
    return a + b + c + d; // only 'a' to be replaced
  }
}

class D extends C {
  static void call(C c, D d) {
    C.foo(100, 200, 300, 400);
    D.foo(100, 201, 300, 400);
    c.foo(100, 200, 301, 400);
    d.foo(100, 200, 300, 401);
  }
}
