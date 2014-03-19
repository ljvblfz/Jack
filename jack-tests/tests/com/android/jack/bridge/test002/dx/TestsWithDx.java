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

package com.android.jack.bridge.test002.dx;

import com.android.jack.bridge.test002.jack.A;
import com.android.jack.bridge.test002.jack.B;
import com.android.jack.bridge.test002.jack.C;
import com.android.jack.bridge.test002.jack.Generic;

import org.junit.Assert;
import org.junit.Test;

public class TestsWithDx {
    private A a = new A();
    private Object a2 = new A();
    private Object o = new Object();
    private B b = new B();
    private Generic b2 = new B();
    private C c = new C();
    private Generic c2 = new C();
    private Generic<Object> go = new Generic<Object>();
    private Generic gb = new Generic<B>();

  @Test
  public void test1() {
    b.setT(a);
    Assert.assertEquals(a,  b.getT());
    b2.setT(a2);
    Assert.assertEquals(a2,  b2.getT());
    c.setT(a);
    Assert.assertEquals(a,  c.getT());
    c2.setT(a2);
    Assert.assertEquals(a2,  c2.getT());
    go.setT(a);
    Assert.assertEquals(a,  go.getT());
    go.setT(o);
    Assert.assertEquals(o,  go.getT());
    gb.setT(b);
    Assert.assertEquals(b,  gb.getT());
    gb.setT(o);
    Assert.assertEquals(o,  gb.getT());
  }

  @Test
  public void test2() {
    b2.setT(o);
    Assert.assertEquals(o,  b2.getT());
  }

  @Test
  public void test3() {
    try {
      c2.setT(o);
      Assert.fail();
    } catch (ClassCastException e) {
      // expected
    }
  }
}
