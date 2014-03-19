/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.tryfinally.finally005.jack;

/**
 * JUnit test allowing to verify that useless 'if' are not generated (if null != null).
 */
public class Finally005 {

  private static class A {
    public int i = 10;

    public void call() {
    }
  }

  public static A newA() {
    return new A();
  }

  public static void get() {
    A a = null;
    try {
      a = newA();
    } finally {
      // After FinallyRemover and ConstantRefinerAndVariableRemover, the following if is no longer
      // required and should be removed since it is similar to if (null != null).
      if (a != null) {
        int l = a.i;
        a.i = 10;
        a.call();
      }
    }
  }
}
