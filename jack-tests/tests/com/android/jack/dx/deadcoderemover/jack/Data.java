/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.dx.deadcoderemover.jack;

public class Data {
  public int allRemoved(int x, int y, boolean b1, boolean b2) {
    // RegOps.ADD
    x = x + 3;
    // RegOps.SUB
    y = y - x;
    // RegOps.MUL
    x = x * y;
    // RegOps.NEG
    y = -x;
    // RegOps.AND
    boolean b = b1 && b2;
    // RegOps.XOR
    x = x ^ 10;
    // RegOps.SHL
    y = x >> 1;
    // RegOps.SHR
    x = y << 3;
    // RegOps.USHR
    y = x >>> 99;
    // RegOps.NOT
    b = !b1;

    // Not possible with the current code gen + DCE algorithm.
    // RegOps.CMPL
    // b1 = x < y;
    // RegOps.CMPG
    // b2 = f1 < f2;

    // RegOps.CONV
    double d = (double) x * y;
    // RegOps.TO_BYTE
    byte bt = (byte) d;
    // RegOps.TO_CHAR
    char c = (char) bt;
    // case RegOps.TO_SHORT
    short s = (short) c;
    return 1;
  }

  public int notRemoved(int x) {
    x = x / 0;
    return 1;
  }
}
