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

package com.android.jack.java8.lambda.test039.jack;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  private int field = 5;

  public int testAdd(int a) {
    I i = () -> a + field;
    return i.addOutsideValue();
  }

  @Test
  public void test001() {
    Tests l = new Tests();
    Assert.assertEquals(8, l.testAdd(3));
    Assert.assertEquals(10, l.testAdd(5));
  }
}
