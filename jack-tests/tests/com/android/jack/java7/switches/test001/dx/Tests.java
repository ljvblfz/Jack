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

package com.android.jack.java7.switches.test001.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.java7.switches.test001.jack.SwitchTest;

/**
 * Test switches with strings.
 */
public class Tests {

  @Test
  public void test001() {
    Assert.assertEquals(1, SwitchTest.switch001("a"));
    Assert.assertEquals(2, SwitchTest.switch001("b"));
    Assert.assertEquals(3, SwitchTest.switch001("c"));
  }

  @Test
  public void test002() {
    Assert.assertEquals(2, SwitchTest.switch002("a"));
    Assert.assertEquals(2, SwitchTest.switch002("b"));
    Assert.assertEquals(3, SwitchTest.switch002("c"));
  }

  @Test
  public void test003() {
    Assert.assertEquals(2, SwitchTest.switch003("a"));
    Assert.assertEquals(2, SwitchTest.switch003("b"));
    Assert.assertEquals(3, SwitchTest.switch003("c"));
  }

  @Test
  public void test004() {
    Assert.assertEquals(1, SwitchTest.switch004("ac"));
    Assert.assertEquals(2, SwitchTest.switch004("ab"));
    Assert.assertEquals(3, SwitchTest.switch004("c"));
  }

  @Test
  public void test005() {
    Assert.assertEquals(1, SwitchTest.switch005("Aa"));
    Assert.assertEquals(2, SwitchTest.switch005("BB"));
    Assert.assertEquals(3, SwitchTest.switch005("AaBB"));
    Assert.assertEquals(4, SwitchTest.switch005("BBAa"));
    Assert.assertEquals(5, SwitchTest.switch005("BBAaC"));
  }

  @Test
  public void test006() {
    Assert.assertEquals(1, SwitchTest.switch006("ac"));
    Assert.assertEquals(2, SwitchTest.switch006("ab"));
    Assert.assertEquals(3, SwitchTest.switch006("c"));
  }

  @Test
  public void test007() {
    try {
      Assert.assertEquals(1, SwitchTest.switch004(null));
      Assert.fail();
    } catch (NullPointerException e) {
      // test Ok
    }
  }

  @Test
  public void test008() {
    Assert.assertEquals(1, SwitchTest.switch007("a", "b"));
    Assert.assertEquals(2, SwitchTest.switch007("a", "c"));
    Assert.assertEquals(3, SwitchTest.switch007("b", "a"));
    Assert.assertEquals(4, SwitchTest.switch007("b", "b"));
    Assert.assertEquals(4, SwitchTest.switch007("b", "c"));
    Assert.assertEquals(5, SwitchTest.switch007("c", "c"));
  }
}