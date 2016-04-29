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

package com.android.jack.unary.test006.dx;

import com.android.jack.unary.test006.jack.UnaryDecrement;

import org.junit.Assert;
import org.junit.Test;

/**
 * Regression test for decrement in array assign.
 */
public class Tests {
  @Test
  public void testDecrementWithArray() {
    int[] array = new int[1];
    Assert.assertEquals(-1, UnaryDecrement.setValueAndGetDecrementedIndex(array, 0));
    Assert.assertEquals(0, array[0]);
  }

}
