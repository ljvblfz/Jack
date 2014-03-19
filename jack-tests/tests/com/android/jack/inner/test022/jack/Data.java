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

package com.android.jack.inner.test022.jack;

public class Data {

  public int value(int val) {
    final int a = val;
    final int b = val;
    final int c = val;
    final int d = val;

    Sum s = new Sum() {
      @Override
      public int values() {
        return a + b + c + d;
      }
    };

    int result = s.values();
    return result;
  }
}