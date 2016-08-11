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

package com.android.jack.optimizations.lambdas.test003.dx;

import com.android.jack.optimizations.lambdas.test003.jack.*;

import junit.framework.Assert;
import org.junit.Test;

/** Just touch all the classes */
public class Tests {
  @Test
  public void test001() throws Throwable {
    A a = new A();
    Assert.assertEquals(
      "{i0}{i1}{i3}",
      a.testGenerics());
  }
}
