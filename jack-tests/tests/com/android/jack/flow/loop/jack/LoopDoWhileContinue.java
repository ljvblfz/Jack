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

public class LoopDoWhileContinue {

  public int loop() {
    int a = 0;
    int i = 0;
    do {
      i = i + 1;
      if (i == 5)
        continue;
      a = a + i;
    } while ( i < 9 );
    return a;
  }

  public int loopWithLabel() {
    int a = 0;
    int i = 0;
    label:
    do {
      i = i + 1;
      do {
        if (i == 5)
          continue label;
      }
      while (i<0);
      a = a + i;
    } while ( i < 9 );
    return a;
  }

  public int loopWithUnusedLabel() {
    int a = 0;
    int i = 0;
    do {
      i = i + 1;
      do {
        if (i == 5)
          continue;
      }
      while (i<0);
      a = a + i;
    } while ( i < 9 );
    return a;
  }
}
