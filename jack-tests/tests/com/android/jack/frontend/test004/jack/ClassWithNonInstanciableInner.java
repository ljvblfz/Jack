/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.frontend.test004.jack;

public class ClassWithNonInstanciableInner {

  private static final boolean FALSE = false;

  public static void test1() {
    if (FALSE) {
      new Runnable() {

        @Override
        public void run() {
          int i = 1;
        }
      };
    }
  }

  public static void test2() {
    class Runner implements Runnable {

        @Override
        public void run() {
        }
      }
    return;
  }

  public static void test3() {
    abstract class Runner implements Runnable {

        @Override
        public void run() {
        }
      }
    return;
  }

}
