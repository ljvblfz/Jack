/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.optimizations.inlinemethod.test001.dx;

import com.android.jack.optimizations.inlinemethod.test001.jack.TestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * Verify body of the function inlined were executed as well as some complex control flow within
 * the body involving return statements.
 */
public class Tests {
  @Test
  public void test01() {
    TestCase t = new TestCase();
    t.callInlineMe01Once();
    Assert.assertEquals(1, t.calledInlineMe01);
  }

  @Test
  public void test02() {
    TestCase t = new TestCase();
    t.callInlineMe01Twice();
    Assert.assertEquals(2, t.calledInlineMe01);
  }

  @Test
  public void test03() {
    TestCase t = new TestCase();
    t.callInlineMe01Loop100x();
    Assert.assertEquals(100, t.calledInlineMe01);
  }

  @Test
  public void test04() {
    TestCase t = new TestCase();
    t.callInlineMe02IgnoreReturn();
    Assert.assertEquals(1, t.calledInlineMe02);
  }

  @Test
  public void test05() {
    TestCase t = new TestCase();
    int result = t.callInlineMe02UseReturn();
    Assert.assertEquals(1, t.calledInlineMe02);
    Assert.assertEquals(200, result);
  }

  @Test
  public void test06() {
    TestCase t = new TestCase();
    int result = t.callBothInlineMe01andInlineMe02();
    Assert.assertEquals(1, t.calledInlineMe01);
    Assert.assertEquals(1, t.calledInlineMe02);
    Assert.assertEquals(200, result);
  }

  @Test
  public void test07() {
    TestCase t = new TestCase();
    int result = t.callInlineMe02Nested(11);
    Assert.assertEquals(0, t.calledInlineMe01);
    Assert.assertEquals(4, t.calledInlineMe02);
    Assert.assertEquals(11 * 2 * 2 * 2 * 2, result);
  }

  @Test
  public void test08() {
    TestCase t = new TestCase();
    int result = t.callInlineMe03(1, 100, 200);
    Assert.assertEquals(0, t.calledInlineMe01);
    Assert.assertEquals(0, t.calledInlineMe02);
    Assert.assertEquals(1, t.calledInlineMe03);
    Assert.assertEquals(100 + 200, result);
  }

  @Test
  public void test09() {
    TestCase t = new TestCase();
    int result = t.callInlineMe03(2, 100, 200);
    Assert.assertEquals(0, t.calledInlineMe01);
    Assert.assertEquals(0, t.calledInlineMe02);
    Assert.assertEquals(1, t.calledInlineMe03);
    Assert.assertEquals(100 - 200, result);
  }

  @Test
  public void test10() {
    TestCase t = new TestCase();
    int result = t.callInlineMe03(-100, -100, 200);
    Assert.assertEquals(0, t.calledInlineMe01);
    Assert.assertEquals(0, t.calledInlineMe02);
    Assert.assertEquals(1, t.calledInlineMe03);
    Assert.assertEquals(-1, result);
  }

  @Test
  public void test11() {
    TestCase t = new TestCase();
    int result = t.callInlineMe03(3, 1, 99);
    Assert.assertEquals(0, t.calledInlineMe01);
    Assert.assertEquals(1, t.calledInlineMe02);
    Assert.assertEquals(1, t.calledInlineMe03);
    Assert.assertEquals(2 * 99 * 99, result);
  }

  @Test
  public void test12() {
    TestCase t = new TestCase();
    int result = t.callInlineMe03(3, 4, 99);
    Assert.assertEquals(0, t.calledInlineMe01);
    Assert.assertEquals(2, t.calledInlineMe02);
    Assert.assertEquals(1, t.calledInlineMe03);
    Assert.assertEquals(2 * 2 * 3 * 99 * 99, result);
  }

  @Test
  public void test13() {
    TestCase t = new TestCase();
    int result = t.callInlineMe03(3, 4, 99);
    Assert.assertEquals(0, t.calledInlineMe01);
    Assert.assertEquals(2, t.calledInlineMe02);
    Assert.assertEquals(1, t.calledInlineMe03);
    Assert.assertEquals(2 * 2 * 3 * 99 * 99, result);
  }

  @Test
  public void test14() {
    Assert.assertEquals("Called, x == true", TestCase.callInlineMe04(true));
    Assert.assertEquals("Called, x != true", TestCase.callInlineMe04(false));
  }
}

