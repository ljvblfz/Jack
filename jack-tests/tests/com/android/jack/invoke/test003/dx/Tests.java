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

package com.android.jack.invoke.test003.dx;

import com.android.jack.invoke.test003.jack.C;
import com.android.jack.invoke.test003.jack.Data;
import com.android.jack.invoke.test003.jack.sub.B;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void test001() {
    B b = new B();
    Assert.assertEquals(1, Data.get(b));
    C c = new C();
    Assert.assertEquals(42, c.get());
    Assert.assertEquals(1, c.getSuper());
  }
}
