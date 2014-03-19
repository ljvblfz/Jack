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

package com.android.jack.unary.test001.dx;

import com.android.jack.unary.test001.jack.Unary;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about unary.
 */
public class Tests {

  @Test
  public void intNeg() {
    Assert.assertEquals(-5,  Unary.neg(5));
  }

  @Test
  public void intBitnot() {
    Assert.assertEquals(-6,  Unary.bitnot(5));
  }

  @Test
  public void longNeg() {
    Assert.assertEquals(-123456789123456L,  Unary.neg(123456789123456L));
  }

  @Test
  public void longBitnot() {
    Assert.assertEquals(-123456789123457L,  Unary.bitnot(123456789123456L));
  }

  @Test
  public void floatNeg() {
    Assert.assertEquals(1.23f,  Unary.neg(-1.23f), 0);
  }

  @Test
  public void doubleNeg() {
    Assert.assertEquals(-1.23e2,  Unary.neg(1.23e2), 0);
  }
}
