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

package com.android.jack.newarray.test004.dx;

import com.android.jack.newarray.test004.jack.Data;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void testByte() {
    Assert.assertEquals(1, Data.getByteArray()[0]);
    Assert.assertEquals(-1, Data.getByteArray1()[1]);
  }

  @Test
  public void testChar() {
    Assert.assertEquals(166, Data.getCharArray()[0]);
  }

  @Test
  public void testShort() {
    Assert.assertEquals(11566, Data.getShortArray()[0]);
  }

  @Test
  public void testInt() {
    Assert.assertEquals(11566, Data.getIntArray()[0]);
  }

  @Test
  public void testFloat() {
    Assert.assertEquals(1.2f, Data.getFloatArray()[0], 0.0);
  }

  @Test
  public void testDouble() {
    Assert.assertEquals(1.2f, Data.getDoubleArray()[0], 0.0);
  }
}
