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

package com.android.jack.optimizations.notsimplifier.test001.dx;

import junit.framework.Assert;

import org.junit.Test;

import com.android.jack.optimizations.notsimplifier.test001.jack.NotSimplifier;

public class Tests {

  //Check that expression is replaced by i1 != i2
  @Test
  public void test001() {
     Assert.assertFalse(NotSimplifier.test001(1, 2));
     Assert.assertFalse(NotSimplifier.test001(2, 1));
     Assert.assertTrue(NotSimplifier.test001(2, 2));
  }

  // Check that expression is replaced by i1 >= i2
  @Test
  public void test002() {
     Assert.assertFalse(NotSimplifier.test002(1, 2));
     Assert.assertTrue(NotSimplifier.test002(2, 1));
     Assert.assertTrue(NotSimplifier.test002(2, 2));
  }

  //Check that expression is replaced by i1 >= i2 || i3 == i4
  @Test
  public void test003() {
     Assert.assertTrue(NotSimplifier.test003(1, 2, 3, 3));
     Assert.assertFalse(NotSimplifier.test003(1, 2, 3, 4));
     Assert.assertTrue(NotSimplifier.test003(2, 2, 3, 4));
  }

  // Check that expression is replaced by i1 >= i2 || b
  @Test
  public void test004() {
     Assert.assertTrue(NotSimplifier.test004(1, 2, true));
     Assert.assertFalse(NotSimplifier.test004(1, 2, false));
     Assert.assertTrue(NotSimplifier.test004(2, 1, false));
     Assert.assertTrue(NotSimplifier.test004(2, 1, true));
  }

  // Check that expression is replaced by i1 >= i2 || i3 != i4
  @Test
  public void test005() {
     Assert.assertFalse(NotSimplifier.test005(1, 2, 3, 3));
     Assert.assertTrue(NotSimplifier.test005(1, 2, 3, 4));
     Assert.assertTrue(NotSimplifier.test005(2, 2, 3, 4));
     Assert.assertTrue(NotSimplifier.test005(2, 2, 3, 3));
  }

  // Check that expression is not replaced by !getBoolean() || b
  @Test
  public void test006() {
     Assert.assertTrue(NotSimplifier.test006(1, 2));
     Assert.assertFalse(NotSimplifier.test006(2, 2));
  }

  // Check that expression is replaced by getBoolean && i1 != i2
  @Test
  public void test007() {
     Assert.assertTrue(NotSimplifier.test007(1, 2));
     Assert.assertFalse(NotSimplifier.test007(2, 2));
  }

  // // Check that expression is replaced by getBoolean() || getBoolean() || i1 == i2
  @Test
  public void test008() {
     Assert.assertTrue(NotSimplifier.test008(1, 2));
     Assert.assertTrue(NotSimplifier.test008(2, 2));
  }

  //Check that expression is replaced by !getBoolean() || i1 == i2 || i3 == i4
  @Test
  public void test009() {
     Assert.assertFalse(NotSimplifier.test009(1, 2, 3, 4));
     Assert.assertTrue(NotSimplifier.test009(1, 2, 3, 3));
     Assert.assertTrue(NotSimplifier.test009(2, 2, 3, 4));
     Assert.assertTrue(NotSimplifier.test009(2, 2, 3, 3));
  }

  @Test
  public void test010() {
    Assert.assertFalse(NotSimplifier.test010(true));
    Assert.assertTrue(NotSimplifier.test010(false));
  }

  //Check that expression !(b1 | b2) is not replaced
  @Test
  public void test011() {
    Assert.assertFalse(NotSimplifier.test011(true, true));
    Assert.assertFalse(NotSimplifier.test011(true, false));
    Assert.assertFalse(NotSimplifier.test011(false, true));
    Assert.assertTrue(NotSimplifier.test011(false, false));
  }

  // Check that expression !(b1 & b2) is not replaced
  @Test
  public void test012() {
    Assert.assertFalse(NotSimplifier.test012(true, true));
    Assert.assertTrue(NotSimplifier.test012(true, false));
    Assert.assertTrue(NotSimplifier.test012(false, true));
    Assert.assertTrue(NotSimplifier.test012(false, false));
  }

  //Check that expression !(b1 ^ b2) is not replaced
  @Test
  public void test013() {
    Assert.assertTrue(NotSimplifier.test013(true, true));
    Assert.assertTrue(NotSimplifier.test013(false, false));
    Assert.assertFalse(NotSimplifier.test013(false, true));
    Assert.assertFalse(NotSimplifier.test013(true, false));
  }

  // Check that expression is replaced by !b1 | b2
  @Test
  public void test014() {
    Assert.assertTrue(NotSimplifier.test014(true, true));
    Assert.assertTrue(NotSimplifier.test014(false, false));
    Assert.assertTrue(NotSimplifier.test014(false, true));
    Assert.assertFalse(NotSimplifier.test014(true, false));
  }

  // Check that expression is replaced by b1 & !b2
  @Test
  public void test015() {
    Assert.assertFalse(NotSimplifier.test015(true, true));
    Assert.assertFalse(NotSimplifier.test015(false, false));
    Assert.assertFalse(NotSimplifier.test015(false, true));
    Assert.assertTrue(NotSimplifier.test015(true, false));
  }

  @Test
  public void test016() {
    Assert.assertTrue(NotSimplifier.test016(false, true));
    Assert.assertFalse(NotSimplifier.test016(false, false));
  }

  @Test
  public void test017() {
    Assert.assertTrue(NotSimplifier.test017(false, true));
    Assert.assertFalse(NotSimplifier.test017(false, false));
  }

  @Test
  public void test018() {
    Assert.assertTrue(NotSimplifier.test017(false, true));
    Assert.assertFalse(NotSimplifier.test017(false, false));
  }
}
