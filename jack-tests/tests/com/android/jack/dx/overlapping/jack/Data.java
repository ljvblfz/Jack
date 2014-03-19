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

package com.android.jack.dx.overlapping.jack;

public class Data {

  private final Object o = null;

  private long getLongValue() {
    return 1024L;
  }

  private static int getResult(Object o1, long l1, long l2, Object o2) {
    return (int)(l1 + l2);
  }

  public final long test001(int i, long position, long size) {
    long alignment = position - position % getLongValue();
    int offset = (int) (position - alignment);
    int result = Data.getResult(o, alignment, size + offset, null);
    return result;
  }


  public static long test002(int val0, long val1, long val2) {
    long val3 = val1 - 1024l;
    return (compute(1, val3, val2, val0));
  }

  private static long compute(int val0, long val1, long val2, int val3) {
    return val0 + val1 + val2 + val3;
  }
}
