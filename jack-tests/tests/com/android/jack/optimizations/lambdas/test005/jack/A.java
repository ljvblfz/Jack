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

package com.android.jack.optimizations.lambdas.test005.jack;

interface I0 {
  String foo();
}

interface I1 {
  String bar();
}

public class A {
  String s0(I0 i) {
    return i.foo();
  }

  String s1(I1 i) {
    return i.bar();
  }

  public static String m1() {
    return "{m1}";
  }

  private static String m2() {
    return "{m1}";
  }

  public String m3() {
    return "{m3}";
  }

  public String m4() {
    return "{m4}";
  }

  private String m5() {
    return "{m5}";
  }

  public String test(A a) {
    return s0(A::m1)
        + s1(A::m1)
        + s0(A::m2)
        + s0(this::m3)
        + s1(this::m3)
        + s0(a::m3)
        + s0(this::m4)
        + s0(this::m4)
        + s0(this::m5)
        + s1(this::m5);
  }
}
