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

package com.android.jack.conditional.test005.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.conditional.test005.jack.Conditional005;
import com.android.jack.conditional.test005.jack.Conditional005.A;

public class Tests {

  @Test
  public void test001() {
    A[] i = new A[]{new A()};
    Assert.assertEquals(1, Conditional005.test001(i));
    Assert.assertEquals(-1, Conditional005.test001(null));
  }

  @Test
  public void test002() {
    A[][] i = new A[][]{{new A(), new A()}};
    Assert.assertEquals(1, Conditional005.test002(i));
    Assert.assertEquals(-1, Conditional005.test002(null));
  }

  @Test
  public void test003() {
    A[] i = new A[]{};
    Assert.assertEquals(0, Conditional005.test003(i));
    Assert.assertEquals(1, Conditional005.test003(null));
  }

  @Test
  public void test004() {
    int[] i = new int[]{2};
    Assert.assertEquals(3, Conditional005.test004(i));
    Assert.assertEquals(2, Conditional005.test004(null));
  }

  @Test
  public void test005() {
    int[] i = new int[]{2};
    Assert.assertEquals(i, Conditional005.test005(i));
    Assert.assertNotNull(Conditional005.test005(null));
  }
}
