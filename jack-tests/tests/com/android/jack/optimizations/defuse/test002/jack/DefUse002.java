/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.optimizations.defuse.test002.jack;

public class DefUse002 {

  public static int get(boolean isA, boolean isB, boolean isC) {
    int tac1;
    if (isA) {
      tac1 = 1;
    } else {
      tac1 = 2;
    }
    int x = tac1;

    int tac2;
    if (isB) {
      tac2 = 3;
    } else {
      tac2 = 4;
    }
    int y = tac2;

    int tac3;
    if (isC) {
      tac3 = x;
    } else {
      tac3 = y;
    }
    int z = tac3;

    return z;
  }

}
