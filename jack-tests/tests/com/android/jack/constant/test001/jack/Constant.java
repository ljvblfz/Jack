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

package com.android.jack.constant.test001.jack;

public class Constant {

  public static boolean getBooleanTrue() {
    return true;
  }

  public static boolean getBooleanFalse() {
    return false;
  }

  public static char getChar() {
    return 'd';
  }

  public static double getDouble() {
    return 12.3;
  }

  public static float getFloat() {
    return 23.4f;
  }

  public static int getInt() {
    return 1337;
  }

  public static long getLong() {
    return 345l;
  }

  public static byte getByte() {
    return -1;
  }

  public static short getShort() {
    return 456;
  }

  public static String getString() {
    return "abc";
  }

  public static Object getNull() {
    return null;
  }

  public static Class getObjectClass() {
    return Object.class;
  }

  public static Class getConstantClass() {
    return Constant.class;
  }

  public static Class getIntClass() {
    return int.class;
  }

  public static Class getArrayClass() {
    return int[].class;
  }
}
