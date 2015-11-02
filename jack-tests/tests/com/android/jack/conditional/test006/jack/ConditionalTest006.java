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

package com.android.jack.conditional.test006.jack;

public class ConditionalTest006 {

  // Check that null is assigned to f1
  public static Float test001() {
    @SuppressWarnings({"boxing", "unused"})
    Float f1 = false? 1.0f: null;
    return f1;
  }

  // Check that null pointer exception is trigger due to unboxing of null
  public static Float test002() {
    @SuppressWarnings({"boxing", "unused"})
    Float f2 = false? 1.0f: false? 1.0f: null;
    return f2;
  }

  // Check that null pointer exception is trigger due to unboxing of null
  public static Boolean test003() {
    @SuppressWarnings({"boxing", "unused"})
    Boolean b = false? true: false? true: null;
    return b;
  }

  // Check that null pointer exception is trigger due to unboxing of null
  public static Byte test004() {
    @SuppressWarnings({"boxing", "unused"})
    Byte b = false? (byte) 1: false? (byte) 2: null;
    return b;
  }

  // Check that null pointer exception is trigger due to unboxing of null
  public static Character test005() {
    @SuppressWarnings({"boxing", "unused"})
    Character c = false? (char) 1: false? (char) 2: null;
    return c;
  }

  // Check that null pointer exception is trigger due to unboxing of null
  public static Short test006() {
    @SuppressWarnings({"boxing", "unused"})
    Short s = false? (short) 1: false? (short) 2: null;
    return s;
  }

  // Check that null pointer exception is trigger due to unboxing of null
  public static Integer test007() {
    @SuppressWarnings({"boxing", "unused"})
    Integer i = false? 1: false? 2: null;
    return i;
  }

  // Check that null pointer exception is trigger due to unboxing of null
  public static Long test008() {
    @SuppressWarnings({"boxing", "unused"})
    Long l = false? 1l: false? 2l: null;
    return l;
  }

  // Check that null pointer exception is trigger due to unboxing of null
  public static Double test009() {
    @SuppressWarnings({"boxing", "unused"})
    Double d = false? 1.2: false? 2.4: null;
    return d;
  }
}
