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

package com.android.jack.flow.loop.jack;

public class LoopWhileContinue {

  public int loop() {
    int a = 0;
    int i = 0;
    while ( i < 9 ) {
      i = i + 1;
      if (i == 5)
        continue;
      a = a + i;
    }
    return a;
  }

  public int loopWithLabel() {
    int a = 0;
    int i = 0;
    label:
    while ( i < 9 ) {
      i = i + 1;
      boolean bool = true;
      while(bool) {
      if (i == 5)
        continue label;
      bool = false;
      }
      a = a + i;
    }
    return a;
  }

  public int loopWithUnusedLabel() {
    int a = 0;
    int i = 0;
    label:
    while ( i < 9 ) {
      i = i + 1;
      boolean bool = true;
      while(bool) {
      bool = false;
      if (i == 5)
        continue;
      }
      a = a + i;
    }
    return a;
  }
}
