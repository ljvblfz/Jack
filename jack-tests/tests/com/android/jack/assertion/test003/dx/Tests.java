/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.assertion.test003.dx;

import com.android.jack.assertion.test003.jack.Data;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests what happens when there are 2 assertion statements in the same method.
 * In the current implementation of Jack, an "assertionsEnabled" field is created in the enclosing
 * type when an assertion statement is encountered, but it should be reused for the following one.
 */
public class Tests {

  @BeforeClass
  public static void setUpClass() {
    Data.class.getClassLoader().setDefaultAssertionStatus(false);
  }

  @Test
  public void test1() {
    try {
      Data.m(true, true);
    } catch (AssertionError e){
      Assert.fail();
    }
  }

  @Test
  public void test2() {
    try {
      Data.m(true, false);
      Assert.fail();
    } catch (AssertionError e){
    }
  }

  @Test
  public void test3() {
    try {
      Data.m(false, true);
      Assert.fail();
    } catch (AssertionError e){
    }
  }

  @Test
  public void test4() {
    try {
      Data.m(false, false);
      Assert.fail();
    } catch (AssertionError e){
    }
  }
}
