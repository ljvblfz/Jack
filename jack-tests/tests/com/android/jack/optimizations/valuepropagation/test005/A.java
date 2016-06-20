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

package com.android.jack.optimizations.valuepropagation.test005;

class A {
  int f0_reassigned_in_constructor_ok = 7;
  int f1_reassigned_in_constructor_wrong = 7;
  int f2_reassigned_in_method_ok = 7;
  int f3_reassigned_in_method_wrong = 7;

  A() {
    f0_reassigned_in_constructor_ok = 77;
    f1_reassigned_in_constructor_wrong = 77;
    f2_reassigned_in_method_ok = 77;
    f3_reassigned_in_method_wrong = 77;
    new A().f0_reassigned_in_constructor_ok = 777;
    new A().f1_reassigned_in_constructor_wrong = 77;
    f0_reassigned_in_constructor_ok = 777;
    f1_reassigned_in_constructor_wrong = 777;
    f2_reassigned_in_method_ok = 777;
    f3_reassigned_in_method_wrong = 777;
  }

  A get() {
    f2_reassigned_in_method_ok = 777;
    f3_reassigned_in_method_wrong = 77;
    return null;
  }

  int check() {
    return 1 +
        f0_reassigned_in_constructor_ok +
        f1_reassigned_in_constructor_wrong +
        f2_reassigned_in_method_ok +
        f3_reassigned_in_method_wrong;
  }
}

class B {
  int definitely_assigned_through_delegation;
  B() {
    definitely_assigned_through_delegation = 123;
  }
  B(int i) {
    this();
    if (i > 0) {
      if (i > 100) {
        definitely_assigned_through_delegation = i;
      }
      definitely_assigned_through_delegation = 123;
    }
  }

  int check() {
    return definitely_assigned_through_delegation;
  }
}

class C {
  static int not_initialized_1;
  static int not_initialized_2;
  C() {
    not_initialized_1 = 123;
    not_initialized_2 = 0;
  }
  int check() {
    return not_initialized_1 + not_initialized_2;
  }
}
