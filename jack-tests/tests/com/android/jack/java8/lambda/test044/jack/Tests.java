/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.java8.lambda.test044.jack;

import com.android.jack.java8.lambda.test044.lib1.B1;

import junit.framework.Assert;

import org.junit.Test;


public class Tests {

  @Test
  public void test001() {
    Assert.assertEquals(1, new B1().m1().getCst());
  }

  @Test
  public void test002() {
    Assert.assertEquals(20, new B2().new C().test());
  }

  @Test
  public void test003() {
    Assert.assertEquals(3, new B1().m3().getCst());
  }

  @Test
  public void test004() {
    Assert.assertEquals(30, new B3().new C().test());
  }

  @Test
  public void test005() {
    Assert.assertEquals(4, new B1().m4().getCst());
  }

  @Test
  public void test006() {
    Assert.assertEquals(5, new B1().m5().getCst());
  }
}
