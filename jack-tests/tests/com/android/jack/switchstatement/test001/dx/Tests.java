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

package com.android.jack.switchstatement.test001.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.switchstatement.test001.jack.Switches001;

/**
 * Tests about switches.
 */
public class Tests {

  @Test
  public void test1() {
    Assert.assertEquals(1, Switches001.switch001(1));
    Assert.assertEquals(2, Switches001.switch001(2));
    Assert.assertEquals(3, Switches001.switch001(3));
  }

  @Test
  public void testNoDefault() {
    Switches001 t = new Switches001();
    Assert.assertEquals(1, t.switch003(-1));
    Assert.assertEquals(2, t.switch003(15));
    Assert.assertEquals(-1, t.switch003(2));
  }

  @Test
  public void testNested() {
    Switches001 t = new Switches001();
    Assert.assertEquals(11, t.switch004(1, 10));
    Assert.assertEquals(12, t.switch004(1, 20));
    Assert.assertEquals(21, t.switch004(2, 10));
    Assert.assertEquals(22, t.switch004(2, 20));
    Assert.assertEquals(-1, t.switch004(2, 30));
  }

  @Test
  public void testWithBlocks() {
    Switches001 t = new Switches001();
    Assert.assertEquals(2, t.switch005(1));
    Assert.assertEquals(4, t.switch005(3));
    Assert.assertEquals(-1, t.switch005(5));
  }

  @Test
  public void testN1() {
    Switches001 t = new Switches001();
    Assert.assertEquals(2, t.switch002(-1));

    Assert.assertEquals(-1, t.switch002(9));
    Assert.assertEquals(20, t.switch002(10));
    Assert.assertEquals(-1, t.switch002(11));

    Assert.assertEquals(-1, t.switch002(14));
    Assert.assertEquals(20, t.switch002(15));
    Assert.assertEquals(-1, t.switch002(16));
  }

  @Test
  public void testB1() {
    Switches001 t = new Switches001();
    Assert.assertEquals(-1, t.switch002(Integer.MAX_VALUE));
  }

  @Test
  public void testB2() {
    Switches001 t = new Switches001();
    Assert.assertEquals(-1, t.switch002(Integer.MIN_VALUE));
  }

  @Test
  public void unorderedSwitch() {
    Switches001 t = new Switches001();
    Assert.assertEquals(1, t.switch006(1));
    Assert.assertEquals(2, t.switch006(2));
    Assert.assertEquals(3, t.switch006(3));
  }

  @Test
  public void switchWithFallThrough() {
    Switches001 t = new Switches001();
    Assert.assertEquals(4, t.switch007(1));
    Assert.assertEquals(2, t.switch007(2));
    Assert.assertEquals(4, t.switch007(3));
  }

  @Test
  public void nestedSwitch() {
    Switches001 t = new Switches001();
    Assert.assertEquals(6, t.switch008(1));
    Assert.assertEquals(2, t.switch008(2));
    Assert.assertEquals(8, t.switch008(3));
  }

  @Test
  public void switchWithChar() {
    Switches001 t = new Switches001();
    Assert.assertTrue('#' == t.switch009('#'));
    Assert.assertTrue('A' == t.switch009('A'));
  }

  @Test
  public void switchWithByteShort() {
    Switches001 t = new Switches001();
    Assert.assertEquals(1, t.switch010((byte)1));
    Assert.assertEquals(2, t.switch010((byte)2));
  }

  @Test
  public void switchWithOnlyDefault() {
    Switches001 t = new Switches001();
    Assert.assertEquals(2, t.switch011(1));
    Assert.assertEquals(2, t.switch011(2));
  }

  @Test
  public void switchWithNoCases() {
    Switches001 t = new Switches001();
    Assert.assertEquals(1, t.switch012(1));
    Assert.assertEquals(1, t.switch012(2));
  }

  @Test
  public void switchWithOnlyDefault2() {
    Switches001 t = new Switches001();
    Assert.assertEquals(1, t.switch013(1));
    Assert.assertEquals(1, t.switch013(2));
  }

  @Test
  public void switchWithOnlyDefault3() {
    Switches001 t = new Switches001();
    Assert.assertEquals(1, t.switch014(1));
    Assert.assertEquals(1, t.switch015(2));
  }

  @Test
  public void switchWithOnlyDefaultWithSwitch() {
    Switches001 t = new Switches001();
    Assert.assertEquals(1, t.switch016(1));
    Assert.assertEquals(2, t.switch016(2));
  }

  @Test
  public void switchWithOnlyDefaultWithLoopBreak() {
    Switches001 t = new Switches001();
    Assert.assertEquals(6, t.switch017(1));
    Assert.assertEquals(2, t.switch017(2));
  }

  @Test
  public void switchWithOnlyDefaultWithLabledBreak() {
    Switches001 t = new Switches001();
    Assert.assertEquals(2, t.switch018(1));
    Assert.assertEquals(2, t.switch018(2));
  }
}
