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

public class LoopDoWhileLiteral {

  public int doWhileFalse() {
    int a = 1;
    do {
      a = a * 2;
    } while (false);
    return a;
  }

  public int doWhileTrue() {
    int a = 1;
    do {
      a = a * 2;
      if (a >= 128) {
        return a;
      }
    } while (true);
  }

}
