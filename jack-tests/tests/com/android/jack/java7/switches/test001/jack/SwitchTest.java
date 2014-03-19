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

package com.android.jack.java7.switches.test001.jack;

/**
 * Switch with strings.
 */
public class SwitchTest {

  public static int switch001(String a) {
    switch (a) {
      case "a":
        return 1;
      case "b":
        return 2;
      default:
        return 3;
    }
  }

  public static int switch002(String a) {
    switch (a) {
      case "a":
      case "b":
        return 2;
      default:
        return 3;
    }
  }

  public static int switch003(String a) {
    switch (a) {
      case "a":
      case "b":
        return 2;
    }
    return 3;
  }

  public static int switch004(String a) {
    switch (a) {
      case "a" + "c":
        return 1;
      case "a" + "b":
        return 2;
    }
    return 3;
  }

  public static int switch005(String a) {
    switch (a) {
      case "Aa":
        return 1;
      case "BB":
        return 2;
      case "AaBB":
        return 3;
      case "BBAa":
        return 4;
    }
    return 5;
  }

  private static final String STR_C = "c";
  private static final String STR_B = "b";

  public static int switch006(String a) {
    switch (a) {
      case "a" + STR_C:
        return 1;
      case "a" + STR_B:
        return 2;
    }
    return 3;
  }

  public static int switch007(String a, String b) {
    switch (a) {
      case "a": {
        switch (b) {
          case "b":
            return 1;
          default:
            return 2;
        }
      }
      case "b":
        switch (b) {
          case "a":
            return 3;
          default:
            return 4;
        }
    }
    return 5;
  }
}
