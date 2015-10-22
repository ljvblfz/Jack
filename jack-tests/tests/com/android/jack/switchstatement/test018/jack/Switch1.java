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

package com.android.jack.switchstatement.test018.jack;

/**
 * Tests about optimized enum switches on package visible Enum.
 * Each enum is used more than once indicating that it will be
 * optimized by using synthetic class.
 */
public class Switch1 {
  @SuppressWarnings("incomplete-switch")
  public static int switch1(Object o1, Object o2) {
    Enum1 enum1 = (Enum1) o1;
    Enum2 enum2 = (Enum2) o2;
    switch (enum1) {
      case VALUE1:
        return 1;
      case VALUE3:
        return 3;
      case VALUE5:
        return 5;
    }
    switch (enum2) {
      case VALUE2:
        return 2;
      case VALUE4:
        return 4;
    }
    return 0;
  }
}
