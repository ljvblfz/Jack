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

package com.android.jack.cast.implicit001.dx;


import com.android.jack.cast.implicit001.jack.Data;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about arithmetic.
 */
public class Tests {

  @SuppressWarnings("cast")
  @Test
  public void test1() {
    Assert.assertEquals((float) 27, Data.returnFloat(27), 0.0);
  }

  @SuppressWarnings("cast")
  @Test
  public void test2() {
    Assert.assertEquals((double) (27 + 54), Data.addDouble(27, 54), 0.0);
  }

  @SuppressWarnings("cast")
  @Test
  public void test3() {
    Assert.assertEquals((double) (27 + 58.1), Data.addDouble(27, 58.1), 0.0);
  }

  @Test
  public void test4() {
    Assert.assertEquals( 59 + ((double) 59), Data.assignDouble(59), 0.0);
  }

  @SuppressWarnings("cast")
  @Test
  public void test5() {
    Assert.assertEquals( (float)(31 + 65), Data.addFloat(31, 65), 0.0);
  }

  @Test
  public void test6() {
    Assert.assertEquals( 31 + 67L, Data.addLong(31, 67L));
  }

  @Test
  public void test7() {
    Assert.assertEquals( 31 + (long) 67, Data.addIntWithCall(31, 67));
  }

  @Test
  public void test8() {
    Assert.assertEquals( (byte)(((byte) 31) + ((byte) -1)), Data.addByte((byte) 31, (byte) -1));
  }

  @Test
  public void test9() {
    Assert.assertEquals(3, Data.addByte());
  }

  @Test
  public void test10() {
    Assert.assertEquals(~(167L), Data.not(167L));
  }

  @Test
  public void test11() {
    Assert.assertEquals(~((byte) -1), Data.not((byte) -1));
  }

  @Test
  public void test12() {
    Assert.assertEquals(-((byte) -1), Data.minus((byte) -1));
  }

  @Test
  public void test13() {
    Assert.assertEquals(-(-3L), Data.minus(-3L));
  }

  @Test
  public void test14() {
    Assert.assertEquals(-(-30.5), Data.minus(-30.5), 0.0);
  }

  @Test
  public void test15() {
    Assert.assertEquals(-((char)1587469), Data.minus((char) 1587469));
  }

  @SuppressWarnings("cast")
  @Test
  public void test16() {
    Assert.assertEquals((double)(-((char) 1587467)), Data.minusDouble((char) 1587467), 0.0);
  }

  @Test
  public void test17() {
    Assert.assertEquals(75896L >> 1258632157456L, Data.shr(75896L, 1258632157456L));
  }

  @Test
  public void test18() {
    Assert.assertEquals(75896L >> (byte) 1258632157, Data.shr(75896L, (byte) 1258632157));
  }

  @Test
  public void test19() {
    int[] array = {-2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    Assert.assertEquals(array[(byte) 7], Data.arrayAccess(array, (byte) 7));
  }

  @Test
  public void test20() {
    Assert.assertEquals(31 + (long) 67, Data.addByteWithCall((byte) 31, (byte) 67));
  }

  @Test
  public void test21() {
    String s = "2 1";
    Assert.assertEquals(s, Data.getObjectFromString(s));
  }

  @Test
  public void test22() {
    double value = 57859423453465456.0;
    Assert.assertEquals(value, Data.inc(value), 0.0);
  }

  @Test
  public void test23() {
    byte value = 127;
    Assert.assertEquals(value, Data.incPost(value));
  }

  @Test
  public void test24() {
    byte value = 127;
    byte valueRef = value;
    Assert.assertEquals(++ valueRef, Data.incPre(value));
  }

  @Test
  public void test25() {
    Assert.assertEquals(-1, Data.setByteArray());
  }
}
