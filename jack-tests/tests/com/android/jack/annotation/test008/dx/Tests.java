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

package com.android.jack.annotation.test008.dx;

import com.android.jack.annotation.test008.jack.Annotation008;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Modifier;

public class Tests {
  @Test
  public void testref001() throws Exception {
    Class<?> anonymousClass = Ref008.getAnonymousClass();
    Assert.assertTrue(anonymousClass.isAnonymousClass());
    Assert.assertFalse(Modifier.isFinal(anonymousClass.getModifiers()));
  }
  @Test
  public void test001() throws Exception {
    Class<?> anonymousClass = Annotation008.getAnonymousClass1();
    Assert.assertTrue(anonymousClass.isAnonymousClass());
    Assert.assertFalse(Modifier.isFinal(anonymousClass.getModifiers()));
  }
  @Test
  public void testref002() throws Exception {
    Class<?> anonymousClass = Ref008.anonymousClass2.getClass();
    Assert.assertTrue(anonymousClass.isAnonymousClass());
    Assert.assertFalse(Modifier.isFinal(anonymousClass.getModifiers()));
  }
  @Test
  public void test002() throws Exception {
    Class<?> anonymousClass = Annotation008.anonymousClass2.getClass();
    Assert.assertTrue(anonymousClass.isAnonymousClass());
    Assert.assertFalse(Modifier.isFinal(anonymousClass.getModifiers()));
  }

}
