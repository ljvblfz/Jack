/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.ssa.test001.dx;

import com.android.jack.ssa.test001.jack.Ssa;

import org.junit.Assert;
import org.junit.Test;

public class Tests {
  @Test
  public void testNestedCatch() {
    Assert.assertEquals("message".length(), Ssa.doubleNestedCatch());
  }

  @Test
  public void testMultipleUses() {
    int x = 11;
    int y = 51;
    int z = 91;

    int a = x + y;
    int b = y + z;
    int c = x + z;

    int x_1 = x + 1;
    int y_1 = y + 1;
    int z_1 = z + 1;

    int result = a + b + c + x_1 + y_1 + z_1;

    Assert.assertEquals(result, Ssa.multipleUses(x, y, z));
  }

  @Test
  public void testFallThroughCaseWithPhi() {
    Assert.assertEquals(2, Ssa.fallThroughCaseWithPhi(2, -1));
  }
}
