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

package com.android.jack.switchstatement.test024.jack;

/**
 * Tests about optimized enum switches on protected Enum. Each enum
 * is used only once indicating that it will be NOT optimized by
 * using synthetic class.
 */
public class Switch1 {
  public static int switch1(Object o1) {
    Enum1.Enum1_ enum1 = (Enum1.Enum1_) o1;
    switch (enum1) {
      case VALUE1:
        return 1;
      case VALUE3:
        return 3;
      case VALUE5:
        return 5;
      default:
        return 0;
    }
  }
}
