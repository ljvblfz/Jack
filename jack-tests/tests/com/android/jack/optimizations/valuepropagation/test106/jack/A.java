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

package com.android.jack.optimizations.valuepropagation.test106.jack;

public class A {
  void s(String s) {
  }
  A(String x) {
    s(x); // Propagate: "AAA"
  }
  A(String x, String y) {
    s(x); s(y); // Propagate: "BBB"
  }
  private void foo(String s) {
    s(s); // Propagate: "XXX"
  }
  private void test() {
    new A("AAA");
    new A("BBB", "CCC");
    foo("XXX");
  }

  private static void touch(Class clazz) { }
  public static void touch() {
    touch(B.class);
    touch(C.class);
    touch(D.class);
  }
}

class B extends A {
  B() {
    super("BBB", "ccc");
  }
  private void foo(String s) {
    s(s); // Propagate: "YYY"
  }
  private void test() {
    foo("YYY");
  }
}

class C extends B {
  void foo(String s) {
    s(s); // Propagate: "ZZZ"
  }
  private void test() {
    foo("ZZZ");
  }
}

class D extends C {
  void foo(String s) {
    s(s);
  }
  private void test() {
    foo("zzz"); // Propagate: none
  }
}
