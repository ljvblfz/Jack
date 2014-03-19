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

package com.android.jack.cast.implicit003.dx;


import com.android.jack.cast.implicit003.jack.Data;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about arithmetic.
 */
public class Tests {

  @SuppressWarnings("boxing")
  @Test
  public void test1() {
    boolean cond = true;
    boolean cond2 = false;
    byte b = 13;
    Byte B = b;
    short s = 14;
    char c = 15;
    int i = 16;
    long l = 17;
    Long L = l;
    float f = 0.18f;
    double d = 0.19;

    Assert.assertEquals(cond ? i : B, Data.conditonalIByte(cond, i, B));
    Assert.assertEquals((Object) (cond ? i : L), (Object) Data.conditonalILong(cond, i, L));
  }

}
