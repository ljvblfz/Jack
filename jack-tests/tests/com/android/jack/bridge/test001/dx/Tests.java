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

package com.android.jack.bridge.test001.dx;

import com.android.jack.bridge.test001.jack.A;
import com.android.jack.bridge.test001.jack.B;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  @Test
  public void test1() {
    A a = new A();
    B b = new B();
    A b2 = new B();
    Assert.assertEquals(a,  a.get());
    Assert.assertEquals(b,  b.get());
    Assert.assertEquals(b2,  b2.get());
  }
}
