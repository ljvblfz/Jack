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

package com.android.jack.comparison.test001.dx;

import com.android.jack.comparison.test001.jack.Data;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about comparisons.
 */
public class Tests {

  @Test
  public void test1() {
    Assert.assertTrue(Data.test001(1));
    Assert.assertFalse(Data.test001(-1));
    Assert.assertTrue(Data.test002(1));
    Assert.assertFalse(Data.test002(-1));
    Assert.assertEquals(1, Data.test003(-1));
    Assert.assertEquals(2, Data.test003(2));
    Assert.assertEquals(-1, Data.test004(-1));
    Assert.assertEquals(1, Data.test004(2));
    Assert.assertEquals(1, Data.test004(9));
    Assert.assertEquals(1, Data.test004(1));
    Assert.assertEquals(1, Data.test004(7));
    Assert.assertEquals(11, Data.test004(11));
    Assert.assertTrue(Data.test005(1));
    Assert.assertTrue(Data.test005(-1));
    Assert.assertTrue(Data.test005(2));
    Assert.assertTrue(Data.test005(7));
    Assert.assertTrue(Data.test005(9));
    Assert.assertTrue(Data.test005(11));
  }

  @Test
  public void test2() {
    Assert.assertTrue(Data.test006("abc", "abc"));
    Assert.assertFalse(Data.test006("abc", new String("abc")));
    Object o = null;
    Assert.assertTrue(Data.test006(o, null));
  }

  @Test
  public void test6() {
    Assert.assertEquals((byte)10, Data.test007(5, (byte) 10));
    Assert.assertEquals(11, Data.test007(11, (byte) 7));
 }

  @Test
  public void test8() {
    Assert.assertEquals(2, Data.test008(1));
  }

  @Test
  public void test9() {
    Assert.assertEquals(2, Data.test009());
  }

  @Test
  public void test10() {
    Assert.assertEquals(2, Data.test010());
  }

  @Test
  public void test11() {
    Assert.assertEquals(1, Data.test011());
  }

  @Test
  public void test12() {
    Data.testFloatCompare(-5.0f, 4.0f, 4.0f, (1.0f/0.0f) / (1.0f/0.0f));
  }
}
