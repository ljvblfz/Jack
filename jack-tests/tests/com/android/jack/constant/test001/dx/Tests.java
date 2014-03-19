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

package com.android.jack.constant.test001.dx;

import com.android.jack.constant.test001.jack.Constant;

import org.junit.Assert;
import org.junit.Test;

public class Tests {
  @Test
  public void constantBooleanTrue() {
    Assert.assertTrue(Constant.getBooleanTrue());
  }

  @Test
  public void constantBooleanFalse() {
    Assert.assertFalse(Constant.getBooleanFalse());
  }

  @Test
  public void constantChar() {
    Assert.assertEquals('d', Constant.getChar());
  }

  @Test
  public void constantDouble() {
    Assert.assertEquals(12.3, Constant.getDouble(), 0);
  }

  @Test
  public void constantFloat() {
    Assert.assertEquals(23.4f, Constant.getFloat(), 0);
  }

  @Test
  public void constantInt() {
    Assert.assertEquals(1337, Constant.getInt());
  }

  @Test
  public void constantLong() {
    Assert.assertEquals(345l, Constant.getLong());
  }

  @Test
  public void constantByte() {
    Assert.assertEquals(-1, Constant.getByte());
  }

  @Test
  public void constantShort() {
    Assert.assertEquals(456, Constant.getShort());
  }

  @Test
  public void constantString() {
    Assert.assertEquals("abc", Constant.getString());
  }

  @Test
  public void constantNull() {
    Assert.assertNull(Constant.getNull());
  }

  public void constantObjectClass() {
    Class<?> cl = Constant.getObjectClass();
    Assert.assertNotNull(cl);
    Assert.assertEquals("java.lang.Object", cl.getName());
  }

  @Test
  public void constantClass() {
    Class<?> cl = Constant.getConstantClass();
    Assert.assertNotNull(cl);
    Assert.assertEquals("com.android.jack.constant.test001.jack.Constant", cl.getName());
  }

  @Test
  public void constantIntClass() {
    Class<?> cl = Constant.getIntClass();
    Assert.assertNotNull(cl);
    Assert.assertEquals("int", cl.getName());
  }

  @Test
  public void constantArrayClass() {
    Class<?> cl = Constant.getArrayClass();
    Assert.assertNotNull(cl);
    Assert.assertEquals("[I", cl.getName());
  }
}
