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

package com.android.jack.optimizations.ifwithconstantsimplifier.test002;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void testThen() {
    TestFuzz t = new TestFuzz();
    t.testThenBlockOptimization();
    Assert.assertEquals(TestFuzz.ITERATIONS_COUNT, t.mI);
  }

  @Test
  public void testElse() {
    TestFuzz t = new TestFuzz();
    t.testElseBlockOptimization();
    Assert.assertEquals(-TestFuzz.ITERATIONS_COUNT, t.mI);
  }

  @Test
  public void testThen2() {
    TestFuzz t = new TestFuzz();
    t.testThenBlockOptimization2();
    Assert.assertEquals(TestFuzz.ITERATIONS_COUNT, t.mI);
  }

  @Test
  public void testElse2() {
    TestFuzz t = new TestFuzz();
    t.testElseBlockOptimization2();
    Assert.assertEquals(-TestFuzz.ITERATIONS_COUNT, t.mI);
  }
}
