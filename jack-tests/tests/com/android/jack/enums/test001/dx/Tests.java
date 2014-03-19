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

package com.android.jack.enums.test001.dx;

import com.android.jack.enums.test001.jack.Data;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  @Test
  public void test1() {
    Assert.assertArrayEquals(new Data[]{Data.A, Data.B, Data.C, Data.D, Data.E}, Data.values());
    Assert.assertEquals(0, Data.A.ordinal());
    Assert.assertEquals(4, Data.E.ordinal());
    Assert.assertEquals(Data.A, Data.valueOf("A"));
    Assert.assertEquals(Data.B, Enum.valueOf(Data.class, "B"));
  }
  @Test
  public void test2() {
    Data d = Data.D;
    int r = -1;
    switch (d) {
      case A:
        r = 2;
        break;
      case B:
        r = 3;
        break;
      case C:
        r = 14;
        break;
      case D:
        r = 5;
        break;
      case E:
        r = 7;
        break;
      default:
        r = 9;
        break;
    }
    Assert.assertEquals(5, r);
  }
}
