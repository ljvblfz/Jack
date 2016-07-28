/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.switchstatement.test028.dx;

import com.android.jack.switchstatement.test028.jack.RedundantSwitch;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {
  @Test
  public void test001() {
    Assert.assertEquals(10, (new RedundantSwitch()).switch001(100));
    Assert.assertEquals(10, (new RedundantSwitch()).switch001(101));
  }

  @Test
  public void test002() {
    Assert.assertEquals(10, (new RedundantSwitch()).switch002(100));
    Assert.assertEquals(10, (new RedundantSwitch()).switch002(101));
  }

  @Test
  public void test003() {
    Assert.assertEquals(10, (new RedundantSwitch()).switch003(100));
    Assert.assertEquals(10, (new RedundantSwitch()).switch003(101));
  }

  @Test
  public void test004() {
    Assert.assertEquals(10, (new RedundantSwitch()).switch004(100));
    Assert.assertEquals(10, (new RedundantSwitch()).switch004(101));
    Assert.assertEquals(4, (new RedundantSwitch()).switch004(4));
  }
}
