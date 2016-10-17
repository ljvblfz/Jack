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

package com.android.jack.jill.test003.jack;

import junit.framework.Assert;

import org.junit.Test;

public class Test003 {

  @Test
  public void test001() {
    Assert.assertEquals(1.0 > 2.0, External.greaterThan1(1.0, 2.0));
    Assert.assertEquals(2.0 > 1.0, External.greaterThan1(2.0, 1.0));
    Assert.assertEquals(2.0 > 2.0, External.greaterThan1(2.0, 2.0));
    Assert.assertEquals(2.0 > Double.NaN, External.greaterThan1(2.0, Double.NaN));
    Assert.assertEquals(Double.NaN > Double.NaN, External.greaterThan1(Double.NaN, Double.NaN));
    Assert.assertEquals(Double.NaN > 2.0, External.greaterThan1(Double.NaN, 2.0));
  }

  @Test
  public void test002() {
    Assert.assertEquals(1.0 > 2.0, External.greaterThan2(1.0, 2.0));
    Assert.assertEquals(2.0 > 1.0, External.greaterThan2(2.0, 1.0));
    Assert.assertEquals(2.0 > 2.0, External.greaterThan2(2.0, 2.0));
    Assert.assertEquals(2.0 > Double.NaN, External.greaterThan2(2.0, Double.NaN));
    Assert.assertEquals(Double.NaN > Double.NaN, External.greaterThan2(Double.NaN, Double.NaN));
    Assert.assertEquals(Double.NaN > 2.0, External.greaterThan2(Double.NaN, 2.0));
  }

  @Test
  public void test003() {
    Assert.assertEquals(1.0 > 2.0, External.greaterThan3(1.0, 2.0));
    Assert.assertEquals(2.0 > 1.0, External.greaterThan3(2.0, 1.0));
    Assert.assertEquals(2.0 > 2.0, External.greaterThan3(2.0, 2.0));
    Assert.assertEquals(2.0 > Double.NaN, External.greaterThan3(2.0, Double.NaN));
    Assert.assertEquals(Double.NaN > Double.NaN, External.greaterThan3(Double.NaN, Double.NaN));
    Assert.assertEquals(Double.NaN > 2.0, External.greaterThan3(Double.NaN, 2.0));
  }
}