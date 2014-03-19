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

package com.android.jack.field.static004.jack;

public class Data4 {

  public static final int I1 = 1;
  public static final int I2 = 2;
  protected static final char I4 = 'r';
  public static final int I5 = 4 + 9;
  public static final long I6 = 6l;
  public static final float I7 = 7f;
  public static final double I8 = 8.8;
  public static final Object I9 = null;

  public static char getI4() {
    return I4;
  }
}
