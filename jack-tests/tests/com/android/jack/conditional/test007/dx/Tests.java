/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.conditional.test007.dx;

import com.android.jack.conditional.test007.jack.ConditionalTest007;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  @Test
  public void test001() {
    Assert.assertEquals((short) 1, ConditionalTest007.test001(1, true));
    Assert.assertEquals((short) 2, ConditionalTest007.test001(1, false));

    Assert.assertEquals((short) 1, ConditionalTest007.test001(2, true));
    Assert.assertEquals((short) 2, ConditionalTest007.test001(2, false));

    Assert.assertEquals((short) 1, ConditionalTest007.test001(3, true));
    Assert.assertEquals((short) 2, ConditionalTest007.test001(3, false));

    Assert.assertEquals((short) 1, ConditionalTest007.test001(4, true));
    Assert.assertEquals((short) 2, ConditionalTest007.test001(4, false));
  }

  public void test002() {
    Assert.assertEquals((byte) 1, ConditionalTest007.test002a(true));
    Assert.assertEquals((byte) 4, ConditionalTest007.test002a(false));

    Assert.assertEquals((short) 1, ConditionalTest007.test002b(true));
    Assert.assertEquals((short) 4, ConditionalTest007.test002b(false));

    Assert.assertEquals('a', ConditionalTest007.test002c(true));
    Assert.assertEquals('a', ConditionalTest007.test002c(false));
  }

  public void test003() {
    Assert.assertEquals((byte) 1, ConditionalTest007.test003a(true));
    Assert.assertEquals((byte) 4, ConditionalTest007.test003a(false));

    Assert.assertEquals((short) 1, ConditionalTest007.test003b(true));
    Assert.assertEquals((short) 4, ConditionalTest007.test003b(false));

    Assert.assertEquals('a', ConditionalTest007.test003c(true));
    Assert.assertEquals('a', ConditionalTest007.test003c(false));
  }


}
