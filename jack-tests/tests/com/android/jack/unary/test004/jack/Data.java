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

package com.android.jack.unary.test004.jack;

public class Data {

  public static int getPreIncValue(byte value) {
    // This will be transform to value = value + 1 by jack
    return ++value;
  }

  public static int getPostIncValue(byte value) {
    // This will be transform to (tmp = value + 1, value = tmp; tmp) by jack
    return value++;
  }

  public static int getPreIncShortValue(short value) {
    // This will be transform to value = value + 1 by jack
    return ++value;
  }

  public static int getPreIncCharValue(char value) {
    // This will be transform to value = value + 1 by jack
    return ++value;
  }

  public static long getPreIncIntValue(int value) {
    // This will be transform to value = value + 1 by jack
    return ++value;
  }
}

