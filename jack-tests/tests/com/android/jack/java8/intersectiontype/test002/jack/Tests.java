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

package com.android.jack.java8.intersectiontype.test002.jack;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.java8.intersectiontype.test002.jack.sub.B;

/**
 * Test checking that intersection type between a class and ZAM interface as receiver type of a call
 * calls the generated bridge to avoid illegal class access.
 */
public class Tests {

  @Test
  public void test001() {
    Assert.assertEquals(1, new IntersectionType002().test(new B()));
  }
}
