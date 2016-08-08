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

package com.android.jack.optimizations.valuepropagation.test104.jack;

interface I {
  void foo(String a, String b, String c, String d, String e);
}

public class A {
  void sink(String s) {
  }
  public void foo(String a, String b, String c, String d, String e) {
    // Propagated: a, b, c, d, e
    sink(a); sink(b); sink(c); sink(d); sink(e);
  }
  private void test() {
    foo("A", "B", "C", "D", "E");
  }
  private void test(I i) {
    i.foo("X", "B", "C", "D", "E");
  }

  private static void touch(Class clazz) { }
  public static void touch() {
    touch(I.class);
    touch(B.class);
    touch(C.class);
    touch(D.class);
    touch(E.class);
  }
}

class B extends A {
  public String foo(String p) {
    return p;
  }
  public void foo(String a, String b, String c, String d, String e) {
    // Propagated: b, c
    sink(a); sink(b); sink(c); sink(d); sink(e);
  }
  private void test() {
    foo("A", "B", "C", "D", "X");
  }
}

class C extends B implements I {
  public String foo(String p) {
    return p;
  }
  private void test() {
    foo("A", "B", "C", "X", "E");
  }
}

class D extends C {
  public String foo(String p) {
    return p;
  }
  public void foo(String a, String b, String c, String d, String e) {
    // Propagated: b
    sink(a); sink(b); sink(c); sink(d); sink(e);
  }
  private void test() {
    foo("A", "B", "X", "D", "E");
  }
}

class E extends D {
  public String foo(String p) {
    return p;
  }
  public void foo(String a, String b, String c, String d, String e) {
    // Propagated: none
    sink(a); sink(b); sink(c); sink(d); sink(e);
  }
  private void test() {
    foo("A", "X", "C", "D", "E");
  }
}
