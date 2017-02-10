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

package com.android.jack.jill.test005.jack;

import junit.framework.Assert;

import org.junit.Test;

public class Test005 {

  @Test
  public void test001() {
    Assert.assertEquals(1, new External1().getValueInt1());
    Assert.assertEquals(2, new External1().getValueInt2());
    Assert.assertEquals(3, new External1().getValueInt3());
    Assert.assertEquals(4, new External1().getValueInt4());
    Assert.assertEquals(5, new External1().getValueInt5());
    Assert.assertEquals(6, new External1().getValueInt6());
    Assert.assertEquals(-2, new External1().getValueIntMinus2());
    Assert.assertEquals(2L, new External1().getValueLong2());
    Assert.assertEquals(1L, new External1().getValueLong1());
    Assert.assertEquals(1L, new External1().getValueLong1Bis());
    Assert.assertEquals(2L, new External1().getValueLong2());
  }
}