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

package com.android.jack.jill.test001.jack;

import junit.framework.Assert;

import org.junit.Test;

public class Test001 {

  @Test
  public void test001() {
    Assert.assertEquals(10, new External().sub1(20, 10));
  }

  @Test
  public void test002() {
    Assert.assertEquals(10, new External().sub2(20, 10));
  }

  @Test
  public void test003() {
    Assert.assertEquals(10, new External().sub3(20, 10));
  }

  @Test
  public void test004() {
    Assert.assertEquals(10, new External().sub4(20, 10));
  }

  @Test
  public void test005() {
    Assert.assertEquals(10, new External().sub5(20, 10));
  }

  @Test
  public void test006() {
    Assert.assertEquals(10, new External().sub6(20, 10));
  }

  @Test
  public void test007() {
    Assert.assertEquals(10, new External().sub7(20, 10));
  }

  @Test
  public void test008() {
    Assert.assertEquals(10, new External().sub8(20, 10));
  }

  @Test
  public void test009() {
    Assert.assertEquals(10, new External().sub9(20, 10));
  }

  @Test
  public void test010() {
    Assert.assertEquals(10, new External().sub10(20, 10));
  }

  @Test
  public void test011() {
    Assert.assertEquals(10, new External().sub11(20, 10));
  }

  @Test
  public void test012() {
    Assert.assertEquals(10, new External().sub12(20, 10));
  }

  @Test
  public void test013() {
    Assert.assertEquals(10, new External().sub13(20, 10));
  }
}