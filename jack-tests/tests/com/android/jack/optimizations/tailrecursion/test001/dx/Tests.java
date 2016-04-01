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

package com.android.jack.optimizations.tailrecursion.test001.dx;

import junit.framework.Assert;

import org.junit.Test;

import com.android.jack.optimizations.tailrecursion.test001.jack.TailRecursion;

public class Tests {
  @Test
  public void test001() {
    Assert.assertEquals(120, TailRecursion.test001(5, 1));
  }

  // overridable instance method -- call to base
  @Test
  public void test002() {
    Assert.assertEquals(120, TailRecursion.test002(5));
    Assert.assertTrue(TailRecursion.instanceFooRecTracker.recursionDetected());
  }

  // overridable instance method -- call to derived
  @Test
  public void test003() {
    Assert.assertEquals(3840, TailRecursion.test003(5));
    Assert.assertTrue(TailRecursion.instanceFooRecTracker.recursionDetected());
  }

  // static method
  @Test
  public void test004() {
    Assert.assertEquals(120, TailRecursion.test004(5));
    Assert.assertFalse(TailRecursion.staticFooRecTracker.recursionDetected());
  }

  // private method
  @Test
  public void test005() {
    Assert.assertEquals(120, TailRecursion.test005(5));
    Assert.assertFalse(TailRecursion.privateFooRecTracker.recursionDetected());
  }

  // final method
  @Test
  public void test006() {
    Assert.assertEquals(120, TailRecursion.test006(5));
    Assert.assertFalse(TailRecursion.finalFooRecTracker.recursionDetected());
  }

  // final method with final params
  @Test
  public void test007() {
    Assert.assertEquals(120, TailRecursion.test007(5));
    Assert.assertFalse(TailRecursion.finalParamFooRecTracker.recursionDetected());
  }
}
