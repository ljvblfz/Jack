/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.arithmetic.test004.dx;

import junit.framework.Assert;

import org.junit.Test;

import com.android.jack.arithmetic.test004.jack.ArithmeticWithoutValue;

/**
 * Arithmetic with unary operator.
 */
public class Tests {

  @Test
  public void test1() {
    Assert.assertEquals(2, ArithmeticWithoutValue.add1(1, 1));
    Assert.assertEquals(2, ArithmeticWithoutValue.add2(1, 1));
    Assert.assertEquals(0, ArithmeticWithoutValue.add3(1, 1));
  }

}
