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

package com.android.jack.optimizations.lambdas.test001.jack;

interface I0 {
  void foo();
}

interface I1 {
  int bar(int i);
}

interface IA {
  Object foo();
}

interface IB extends IA {
  Integer foo();
}

interface IA2 {
  Object foo();
}

interface TestB {
  String foo(String i);
}

public class A {
  static int i0Return;

  int sa(IA i) {
    return ((Integer) i.foo()).intValue();
  }

  int sa2(IA2 i) {
    return ((Integer) i.foo()).intValue();
  }

  int sb(IB i) {
    return i.foo().intValue();
  }

  int sc(I1 i) {
    return i.bar(0);
  }

  int sd(I0 i) {
    i.foo();
    return i0Return;
  }

  public int testA() {
    // Same empty capture signature, different methods
    return sa(() -> 1) +
        sa(() -> 20) +
        sa(() -> 300) +
        sb(() -> 4000) +
        sc(i -> i + 50000) +
        sd(() -> { i0Return = 600000; }) +
        sd(() -> { i0Return = 7000000; }) +
        sd(() -> { i0Return = 80000000; });
  }

  String sB(TestB inter, String p) {
    return inter.foo(p);
  }

  private String fld = "{fld}";

  public String testB(String p) {
    boolean z = true;
    byte b = 2;
    short s = 3;
    int i = 4;
    long l = 5;
    float f = 6;
    double d = 7;
    char c = '8';
    // Same signature with different captures
    return
        sB(x -> x + p, "#1:") +
        sB(x -> x + fld, "#2:") +
        sB(x -> x + p + fld, "#3:") +
        sB(x -> x + fld + p, "#4:") +
        sB(x -> x + i + fld, "#5:") +
        sB(x -> x + fld + i, "#6:") +
        sB(x -> x + z + b + s + i + l + f + d + c + fld, "#7:") +
        sB(x -> x + b + i + l + fld + z + f + d + s + c, "#8:") +
        sB(x -> x + fld + c + d + f + l + i + s + b + z, "#9:");
  }
}