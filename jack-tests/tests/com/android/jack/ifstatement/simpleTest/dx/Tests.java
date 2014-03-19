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

package com.android.jack.ifstatement.simpleTest.dx;

import com.android.jack.ifstatement.simpleTest.jack.If;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about arithmetic.
 */
public class Tests {

  @Test
  public void testTrue() {
    Assert.assertEquals(1, If.testTrue());
  }

  @Test
  public void testTrue2() {
    Assert.assertEquals(-1, If.testTrue2());
  }

  @Test
  public void testFalse() {
    Assert.assertEquals(2, If.testFalse());
  }

  @Test
  public void testFalse2() {
    Assert.assertEquals(-1, If.testFalse2());
  }

  @Test
  public void testFalseEmpty() {
    Assert.assertEquals(3, If.testFalseEmpty());
  }

  @Test
  public void testParam() {
    Assert.assertEquals(3, If.testParam(true));
    Assert.assertEquals(4, If.testParam(false));
  }

  @Test
  public void testLt() {
    Assert.assertEquals(1, If.testLt(1, 2));
    Assert.assertEquals(0, If.testLt(4, 3));
    Assert.assertEquals(0, If.testLt(5, 5));
  }

  @Test
  public void testGt() {
    Assert.assertEquals(0, If.testGt(1, 2));
    Assert.assertEquals(1, If.testGt(4, 3));
    Assert.assertEquals(0, If.testGt(5, 5));
  }

  @Test
  public void testLte() {
    Assert.assertEquals(1, If.testLte(1, 2));
    Assert.assertEquals(0, If.testLte(4, 3));
    Assert.assertEquals(1, If.testLte(5, 5));
  }

  @Test
  public void testGte() {
    Assert.assertEquals(0, If.testGte(1, 2));
    Assert.assertEquals(1, If.testGte(4, 3));
    Assert.assertEquals(1, If.testGte(5, 5));
  }

  @Test
  public void testEq() {
    Assert.assertEquals(0, If.testEq(1, 2));
    Assert.assertEquals(0, If.testEq(4, 3));
    Assert.assertEquals(1, If.testEq(5, 5));
  }

  @Test
  public void testNeq() {
    Assert.assertEquals(1, If.testNeq(1, 2));
    Assert.assertEquals(1, If.testNeq(4, 3));
    Assert.assertEquals(0, If.testNeq(5, 5));
  }

  @Test
  public void testCst() {
    Assert.assertTrue(If.testCst(5));
    Assert.assertFalse(If.testCst(7));
  }

  @Test
  public void testNested() {
    Assert.assertTrue(If.testNested());
  }
}
