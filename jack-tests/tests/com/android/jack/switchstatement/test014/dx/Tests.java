/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.switchstatement.test014.dx;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.android.jack.switchstatement.test014.jack.Switch1;
import com.android.jack.switchstatement.test014.jack.Switch2;

/**
 * Tests about switches. Running instrument code to see if it is executable.
 */
public class Tests {
  private Object e1v1;
  private Object e1v2;
  private Object e1v3;
  private Object e1v4;
  private Object e1v5;

  private Object e2v1;
  private Object e2v2;
  private Object e2v3;
  private Object e2v4;
  private Object e2v5;

  @Before
  public void init() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
    {
      Class<? extends Object> enum1Class = Class.forName("com.android.jack.switchstatement.test014.jack.Enum1$Enum1_");
      Assert.assertTrue(enum1Class.isEnum());
      Assert.assertEquals(5, enum1Class.getFields().length);
      Field enum1Value1 = enum1Class.getField("VALUE1");
      Field enum1Value2 = enum1Class.getField("VALUE2");
      Field enum1Value3 = enum1Class.getField("VALUE3");
      Field enum1Value4 = enum1Class.getField("VALUE4");
      Field enum1Value5 = enum1Class.getField("VALUE5");
      e1v1 = enum1Value1.get(null);
      e1v2 = enum1Value2.get(null);
      e1v3 = enum1Value3.get(null);
      e1v4 = enum1Value4.get(null);
      e1v5 = enum1Value5.get(null);
    }
    {
      Class<? extends Object> enum2Class = Class.forName("com.android.jack.switchstatement.test014.jack.Enum2$Enum2_");
      Assert.assertTrue(enum2Class.isEnum());
      Assert.assertEquals(5, enum2Class.getFields().length);
      Field enum2Value1 = enum2Class.getField("VALUE1");
      Field enum2Value2 = enum2Class.getField("VALUE2");
      Field enum2Value3 = enum2Class.getField("VALUE3");
      Field enum2Value4 = enum2Class.getField("VALUE4");
      Field enum2Value5 = enum2Class.getField("VALUE5");

      e2v1 = enum2Value1.get(null);
      e2v2 = enum2Value2.get(null);
      e2v3 = enum2Value3.get(null);
      e2v4 = enum2Value4.get(null);
      e2v5 = enum2Value5.get(null);
    }
  }

  @Test
  public void test1() {

    Assert.assertEquals(1, Switch1.switch1(e1v1, e2v1));
    Assert.assertEquals(1, Switch1.switch1(e1v1, e2v2));
    Assert.assertEquals(1, Switch1.switch1(e1v1, e2v3));
    Assert.assertEquals(1, Switch1.switch1(e1v1, e2v4));
    Assert.assertEquals(1, Switch1.switch1(e1v1, e2v5));


    Assert.assertEquals(0, Switch1.switch1(e1v2, e2v1));
    Assert.assertEquals(2, Switch1.switch1(e1v2, e2v2));
    Assert.assertEquals(0, Switch1.switch1(e1v2, e2v3));
    Assert.assertEquals(4, Switch1.switch1(e1v2, e2v4));
    Assert.assertEquals(0, Switch1.switch1(e1v2, e2v5));


    Assert.assertEquals(3, Switch1.switch1(e1v3, e2v1));
    Assert.assertEquals(3, Switch1.switch1(e1v3, e2v2));
    Assert.assertEquals(3, Switch1.switch1(e1v3, e2v3));
    Assert.assertEquals(3, Switch1.switch1(e1v3, e2v4));
    Assert.assertEquals(3, Switch1.switch1(e1v3, e2v5));


    Assert.assertEquals(0, Switch1.switch1(e1v4, e2v1));
    Assert.assertEquals(2, Switch1.switch1(e1v4, e2v2));
    Assert.assertEquals(0, Switch1.switch1(e1v4, e2v3));
    Assert.assertEquals(4, Switch1.switch1(e1v4, e2v4));
    Assert.assertEquals(0, Switch1.switch1(e1v4, e2v5));


    Assert.assertEquals(5, Switch1.switch1(e1v5, e2v1));
    Assert.assertEquals(5, Switch1.switch1(e1v5, e2v2));
    Assert.assertEquals(5, Switch1.switch1(e1v5, e2v3));
    Assert.assertEquals(5, Switch1.switch1(e1v5, e2v4));
    Assert.assertEquals(5, Switch1.switch1(e1v5, e2v5));
  }

  @Test
  public void test2() {
    Assert.assertEquals(1, Switch2.switch2(e1v1, e2v1));
    Assert.assertEquals(1, Switch2.switch2(e1v1, e2v2));
    Assert.assertEquals(1, Switch2.switch2(e1v1, e2v3));
    Assert.assertEquals(1, Switch2.switch2(e1v1, e2v4));
    Assert.assertEquals(1, Switch2.switch2(e1v1, e2v5));


    Assert.assertEquals(0, Switch2.switch2(e1v2, e2v1));
    Assert.assertEquals(2, Switch2.switch2(e1v2, e2v2));
    Assert.assertEquals(0, Switch2.switch2(e1v2, e2v3));
    Assert.assertEquals(4, Switch2.switch2(e1v2, e2v4));
    Assert.assertEquals(0, Switch2.switch2(e1v2, e2v5));


    Assert.assertEquals(3, Switch2.switch2(e1v3, e2v1));
    Assert.assertEquals(3, Switch2.switch2(e1v3, e2v2));
    Assert.assertEquals(3, Switch2.switch2(e1v3, e2v3));
    Assert.assertEquals(3, Switch2.switch2(e1v3, e2v4));
    Assert.assertEquals(3, Switch2.switch2(e1v3, e2v5));


    Assert.assertEquals(0, Switch2.switch2(e1v4, e2v1));
    Assert.assertEquals(2, Switch2.switch2(e1v4, e2v2));
    Assert.assertEquals(0, Switch2.switch2(e1v4, e2v3));
    Assert.assertEquals(4, Switch2.switch2(e1v4, e2v4));
    Assert.assertEquals(0, Switch2.switch2(e1v4, e2v5));


    Assert.assertEquals(5, Switch2.switch2(e1v5, e2v1));
    Assert.assertEquals(5, Switch2.switch2(e1v5, e2v2));
    Assert.assertEquals(5, Switch2.switch2(e1v5, e2v3));
    Assert.assertEquals(5, Switch2.switch2(e1v5, e2v4));
    Assert.assertEquals(5, Switch2.switch2(e1v5, e2v5));
  }
}
