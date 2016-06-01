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

package com.android.jack.optimizations.inlinemethod.test002.dx;

import com.android.jack.optimizations.inlinemethod.test002.jack.TestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * Verify exception handling.
 */
public class Tests {

  @Test
  public void test01() {
    TestCase t1 = new TestCase();
    int result1 = t1.callInlineMe01NoCatch(1);
    Assert.assertEquals(1, t1.calledInlineMe04);
    Assert.assertEquals(1, result1);

    TestCase t2 = new TestCase();
    boolean caughtException = false;
    try {
      int result2 = t2.callInlineMe01NoCatch(-1);
    } catch (RuntimeException e) {
      caughtException = true;
    }

    Assert.assertEquals(1, t2.calledInlineMe04);
    Assert.assertTrue(caughtException);
  }

  @Test
  public void test02() {
    TestCase t1 = new TestCase();
    int result1 = t1.callInlineMe01WithCatchNpe(-2);
    Assert.assertEquals(1, t1.calledInlineMe04);
    Assert.assertEquals(1, t1.calledInlineMe01CatchNpe);
    Assert.assertEquals(-1, result1);
  }

}

