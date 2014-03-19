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

package com.android.jack.conditional.test007.jack;

public class ConditionalTest007 {

  @SuppressWarnings("boxing")
  public static short test001(int test, boolean condition) {

    Byte b1 = new Byte((byte) 1);
    byte b2 = 1;

    Short s1 = new Short((short) 2);
    short s2 = 2;

    switch (test) {
      case 1:
        return condition ? b1 : s1;
     case 2:
       return condition ? b2 : s1;
     case 3:
       return condition ? b1 : s2;
     case 4:
       return condition ? b2 : s2;
      default:
        throw new AssertionError();
    }
  }

  public static byte test002a(boolean condition) {
    byte b = 1;

    byte resultByte = condition ? b : 2*2;

    return resultByte;
  }

  public static short test002b(boolean condition) {
    short s = 1;

    short resultShort = condition ? s : 2*2;

    return resultShort;
  }

  public static char test002c(boolean condition) {
    char c = 'a';

    char resultChar = condition ? c : 2*2;

    return resultChar;
  }


  public static byte test003a(boolean condition) {
    @SuppressWarnings("boxing")
    Byte b = 1;

    @SuppressWarnings("boxing")
    byte resultByte = condition ? b : 2*2;

    return resultByte;
  }

  public static short test003b(boolean condition) {
    @SuppressWarnings("boxing")
    Short s = 1;

    @SuppressWarnings("boxing")
    short resultShort = condition ? s : 2*2;

    return resultShort;
  }

  public static char test003c(boolean condition) {
    @SuppressWarnings("boxing")
    Character c = 'a';

    @SuppressWarnings("boxing")
    char resultChar = condition ? c : 2*2;

    return resultChar;
  }

}
