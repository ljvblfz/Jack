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

package com.android.jack.switchstatement.test021.jack;

import java.lang.Thread.State;

/**
 * Tests about optimized enum switches on public framework Enum.
 * Each enum is used only once indicating that it will NOT be
 * optimized by using synthetic class.
 * Please note that in this case framework Enum can only be public,
 * otherwise user class cannot access it.
 */
public class Switch1 {
  public static int switch1(State state) {
    switch (state) {
      case NEW:
        return 1;
      case RUNNABLE:
        return 2;
      case BLOCKED:
        return 3;
      case WAITING:
        return 4;
      case TIMED_WAITING:
        return 5;
      case TERMINATED:
        return 6;
      default:
        return 0;
    }
  }
}
