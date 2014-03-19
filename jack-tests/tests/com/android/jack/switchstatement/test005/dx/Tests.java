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

package com.android.jack.switchstatement.test005.dx;

import com.android.jack.switchstatement.test005.jack.Switch;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about switches.
 */
public class Tests {

  @Test
  public void test1() {
    Assert.assertEquals(1, Switch.switch001(1));
    Assert.assertEquals(2, Switch.switch001(2));
    Assert.assertEquals(3, Switch.switch001(3));
  }

  @Test
  public void test2() {
    Assert.assertEquals(1, Switch.switch002(1));
    Assert.assertEquals(2, Switch.switch002(2));
    Assert.assertEquals(3, Switch.switch002(3));
  }

  @Test
  public void test3() {
    Assert.assertEquals(1, Switch.switch003(1));
    Assert.assertEquals(2, Switch.switch003(2));
    Assert.assertEquals(3, Switch.switch003(3));
  }

  @Test
  public void test4() {
    Assert.assertEquals(1, Switch.switch004(1));
    Assert.assertEquals(2, Switch.switch004(2));
    Assert.assertEquals(3, Switch.switch004(3));
  }
}
