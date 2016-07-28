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

package com.android.jack.ifstatement.shortCondition.dx;

import com.android.jack.ifstatement.shortCondition.jack.If;

import org.junit.Assert;
import org.junit.Test;

/** Testing removal of redundant conditional branches. */
public class Tests {

  @Test
  public void testSimple() {
    Assert.assertEquals(1, new If().shortCircuit1(true));
    Assert.assertEquals(1, new If().shortCircuit1(false));
  }

  @Test
  public void testLong() {
    Assert.assertEquals(1, new If().shortCircuit2(true, true, true, true, true));
    Assert.assertEquals(1, new If().shortCircuit2(true, false, true, true, true));
    Assert.assertEquals(1, new If().shortCircuit2(true, true, false, true, true));
    Assert.assertEquals(1, new If().shortCircuit2(true, true, true, false, true));
    Assert.assertEquals(1, new If().shortCircuit2(true, true, true, false, false));
  }

  @Test
  public void testSideEffect() {
    Assert.assertEquals(1, new If().sideEffect(1));
    Assert.assertEquals(2, new If().sideEffect(2));
    Assert.assertEquals(3, new If().sideEffect(3));
    Assert.assertEquals(4, new If().sideEffect(4));
    Assert.assertEquals(5, new If().sideEffect(5));
  }
}
