/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.ssa.test001.jack;

public class Ssa {

  private static void sometimesThrow(boolean shouldThrow) {
    if (shouldThrow) {
      throw new RuntimeException("message");
    }
  }

  /**
   * A doublely nested catch case.
   */
  public static int doubleNestedCatch() {
    try {
      sometimesThrow(true);
      return -1;
    } catch (Throwable e0) {
      try {
        sometimesThrow("message".equals(e0.getMessage()));
      } catch (Exception e1) {
        return e0.getMessage().length();
      }
    }
    return -1;
  }
}
