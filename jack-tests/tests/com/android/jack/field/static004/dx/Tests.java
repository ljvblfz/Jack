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

package com.android.jack.field.static004.dx;

import com.android.jack.field.static004.jack.Data1;
import com.android.jack.field.static004.jack.Data2;
import com.android.jack.field.static004.jack.Data3;
import com.android.jack.field.static004.jack.Data4;
import com.android.jack.field.static004.jack.Data5;
import com.android.jack.field.static004.jack.Data6;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void test1() {
    Assert.assertEquals(1,  Data1.I1);
    Assert.assertEquals(2,  Data1.I2);
    Assert.assertEquals(3,  Data1.getI3());
    Assert.assertEquals(4,  Data1.getI4());
    Assert.assertEquals(4 + 9,  Data1.getI5());
    Assert.assertEquals(6,  Data1.I6);
    Assert.assertEquals(7,  Data1.I7, 0.0);
    Assert.assertEquals(8.8,  Data1.I8, 0.0);
    Assert.assertEquals(null,  Data1.I9);
  }

  @Test
  public void test2() {
    Assert.assertEquals("I",  Data2.I1);
  }

  @Test
  public void test3() {
    Assert.assertEquals(1,  Data3.I1);
  }

  @Test
  public void test4() {
    Assert.assertEquals(1,  Data4.I1);
    Assert.assertEquals(2,  Data4.I2);
    Assert.assertEquals('r',  Data4.getI4());
    Assert.assertEquals(4 + 9,  Data4.I5);
    Assert.assertEquals(6l,  Data1.I6);
    Assert.assertEquals(7f,  Data1.I7, 0.0);
    Assert.assertEquals(8.8,  Data1.I8, 0.0);
    Assert.assertEquals(null,  Data1.I9);
  }

  @Test
  public void test5() {
    Assert.assertEquals(Integer.valueOf(1),  Data5.I1);
    Assert.assertEquals(Byte.valueOf((byte) 127),  Data5.B1);
    Assert.assertEquals(Short.valueOf((short) 256),  Data5.S1);
    Assert.assertEquals(Character.valueOf('c'),  Data5.C1);
    Assert.assertEquals(Float.valueOf(1.0f),  Data5.F1);
    Assert.assertEquals(Double.valueOf(2.0),  Data5.D1);
    Assert.assertEquals(Long.valueOf(1),  Data5.L1);
  }

  @Test
  public void test6() {
    Assert.assertEquals(Data6.class,  Data6.I1);
  }
}
