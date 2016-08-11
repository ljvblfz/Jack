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

package com.android.jack.optimizations.lambdas.test004.jack;

interface MarkerA {
}

interface MarkerB {
}

interface I0 {
  String foo();
}

interface I1 {
  String foo();
}

public class A {
  String s0(I0 i) {
    return i.foo();
  }

  String s1(I1 i) {
    return i.foo();
  }

  public String testMarkers() {
    return s0(() -> "{i0}")
        + s0((I0 & MarkerA) (() -> "{i0&a}"))
        + s0((I0 & MarkerB) (() -> "{i0&b}"))
        + s0((I0 & MarkerA & MarkerB) (() -> "{i0&a&b}"))
        //+ s0((I0 & I1 & MarkerA & MarkerB) (() -> "{i0&i1&a&b}")) | Known JACK issue
        //+ s0((I0 & I1) (() -> "{i0&i1#01}"))                      | Known JACK issue
        //+ s1((I0 & I1) (() -> "{i0&i1#02}"))                      | Known JACK issue
        + s1((I1 & MarkerA & MarkerB) (() -> "{i1&a&b}"));
  }
}
