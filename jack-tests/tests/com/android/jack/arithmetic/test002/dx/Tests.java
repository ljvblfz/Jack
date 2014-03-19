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

package com.android.jack.arithmetic.test002.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.arithmetic.test002.jack.Add;
import com.android.jack.arithmetic.test002.jack.And;
import com.android.jack.arithmetic.test002.jack.Div;
import com.android.jack.arithmetic.test002.jack.Mod;
import com.android.jack.arithmetic.test002.jack.Mul;
import com.android.jack.arithmetic.test002.jack.Or;
import com.android.jack.arithmetic.test002.jack.Shl;
import com.android.jack.arithmetic.test002.jack.Shr;
import com.android.jack.arithmetic.test002.jack.Sub;
import com.android.jack.arithmetic.test002.jack.Ushr;
import com.android.jack.arithmetic.test002.jack.Xor;

/**
 * Tests about arithmetic.
 */
public class Tests {

  @Test
  public void test1() {
    Assert.assertEquals(1 + 2,  Add.apply(1, 2));
    Assert.assertEquals(21 - 3, Sub.apply(21, 3));
    Assert.assertEquals(3 & 2,  And.apply(3, 2));
    Assert.assertEquals(4 | 8,  Or.apply(4, 8));
    Assert.assertEquals(4 ^ 8,  Xor.apply(4, 8));
    Assert.assertEquals(8 ^ 8,  Xor.apply(8, 8));
    Assert.assertEquals(8 / 4,  Div.apply(8, 4));
    Assert.assertEquals(8 % 4,  Mod.apply(8, 4));
    Assert.assertEquals(8 % 5,  Mod.apply(8, 5));
    Assert.assertEquals(8 * 5,  Mul.apply(8, 5));
    Assert.assertEquals(-1 >> 10, Shr.apply(-1, 10));
    Assert.assertEquals(472758 >> 3, Shr.apply(472758, 3));
    Assert.assertEquals(-1 >>> 10, Ushr.apply(-1, 10));
    Assert.assertEquals(472758 >>> 3, Ushr.apply(472758, 3));
    Assert.assertEquals(-1 << 10, Shl.apply(-1, 10));
    Assert.assertEquals(472758 << 3, Shl.apply(472758, 3));
    Assert.assertTrue(Or.applyBool(true, false));
    Assert.assertTrue(Xor.applyBool(true, false));
    Assert.assertFalse(And.applyBool(true, false));

}

}
