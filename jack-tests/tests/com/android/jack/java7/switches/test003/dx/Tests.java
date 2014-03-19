/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.java7.switches.test003.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.java7.switches.test003.jack.SwitchTest;


/**
 * Test switches with strings.
 */
public class Tests {

  @Test
  public void test001() {
    Assert.assertEquals(1, SwitchTest.switch001("Aa"));
    Assert.assertEquals(3, SwitchTest.switch001("BB"));
    Assert.assertEquals(5, SwitchTest.switch001("d"));
  }
}