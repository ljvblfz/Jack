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

package com.android.jack.annotation.test009.dx;

import com.android.jack.annotation.test009.jack.Annotation009;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {
  @Test
  public void test001() throws Exception {
    Class<?> clazz = Annotation009.getLocalClass1();
    Class<?> ref = Annotation009Ref.getLocalClass1();
    Assert.assertNull(clazz.getEnclosingMethod());
    Assert.assertEquals(clazz.getEnclosingMethod() == null, ref.getEnclosingMethod() == null);
  }

  @Test
  public void test002() throws Exception {
    Class<?> clazz = Annotation009.getLocalClass2();
    Class<?> ref = Annotation009Ref.getLocalClass2();
    Assert.assertEquals(clazz.getEnclosingMethod() == null, ref.getEnclosingMethod() == null);
  }

  @Test
  public void test003() throws Exception {
    Class<?> clazz = Annotation009.getLocalClass3();
    Class<?> ref = Annotation009Ref.getLocalClass3();
    Assert.assertEquals(clazz.getEnclosingMethod() == null, ref.getEnclosingMethod() == null);
  }

  @Test
  public void test004() throws Exception {
    Class<?> clazz = Annotation009.getLocalClass4();
    Class<?> ref = Annotation009Ref.getLocalClass4();
    Assert.assertEquals(clazz.getEnclosingMethod() == null, ref.getEnclosingMethod() == null);
  }

  @Test
  public void test005() throws Exception {
    Class<?> clazz = Annotation009.getLocalClass5();
    Class<?> ref = Annotation009Ref.getLocalClass5();
    Assert.assertEquals(clazz.getEnclosingMethod() == null, ref.getEnclosingMethod() == null);
  }
}
