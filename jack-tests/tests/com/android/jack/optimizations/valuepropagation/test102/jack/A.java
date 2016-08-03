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

package com.android.jack.optimizations.valuepropagation.test102;

interface I {
  void foo(String a, String b, String c, String d, String e, String f);
  void bar(String a, String b, String c, String d, String e, String f);
}

class A {
  void s(String s) { }
  public void foo(String a, String b, String c, String d, String e, String f) {
    // a + b + c + d + e + f
    s(a); s(b); s(c); s(d); s(e); s(f);
  }
  public void bar(String a, String b, String c, String d, String e, String f) {
    // a + b + c + d + e + f
    s(a); s(b); s(c); s(d); s(e); s(f);
  }
  private void test() {
    foo("A", "B", "C", "D", "E", "F");
    bar("A", "B", "C", "D", "E", "F");
  }
  static void test(I i) {
    i.foo(null, "B", "C", "D", "E", "F");
    i.bar(null, "B", "C", "D", "E", "F");
  }
}

class B extends A {
  public void foo(String a, String b, String c, String d, String e, String f) {
    // a + c + d + e + f
    s(a); s(b); s(c); s(d); s(e); s(f);
  }
  public void bar(String a, String b, String c, String d, String e, String f) {
    // d + e + f
    s(a); s(b); s(c); s(d); s(e); s(f);
  }
  private void test() {
    foo("A", null, "C", "D", "E", "F");
    bar("A", null, "C", "D", "E", "F");
  }
}

class C extends B implements I {
  public void foo(String a, String b, String c, String d, String e, String f) {
    // d + e + f
    s(a); s(b); s(c); s(d); s(e); s(f);
  }
  private void test() {
    foo("A", "B", null, "D", "E", "F");
    bar("A", "B", null, "D", "E", "F");
  }
}

class D extends C {
  public void foo(String a, String b, String c, String d, String e, String f) {
    // e + f
    s(a); s(b); s(c); s(d); s(e); s(f);
  }
  public void bar(String a, String b, String c, String d, String e, String f) {
    // f
    s(a); s(b); s(c); s(d); s(e); s(f);
  }
  private void test() {
    foo("A", "B", "C", null, "E", "F");
    bar("A", "B", "C", null, "E", "F");
  }
}

class CC extends B {
  public void foo(String a, String b, String c, String d, String e, String f) {
    // a + d + e + f
    s(a); s(b); s(c); s(d); s(e); s(f);
  }
  public void bar(String a, String b, String c, String d, String e, String f) {
    // a + e + f
    s(a); s(b); s(c); s(d); s(e); s(f);
  }
  private void test() {
    foo("A", "B", null, "D", "E", "F");
    bar("A", "B", null, "D", "E", "F");
  }
}

class DD extends CC {
  public void foo(String a, String b, String c, String d, String e, String f) {
    // a + e + f
    s(a); s(b); s(c); s(d); s(e); s(f);
  }
  private void test() {
    foo("A", "B", "C", null, "E", "F");
    bar("A", "B", "C", null, "E", "F");
  }
}

class E extends D {
  public void foo(String a, String b, String c, String d, String e, String f) {
    // f
    s(a); s(b); s(c); s(d); s(e); s(f);
  }
  private void test() {
    foo("A", "B", "C", "D", null, "F");
    bar("A", "B", "C", "D", null, "F");
  }
}
