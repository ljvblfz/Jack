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

package com.android.jack.cast.implicit004.dx;


import com.android.jack.cast.implicit004.jack.Data;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about arithmetic.
 */
public class Tests {

  @Test
  public void test1() {
    byte[] array = new byte[]{1, 2, 3};
    int index = 2;
    byte or = 127;
    int m1 = array[index] | or;
    Assert.assertEquals(m1, Data.m1(array, index, or));
    Assert.assertEquals(m1, array[index]);
  }

  @Test
  public void test2() {
    double[] array = new double[]{1.0, 2.0, 3.0};
    int index = 2;
    byte or1 = 127;
    byte or2 = 7;
    double m2 = or1 | or2;
    Assert.assertEquals(m2, Data.m2(array, index, or1, or2), 0.0);
    Assert.assertEquals(m2, array[index], 0.0);
  }

  @Test
  public void test3() {
    char[] array = new char[]{1, 2, 3};
    int index = 2;
    byte or = 127;
    char m3 = (char) (array[index] | or);
    Assert.assertEquals(m3, Data.m3(array, index, or));
    Assert.assertEquals(m3, array[index]);
  }

  @Test
  public void test4() {
    byte[] array = new byte[]{1, 2, 3};
    int index = 2;
    byte b = 127;
    byte m4 = (byte) ~b;
    Assert.assertEquals(m4, Data.m4(array, index, b));
    Assert.assertEquals(m4, array[index]);
  }

  @Test
  public void test5() {
    byte[] array = new byte[]{1, 2, 3};
    int index = 2;
    byte b = 127;
    byte m5 = (byte) (b + 1);
    Assert.assertEquals(m5, Data.m5(array, index, b));
    Assert.assertEquals(m5, array[index]);
  }

  @Test
  public void test6() {
    Assert.assertEquals(-1, Data.m6((byte) 1));
  }

}
