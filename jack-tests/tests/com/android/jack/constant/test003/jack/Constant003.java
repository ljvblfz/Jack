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

package com.android.jack.constant.test003.jack;

public class Constant003 {

  private final int ZERO = 0;
  private static final int ONE = 1;

  public static int getZERO() {
    return ((Constant003) null).ZERO;
  }

  public int get(int value) {
    switch (value) {
      case ZERO:
        return 0;
      case ONE:
        return 1;
      default:
        return 2;
    }
  }
}
