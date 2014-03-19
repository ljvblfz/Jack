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

package com.android.jack.assertion.test002.dx;

import com.android.jack.assertion.test002.jack.Data;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class Tests {

  @BeforeClass
  public static void setUpClass() {
    Data.class.getClassLoader().setDefaultAssertionStatus(false);
  }

  @Test
  public void test() {
    try {
      Data.throwAssert();
    } catch (AssertionError e){
      Assert.fail();
    }
  }
}
