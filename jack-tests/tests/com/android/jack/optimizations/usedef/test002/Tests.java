/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.optimizations.usedef.test002;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void testNullFieldRead() {
    try {
      TestClass o = null;
      int i = o.field;
      Assert.fail(Integer.toString(i));
    } catch (NullPointerException expected) {
      // expected exception
    }
  }

  @Test
  public void testNullFieldWrite() {
    try {
      TestClass o = null;
      o.field = 1;
      Assert.fail();
    } catch (NullPointerException expected) {
      // expected exception
    }
  }

  @Test
  public void testNullArrayRead() {
    try {
      int[] arr = null;
      int i = arr[0];
      Assert.fail(Integer.toString(i));
    } catch (NullPointerException expected) {
      // expected exception
    }
  }

  @Test
  public void testNullArrayWrite() {
    try {
      int[] arr = null;
      arr[0] = 1;
      Assert.fail();
    } catch (NullPointerException expected) {
      // expected exception
    }
  }

  @Test
  public void testNullMethodCall() {
    try {
      TestClass o = null;
      o.method();
      Assert.fail();
    } catch (NullPointerException expected) {
      // expected exception
    }
  }
}
