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

package com.android.jack.threeaddress.test001.jack;

public class ThreeAddressCode003 {

  public static int assignIntoExpr001() {
    int lv1 = 1;
    int lv2;
    return lv2 = lv1;
  }

  public static int assignIntoExpr002() {
    int lv1 = 1;
    int lv2;
    int lv3;
    return lv3 = lv2 = ++lv1;
  }

  public static int index = 0;

  public static int getIndex() {
    return index++;
  }

  public static int assignIntoExpr003() {
    int lv1 = 1;
    int a[] = new int[5];
    int j = 0;
    j = a[getIndex()] = ++lv1;
    return getIndex();
  }

  public static int assignIntoExpr004() {
    int lv1 = 1;
    int a[] = new int[5];
    int j = 0;
    if ((j = a[getIndex()] = ++lv1) == 2) {
      return 3;
    }
    return getIndex();
  }

  public static int test() {
    byte[] tokenTypes = new byte[256];
    byte TOKEN_DIGIT = 16;

    tokenTypes['.'] |= TOKEN_DIGIT;

    return tokenTypes['.'];
  }

  private int offset = 1;
  public char next() {
    String string = "abc";
    return string.charAt(++offset);
  }
}
