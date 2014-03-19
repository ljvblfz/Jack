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

package com.android.jack.cast.useless002.jack;

/**
 * JUnit test allowing to verify that useless casts are not generated.
 */
public class UselessCast {

  // Jack IR should not contains cast between int to long.
  public static long uselessCast(int i1, int i2, int i3) {
    if (i1 < i2) {
      return i3 != 0 ? 1 : 0;
    } else {
      return 0;
    }
  }
}
