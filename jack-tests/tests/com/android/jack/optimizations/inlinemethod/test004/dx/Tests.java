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

package com.android.jack.optimizations.inlinemethod.test004.dx;

import com.android.jack.optimizations.inlinemethod.test004.jack.TestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests @ForceInline annotation appeared in classpath instead.
 */
public class Tests {
  @Test
  public void test01() {
    Assert.assertEquals(1, TestCase.callAddOne(0));
    Assert.assertEquals(2, TestCase.callAddOne(1));
    Assert.assertEquals(3, TestCase.callAddOne(2));
  }
}

