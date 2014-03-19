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

package com.android.jack.switchstatement.test003.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.switchstatement.test003.jack.Switches003;

/**
 * Tests about switches.
 */
public class Tests {

  @Test
  public void test2() {
    Switches003 s = new Switches003();
    try {
      s.getValue(null);
      Assert.fail();
    } catch (NullPointerException npe) {
      // OK
    }
  }

  @Test
  public void test3() {
    Assert.assertEquals(0, Switches003.getValue2(2));
    Assert.assertEquals(Integer.MIN_VALUE, Switches003.getValue2(Integer.MIN_VALUE));
  }
}
