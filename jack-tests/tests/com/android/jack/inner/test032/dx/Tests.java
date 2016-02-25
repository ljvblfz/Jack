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

package com.android.jack.inner.test032.dx;

import com.android.jack.inner.test032.jack.ReflectTestOuter;

import junit.framework.Assert;

import java.util.Arrays;

public class Tests {
  @org.junit.Test
  public void test001() throws Exception {
    Assert.assertEquals(1, new ReflectTestOuter().a.get());
  }

  @org.junit.Test
  public void test002() throws Exception {
    Class<?> classA = new ReflectTestOuter().classA;
    Assert.assertEquals(ReflectTestOuter.class, classA.getEnclosingClass());
    Assert.assertNull(classA.getEnclosingMethod());
    Assert.assertNull(classA.getEnclosingConstructor());
    Assert.assertFalse(Arrays.asList(ReflectTestOuter.class.getDeclaredClasses()).contains(classA));
  }
}
