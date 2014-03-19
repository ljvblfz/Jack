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

package com.android.jack.newarray.test005.dx;

import junit.framework.Assert;

import org.junit.Test;

import com.android.jack.newarray.test005.jack.Data;

public class Tests {

  @Test
  public void test1() {
    Assert.assertEquals(0, Data.getArray1()[0][0]);
  }

  @Test
  public void test2() {
    Assert.assertEquals(1, Data.getArray2()[0]);
    Assert.assertEquals(3, Data.getArray2()[2]);
    Assert.assertEquals(5, Data.getArray2()[4]);
  }

  @Test
  public void test3() {
    Assert.assertEquals(1, Data.getArray3()[0]);
    Assert.assertEquals(3, Data.getArray3()[2]);
    Assert.assertEquals(6, Data.getArray3()[5]);
  }

  @Test
  public void test4() {
    Assert.assertEquals(-1, Data.getArray4()[0]);
    Assert.assertEquals(2, Data.getArray4()[1]);
  }

  @Test
  public void test5() {
    Assert.assertEquals(10, Data.getArray5()[0][1][2][3]);
  }

  @Test
  public void test6() {
    Assert.assertEquals(null, Data.getArray6()[0]);
  }
}
