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

package com.android.jack.assign.test001.jack;

public class Assignments {
  public static int field = 5;

  public static int getLvValue() {
    int lv = lv = 5;
    return lv;
  }

  public static int getFieldValue() {
    field = field = 5;
    return field;
  }

  public static int getParameterValue(int value) {
    value = value = 5;
    return value;
  }
}
