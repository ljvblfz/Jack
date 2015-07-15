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

package com.android.jack.switchstatement.test012.dx;

import com.android.jack.switchstatement.test012.jack.Enum1;
import com.android.jack.switchstatement.test012.jack.Enum2;
import com.android.jack.switchstatement.test012.jack.Switch1;
import com.android.jack.switchstatement.test012.jack.Switch2;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about switches. Running instrument code to see if it is executable.
 */
public class Tests {
  @Test
  public void test1() {
    Assert.assertEquals(1, Switch1.switch1(Enum1.VALUE1, Enum2.VALUE1));
    Assert.assertEquals(1, Switch1.switch1(Enum1.VALUE1, Enum2.VALUE2));
    Assert.assertEquals(1, Switch1.switch1(Enum1.VALUE1, Enum2.VALUE3));
    Assert.assertEquals(1, Switch1.switch1(Enum1.VALUE1, Enum2.VALUE4));
    Assert.assertEquals(1, Switch1.switch1(Enum1.VALUE1, Enum2.VALUE5));


    Assert.assertEquals(0, Switch1.switch1(Enum1.VALUE2, Enum2.VALUE1));
    Assert.assertEquals(2, Switch1.switch1(Enum1.VALUE2, Enum2.VALUE2));
    Assert.assertEquals(0, Switch1.switch1(Enum1.VALUE2, Enum2.VALUE3));
    Assert.assertEquals(4, Switch1.switch1(Enum1.VALUE2, Enum2.VALUE4));
    Assert.assertEquals(0, Switch1.switch1(Enum1.VALUE2, Enum2.VALUE5));


    Assert.assertEquals(3, Switch1.switch1(Enum1.VALUE3, Enum2.VALUE1));
    Assert.assertEquals(3, Switch1.switch1(Enum1.VALUE3, Enum2.VALUE2));
    Assert.assertEquals(3, Switch1.switch1(Enum1.VALUE3, Enum2.VALUE3));
    Assert.assertEquals(3, Switch1.switch1(Enum1.VALUE3, Enum2.VALUE4));
    Assert.assertEquals(3, Switch1.switch1(Enum1.VALUE3, Enum2.VALUE5));


    Assert.assertEquals(0, Switch1.switch1(Enum1.VALUE4, Enum2.VALUE1));
    Assert.assertEquals(2, Switch1.switch1(Enum1.VALUE4, Enum2.VALUE2));
    Assert.assertEquals(0, Switch1.switch1(Enum1.VALUE4, Enum2.VALUE3));
    Assert.assertEquals(4, Switch1.switch1(Enum1.VALUE4, Enum2.VALUE4));
    Assert.assertEquals(0, Switch1.switch1(Enum1.VALUE4, Enum2.VALUE5));


    Assert.assertEquals(5, Switch1.switch1(Enum1.VALUE5, Enum2.VALUE1));
    Assert.assertEquals(5, Switch1.switch1(Enum1.VALUE5, Enum2.VALUE2));
    Assert.assertEquals(5, Switch1.switch1(Enum1.VALUE5, Enum2.VALUE3));
    Assert.assertEquals(5, Switch1.switch1(Enum1.VALUE5, Enum2.VALUE4));
    Assert.assertEquals(5, Switch1.switch1(Enum1.VALUE5, Enum2.VALUE5));
  }

  @Test
  public void test2() {
    Assert.assertEquals(1, Switch2.switch2(Enum1.VALUE1, Enum2.VALUE1));
    Assert.assertEquals(1, Switch2.switch2(Enum1.VALUE1, Enum2.VALUE2));
    Assert.assertEquals(1, Switch2.switch2(Enum1.VALUE1, Enum2.VALUE3));
    Assert.assertEquals(1, Switch2.switch2(Enum1.VALUE1, Enum2.VALUE4));
    Assert.assertEquals(1, Switch2.switch2(Enum1.VALUE1, Enum2.VALUE5));


    Assert.assertEquals(0, Switch2.switch2(Enum1.VALUE2, Enum2.VALUE1));
    Assert.assertEquals(2, Switch2.switch2(Enum1.VALUE2, Enum2.VALUE2));
    Assert.assertEquals(0, Switch2.switch2(Enum1.VALUE2, Enum2.VALUE3));
    Assert.assertEquals(4, Switch2.switch2(Enum1.VALUE2, Enum2.VALUE4));
    Assert.assertEquals(0, Switch2.switch2(Enum1.VALUE2, Enum2.VALUE5));


    Assert.assertEquals(3, Switch2.switch2(Enum1.VALUE3, Enum2.VALUE1));
    Assert.assertEquals(3, Switch2.switch2(Enum1.VALUE3, Enum2.VALUE2));
    Assert.assertEquals(3, Switch2.switch2(Enum1.VALUE3, Enum2.VALUE3));
    Assert.assertEquals(3, Switch2.switch2(Enum1.VALUE3, Enum2.VALUE4));
    Assert.assertEquals(3, Switch2.switch2(Enum1.VALUE3, Enum2.VALUE5));


    Assert.assertEquals(0, Switch2.switch2(Enum1.VALUE4, Enum2.VALUE1));
    Assert.assertEquals(2, Switch2.switch2(Enum1.VALUE4, Enum2.VALUE2));
    Assert.assertEquals(0, Switch2.switch2(Enum1.VALUE4, Enum2.VALUE3));
    Assert.assertEquals(4, Switch2.switch2(Enum1.VALUE4, Enum2.VALUE4));
    Assert.assertEquals(0, Switch2.switch2(Enum1.VALUE4, Enum2.VALUE5));


    Assert.assertEquals(5, Switch2.switch2(Enum1.VALUE5, Enum2.VALUE1));
    Assert.assertEquals(5, Switch2.switch2(Enum1.VALUE5, Enum2.VALUE2));
    Assert.assertEquals(5, Switch2.switch2(Enum1.VALUE5, Enum2.VALUE3));
    Assert.assertEquals(5, Switch2.switch2(Enum1.VALUE5, Enum2.VALUE4));
    Assert.assertEquals(5, Switch2.switch2(Enum1.VALUE5, Enum2.VALUE5));
  }
}
