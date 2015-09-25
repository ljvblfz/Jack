/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.java8.defaultmethod.test014.jack;

interface I1 {
  ClassInitializer1 ci1 = new ClassInitializer1();

  default void method1() {
      System.out.println("default method 1");
  }
}

class ClassInitializer1 {
  static {
    if (DefaultMethod.result == null) {
      DefaultMethod.result = "static initializer 1:";
  } else {
    DefaultMethod.result += "static initializer 1";
  }
  }
}

interface I2 {
  ClassInitializer2 ci = new ClassInitializer2();

  default void method2() {
      System.out.println("default method 2");
  }
}

class ClassInitializer2 {
  static {
    if (DefaultMethod.result == null) {
      DefaultMethod.result = "static initializer 2:";
  } else {
    DefaultMethod.result += "static initializer 2";
  }
  }
}

/**
 * Check that interface are initialized into the right order
 */
public class DefaultMethod implements I1, I2 {

  public static String result;

  public String test() {
    return result;
  }
}
