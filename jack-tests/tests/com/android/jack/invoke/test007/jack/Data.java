/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.invoke.test007.jack;

public class Data {

  public long sum(long l1, long l2, long l3) {
    return l1 + l2 + l3;
  }

  public long test() {
    int shift = 1;
    long l1 = 1;
    long l2 = 2;
    long l3 = 3;
    return sum(l3, l2, l1);
  }
}
