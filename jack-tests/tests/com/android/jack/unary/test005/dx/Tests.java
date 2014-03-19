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

package com.android.jack.unary.test005.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.unary.test005.jack.UnaryNot;

/**
 * Tests '!' operator with comparison operators.
 */
public class Tests {

  @Test
  public void notWithAComparison() {
    Assert.assertEquals(2, UnaryNot.getValue1(1, 2));
    Assert.assertEquals(2, UnaryNot.getValue1(3, 5));
    Assert.assertEquals(1, UnaryNot.getValue1(5, 3));
    Assert.assertEquals(1, UnaryNot.getValue1(2, 2));
    Assert.assertEquals(1, UnaryNot.getValue1(2, 1));
  }

  @Test
  public void notWithComparisons() {
    Assert.assertEquals(2, UnaryNot.getValue2(1, 2, 3, 5));
    Assert.assertEquals(2, UnaryNot.getValue2(3, 5, 6, 7));
    Assert.assertEquals(1, UnaryNot.getValue2(5, 3, 1, 2));
    Assert.assertEquals(1, UnaryNot.getValue2(2, 2, 5, 5));
    Assert.assertEquals(1, UnaryNot.getValue2(2, 1, 5, 3));
  }
}
