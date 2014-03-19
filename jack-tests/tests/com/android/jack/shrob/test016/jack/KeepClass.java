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

package com.android.jack.shrob.test016.jack;

public class KeepClass {
  public static String value() {
    return "com.android.jack.shrob.test016.jack.A";
  }

  public static String value2() {
    return "Lcom.android.jack.shrob.test016.jack.A;";
  }

  public static String value3() {
    return "com/android/jack/shrob/test016/jack/A";
  }

  public static String value4() {
    return "Lcom/android/jack/shrob/test016/jack/A;";
  }

  public static String value5() {
    return "A";
  }

  public static String value6() {
    return "[com.android.jack.shrob.test016.jack.A";
  }

  public static String value7() {
    return "com.android.jack.shrob.test016.jack.A[]";
  }

  public static String value8() {
    return "dfgdgcom.android.jack.shrob.test016.jack.A";
  }

  public static String value9() {
    return "com.android.jack.shrob.test016.jack.Afgdg";
  }
}
