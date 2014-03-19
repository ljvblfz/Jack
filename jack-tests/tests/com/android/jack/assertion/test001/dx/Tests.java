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

package com.android.jack.assertion.test001.dx;

import com.android.jack.assertion.test001.jack.Data;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class Tests {

  @BeforeClass
  public static void setUpClass() {
    Data.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void test1() {
    try {
      Data.throwAssert();
    } catch (AssertionError e){
      // expected
      Assert.assertNull(e.getMessage());
      return;
    }
    Assert.fail();
  }

  @Test
  public void test2() {
    try {
      Data.throwAssert("assertion", 0);
    } catch (AssertionError e){
      Assert.fail();
    }
  }

  @Test
  public void test3() {
    try {
      Data.throwAssert("assertion", 1);
    } catch (AssertionError e){
      // expected
      Assert.assertEquals("assertion", e.getMessage());
      return;
    }
    Assert.fail();
  }

  @Test
  public void test4() {
    Data.throwAssert(true);
    try {
      Data.throwAssert(false);
    } catch (AssertionError e){
      // expected
      Assert.assertEquals("boolean is false", e.getMessage());
      return;
    }
    Assert.fail();
  }

  @Test
  public void test5() {
    try {
      Data.throwAssertObject(new Object());
    } catch (AssertionError e){
      // expected
      Assert.assertNotNull(e.getMessage());
      return;
    }
    Assert.fail();
  }
}
