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

package com.android.jack.optimizations.valuepropagation.test002;

class A {
  static int static_not_initialized;

  int instance;

  A(int i) {
    // The field must become a constant although there is no static
    // constructor and it is not initialized, thus replaced with 0
    instance = static_not_initialized;
  }

  static boolean test() {
    return true;
  }
}

class B {
  static int static_initialized = 1;
  static int static_not_initialized;

  static {
    // Definitely initialize 'static_not_initialized' and
    // Partially re-initialize 'static_initialized' with the same value
    if (A.test()) {
      static_initialized = 1;
      static_not_initialized = 33;
    } else {
      static_not_initialized = 33;
    }
  }

  int instance;
  B anotherB;

  B(int i) {
    // Both static fields must be replaced with constants
    i = static_initialized + static_not_initialized;

    B other = this;
    // Reference 'other.instance' is detected to be a simple alias to this and
    // it is not raplaced with 1234, but 'anotherB.instance' is
    i = this.instance + other.instance + instance + anotherB.instance + i;
    instance = 1234;
  }
}

class C {
  int instance;

  C() {
    // No definition reach the exit block
    while (true) {
      instance = 123;
    }
  }

  static int foo() {
    return new C().instance;
  }
}

class D {
  volatile int instance;

  D() {
    instance = 123;
  }

  int foo() {
    // Don't propagate values of volatile fields
    return instance;
  }
}

