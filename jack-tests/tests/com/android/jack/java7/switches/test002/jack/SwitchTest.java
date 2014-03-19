/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.java7.switches.test002.jack;

/**
 * Switch with strings.
 */
public class SwitchTest {

  public static int count = 0;

  private static String getValue(String a) {
    count++;
    return a;
  }

  public static int switch001(String a) {
    switch (getValue(a)) {
      case "a":
        return 1;
      case "b":
        return 2;
      default:
        return 3;
    }
  }
}
