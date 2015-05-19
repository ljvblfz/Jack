/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.optimizations.notsimplifier.test002.dx;

import com.android.jack.optimizations.notsimplifier.test002.jack.NotSimplifier;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests about '!' simplifier optimization using comparison with bounds values or not of types.
 */
public class Tests {

  @Test
  public void test1() {
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(Double.NaN));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(0.0d));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(-5.0d));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(-Double.MIN_VALUE));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(Double.NEGATIVE_INFINITY));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(Double.POSITIVE_INFINITY));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(Double.MAX_VALUE));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(5.0d));
  }

  @Test
  public void test2() {
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(Float.NaN));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(0.0f));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(-5.0f));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(-Float.MIN_VALUE));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(Float.NEGATIVE_INFINITY));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(Float.POSITIVE_INFINITY));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(Float.MAX_VALUE));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(5.0f));
  }

  @Test
  public void test3() {
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(0l));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(-5l));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(-Long.MIN_VALUE));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(Long.MAX_VALUE));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(1l));
  }

  @Test
  public void test4() {
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(0));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(-5));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(-Integer.MIN_VALUE));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(Integer.MAX_VALUE));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(1));
  }

  @Test
  public void test5() {
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0((byte) 0));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0((byte) -5));
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(Byte.MIN_VALUE));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(Byte.MAX_VALUE));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0((byte) 1));
  }

  @Test
  public void test6() {
    Assert.assertTrue(NotSimplifier.smallerOrEqualTo0(Character.MIN_VALUE));
    Assert.assertFalse(NotSimplifier.smallerOrEqualTo0(Character.MAX_VALUE));
  }
}
