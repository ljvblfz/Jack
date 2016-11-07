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

package com.android.jack.optimizations.inlinemethod.test003.dx;

import com.android.jack.optimizations.inlinemethod.test003.jack.TestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests focuses on different value of 'this' instance in the target method.
 */
public class Tests {
  @Test
  public void test01() {
    int k = 99;
    TestCase.S t = new TestCase.S();
    int result = t.callInlineMeAccessosr(k);
    Assert.assertEquals(k + 1, result);
    Assert.assertEquals(k + 1, t.c1.fieldGetter());
    Assert.assertEquals(k + 1, t.c1.publicField);
  }

  @Test
  public void test02() {
    int k = 99;
    TestCase.S t = new TestCase.S();
    int result = t.callInlineMeDirectAccess(k);
    Assert.assertEquals(k - 1, result);
    Assert.assertEquals(k - 1, t.c1.fieldGetter());
    Assert.assertEquals(k - 1, t.c1.publicField);
  }

  @Test
  public void test03() {
    int k = 99;
    TestCase.S t = new TestCase.S();
    int result = t.callInlineMeInlinedGetter(k);
    Assert.assertEquals(k + 1, result);
    Assert.assertEquals(k + 1, t.c1.fieldGetter());
    Assert.assertEquals(k + 1, t.c1.publicField);
  }
}

