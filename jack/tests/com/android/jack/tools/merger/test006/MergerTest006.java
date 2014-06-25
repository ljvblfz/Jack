/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"));
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

package com.android.jack.tools.merger.test006;

import com.android.jack.Main;
import com.android.jack.TestTools;
import com.android.jack.tools.merger.MergerTestTools;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test checking that merging of annotation declaration with default values works.
 */
public class MergerTest006 extends MergerTestTools {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }


  @Test
  public void testMergerFromAnnotationTest() throws Exception {
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("annotation/test001"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("annotation/test002"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("annotation/test003"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("annotation/test004"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("annotation/test005"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("annotation/test006"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("annotation/test007"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("annotation/test008"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("annotation/test009"), false /* withDebug */));
  }

  @Test
  public void testMergerFromArithmeticTest() throws Exception {
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("arithmetic/test001"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("arithmetic/test002"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("arithmetic/test003"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("arithmetic/test004"), false /* withDebug */));
  }

  @Test
  public void testMergerFromArrayTest() throws Exception {
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("newarray/test001"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("newarray/test002"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("newarray/test003"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("newarray/test004"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("newarray/test005"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("array/test001"), false /* withDebug */));
  }

  @Test
  public void testMergerFromAssertionTest() throws Exception {
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("assertion/test001"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("assertion/test002"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("assertion/test003"), false /* withDebug */));
  }

  @Test
  public void testMergerFromAssignmentTest() throws Exception {
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("box/test001"), false /* withDebug */));
  }

  @Test
  public void testMergerFromBoxTest() throws Exception {
    Assert.assertFalse(compareMonoDexWithOneDexPerType(TestTools.getJackTestsWithJackFolder("assign"),
        false /* withDebug */));
  }

  @Test
  public void testMergerFromBridgeTest() throws Exception {
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("bridge/test001"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("bridge/test002"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("bridge/test003"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("bridge/test004"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("bridge/test005"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("bridge/test006"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("bridge/test007"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("bridge/test008"), false /* withDebug */));
  }

  @Test
  public void testMergerFromTryCatchFinallyTest() throws Exception {
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("tryfinally/finally002"), false /* withDebug */));
  }

  @Test
  public void testMergerFromUnaryTest() throws Exception {
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("unary/test001"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("unary/test002"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("unary/test003"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("unary/test004"), false /* withDebug */));
    Assert.assertFalse(compareMonoDexWithOneDexPerType(
        TestTools.getJackTestsWithJackFolder("unary/test005"), false /* withDebug */));
  }
}
