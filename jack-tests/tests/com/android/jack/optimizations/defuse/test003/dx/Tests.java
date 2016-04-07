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

package com.android.jack.optimizations.defuse.test003.dx;

import com.android.jack.optimizations.defuse.test003.jack.DefUse003;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  @Test
  public void test001() {
    Assert.assertEquals(1, DefUse003.get(true, true, true));
    Assert.assertEquals(3, DefUse003.get(true, true, false));
    Assert.assertEquals(1, DefUse003.get(true, false, true));
    Assert.assertEquals(4, DefUse003.get(true, false, false));
    Assert.assertEquals(2, DefUse003.get(false, true, true));
    Assert.assertEquals(3, DefUse003.get(false, true, false));
    Assert.assertEquals(2, DefUse003.get(false, false, true));
    Assert.assertEquals(4, DefUse003.get(false, false, false));
  }
}
