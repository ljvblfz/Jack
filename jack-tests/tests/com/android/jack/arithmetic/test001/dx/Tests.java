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

package com.android.jack.arithmetic.test001.dx;

import com.android.jack.arithmetic.test001.jack.Add;
import com.android.jack.arithmetic.test001.jack.And;
import com.android.jack.arithmetic.test001.jack.Div;
import com.android.jack.arithmetic.test001.jack.Mod;
import com.android.jack.arithmetic.test001.jack.Mul;
import com.android.jack.arithmetic.test001.jack.Or;
import com.android.jack.arithmetic.test001.jack.Shl;
import com.android.jack.arithmetic.test001.jack.Shr;
import com.android.jack.arithmetic.test001.jack.Sub;
import com.android.jack.arithmetic.test001.jack.Ushr;
import com.android.jack.arithmetic.test001.jack.Xor;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about arithmetic.
 */
public class Tests {

  @Test
  public void test1() {
    Assert.assertEquals(0,  Add.apply(0, 0));
    Assert.assertEquals(10 + 7, Add.apply(10, 7));
    Assert.assertEquals(21 - 3, Sub.apply(21, 3));
    Assert.assertEquals(-1 & 19, And.apply(-1, 19));
    Assert.assertEquals(21 & 54, And.apply(21, 54));
    Assert.assertEquals(210 / 10, Div.apply(210, 10));
    Assert.assertEquals(119 / 5, Div.apply(119, 5));
    Assert.assertEquals(119 % 5,  Mod.apply(119, 5));
    Assert.assertEquals(6 * 5, Mul.apply(6, 5));
    Assert.assertEquals(30 | 1, Or.apply(30, 1));
    Assert.assertEquals(-1 | 27, Or.apply(-1, 27));
    Assert.assertEquals(-1 >> 10, Shr.apply(-1, 10));
    Assert.assertEquals(472758 >> 3, Shr.apply(472758, 3));
    Assert.assertEquals(-1 >>> 10, Ushr.apply(-1, 10));
    Assert.assertEquals(472758 >>> 3, Ushr.apply(472758, 3));
    Assert.assertEquals(-1 << 10, Shl.apply(-1, 10));
    Assert.assertEquals(472758 << 3, Shl.apply(472758, 3));
    Assert.assertEquals(472758 ^ 758996, Xor.apply(472758, 758996));
}

}
