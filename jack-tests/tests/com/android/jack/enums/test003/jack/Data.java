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

package com.android.jack.enums.test003.jack;

public class Data {

  public static String get1() {
    for (Values a : Values.values()) {
      switch (a) {
        case ONE:
          break;
        case TWO:
          break;
        default:
          return a.toString();
      }
    }

    return "None";
  }

  public static String get2() {
    for (Other a : Other.values()) {
      switch (a) {
        case ONE:
          break;
        case TWO:
          break;
        default:
          return a.toString();
      }
    }

    return "None";
  }

  public static String get3(Other a) {
    switch (a) {
      case ONE:
        return "ONE";
      case TWO:
        return "TWO";
      default:
        return a.toString();
    }
  }

  private static Other getEnumValue() {
    return Other.ONE;
  }
  public static String get4() {
    switch (getEnumValue()) {
      case ONE:
        return "ONE";
      case TWO:
        return "TWO";
      default:
        return "default";
    }
  }
}
