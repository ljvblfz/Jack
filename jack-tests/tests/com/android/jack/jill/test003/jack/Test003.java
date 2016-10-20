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
    test(new External1());
  }

  @Test
  public void test002() {
    test(new External2());
  }

  @Test
  public void test003() {
    test(new External3());
  }

  @Test
  public void test004() {
    test(new External4());
  }

  private void test(GreaterThan gt) {
    Assert.assertEquals(1.0 > 2.0, gt.greaterThan(1.0, 2.0));
    Assert.assertEquals(2.0 > 1.0, gt.greaterThan(2.0, 1.0));
    Assert.assertEquals(2.0 > 2.0, gt.greaterThan(2.0, 2.0));
    Assert.assertEquals(2.0 > Double.NaN, gt.greaterThan(2.0, Double.NaN));
    Assert.assertEquals(Double.NaN > Double.NaN, gt.greaterThan(Double.NaN, Double.NaN));
    Assert.assertEquals(Double.NaN > 2.0, gt.greaterThan(Double.NaN, 2.0));
  }
}