/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.string.concat001.jack;

public class Data {

  public static String test001(String a, String b) {
    return a + b;
  }

  public static String test002(String a, Object b) {
    return a + b;
  }

  public static String test003(String a) {
    return a + "literal1";
  }

  public static String test004(Object a) {
    return a + "literal2";
  }

  public static String test005(String a, Object b) {
    return a + b + "literal3";
  }

  public static String test006(Object a, Object b) {
    return "literal4" + a + b;
  }

  public static String test007(Object a, Object b) {
    return a + "literal5" + b;
  }

  public static String test008(String a, String b) {
    return a += b;
  }

  public static String test009(String a, Object b) {
    return a + (b + "literal6");
  }

  public static String test010(String a, Object b) {
    return (a + b) + "literal7";
  }

  public static String test011(String a, Object b) {
    return a += b + "literal8";
  }

  public static String test012(String a, String b, CharSequence c) {
    return a += b + c;
  }
}
