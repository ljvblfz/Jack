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

package com.android.jack.tryfinally.finally002.dx;

import com.android.jack.tryfinally.finally002.jack.Data;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {
  @Test
  public void test001() {
    Assert.assertEquals(8, Data.get001(1));
  }

  @Test
  public void test002() {
    Assert.assertEquals(10, Data.get001(2));
  }

  @Test
  public void test003() {
    Assert.assertEquals(14, Data.get001(3));
  }

  @Test
  public void test004() {
    Assert.assertEquals(3, Data.get001(4));
  }

  @Test
  public void test005() {
    Assert.assertEquals(66, Data.get002(-1));
  }

  @Test
  public void test006() {
    Assert.assertEquals(32, Data.get002(1));
  }

  @Test
  public void test007() {
    Assert.assertEquals(37, Data.get003(-1));
  }

  @Test
  public void test008() {
    Assert.assertEquals(33, Data.get003(1));
  }

  @Test
  public void test009() {
    try {
      Data.get004();
      Assert.fail();
    } catch (NullPointerException e) {
      // OK
    }
  }

  @Test
  public void test010() {
    Assert.assertEquals(3, Data.get005());
  }

  @Test
  public void test011() {
    try {
      Assert.assertEquals(3, Data.get006());
      Assert.fail();
    } catch (NullPointerException e) {
      //Ok
    }
  }

  @Test
  public void test012() {
    Assert.assertEquals(1, Data.get007());
    Assert.assertEquals(0, Data.field007);
  }
}
