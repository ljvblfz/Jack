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

package com.android.jack.optimizations.lambdas.test003.jack;

interface I0<T> {
  T foo();
}

interface I1<T> {
  Object foo();
}

interface I2 {
  <T> T foo();
}

interface I3<T> extends I2 {
  T foo();
}

public class A {
  <T> T s0(I0<T> i) {
    return i.foo();
  }

  String s1(I1<String> i) {
    return (String) i.foo();
  }

  <T> T s2(I3<T> i) {
    return i.foo();
  }

  public String testGenerics() throws Throwable {
    return s0(() -> "{i0}") + s1(() -> "{i1}") + s2(() -> "{i3}");
  }
}
