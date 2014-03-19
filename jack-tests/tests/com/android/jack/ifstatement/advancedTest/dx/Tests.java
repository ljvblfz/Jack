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

package com.android.jack.ifstatement.advancedTest.dx;

import com.android.jack.ifstatement.advancedTest.jack.IfAdvanced;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about advanced if statement.
 */
public class Tests {

  @Test
  public void testOr() {
    Assert.assertEquals(1, IfAdvanced.testOr(true, true));
    Assert.assertEquals(1, IfAdvanced.testOr(true, false));
    Assert.assertEquals(1, IfAdvanced.testOr(false, true));
    Assert.assertEquals(0, IfAdvanced.testOr(false, false));
  }

  @Test
  public void testAnd() {
    Assert.assertEquals(1, IfAdvanced.testAnd(true, true));
    Assert.assertEquals(0, IfAdvanced.testAnd(true, false));
    Assert.assertEquals(0, IfAdvanced.testAnd(false, true));
    Assert.assertEquals(0, IfAdvanced.testOr(false, false));
  }

  @Test
  public void testConditional() {
    Assert.assertEquals(0, IfAdvanced.testConditional(false));
    Assert.assertEquals(1, IfAdvanced.testConditional(true));
  }

  @Test
  public void testElseIf() {
    Assert.assertEquals(0, IfAdvanced.testElseIf(0));
    Assert.assertEquals(1, IfAdvanced.testElseIf(1));
    Assert.assertEquals(-1, IfAdvanced.testElseIf(3));
  }

  @Test
  public void testMix() {
    Assert.assertEquals(0, IfAdvanced.testMix(0, 8));
    Assert.assertEquals(1, IfAdvanced.testMix(2, 6));
    Assert.assertEquals(1, IfAdvanced.testMix(2, 5));
  }

  @Test
  public void testBraces() {
    Assert.assertFalse(IfAdvanced.testBraces());
  }

  @Test
  public void testNoReturnInBranch() {
    Assert.assertEquals(IfAdvanced.testNoReturnInBranch(5), 5);
    Assert.assertEquals(IfAdvanced.testNoReturnInBranch(-7), 7);
  }

  @Test
  public void testIfFalse1() {
    Assert.assertEquals(2, new IfAdvanced().testIfFalse1());
  }

  @Test
  public void testIfFalse2() {
    Assert.assertEquals(2, new IfAdvanced().testIfFalse2());
  }

  @Test
  public void testIfFalse3() {
    Assert.assertEquals(2, new IfAdvanced().testIfFalse3());
  }

  @Test
  public void testIfTrue1() {
    Assert.assertEquals(1, new IfAdvanced().testIfTrue1());
  }

  @Test
  public void testIfTrue2() {
    Assert.assertEquals(1, new IfAdvanced().testIfTrue2());
  }

  @Test
  public void testIfTrue3() {
    Assert.assertEquals(1, new IfAdvanced().testIfTrue3());
  }

  @Test
  public void testEmptyIfThen() {
    Assert.assertEquals(1, new IfAdvanced().emptyIfThen(true));
    Assert.assertEquals(2, new IfAdvanced().emptyIfThen(false));
  }

  @Test
  public void testEmptyIfElse() {
    Assert.assertEquals(2, new IfAdvanced().emptyIfElse(true));
    Assert.assertEquals(1, new IfAdvanced().emptyIfElse(false));
  }
}
