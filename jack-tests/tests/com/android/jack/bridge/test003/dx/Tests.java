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

package com.android.jack.bridge.test003.dx;

import com.android.jack.bridge.test003.jack.Data;
import com.android.jack.bridge.test003.jack.Data.B;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Tests {
  @Test
  public void test001() throws Exception {
    Data data = new Data();
    Method method = data.getMethod1();
    Assert.assertEquals(B.class, method.getDeclaringClass());
    Assert.assertTrue(method.isSynthetic());
    Assert.assertTrue(method.isBridge());
    data.getMethod2();

    Data.B b = new B();
    b.unchanged();
    b.unchangedFinal();
  }

  @Test
  public void test003() throws Exception {
    Data data = new Data();
    Method method = data.getMethod3();
    Assert.assertEquals(B.class, method.getDeclaringClass());
    Assert.assertTrue(method.isSynthetic());
    Assert.assertTrue(method.isBridge());
    Assert.assertFalse(Modifier.isSynchronized(method.getModifiers()));
    Data.B b = new B();
    b.unchangedSynchronized();
  }
}
