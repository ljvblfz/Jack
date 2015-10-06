/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.java8.gwt.test016.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface ThreeArgs {
    int foo(int x, int y, int z);
  }

  interface ThreeVarArgs {
    int foo(int x, int y, int... z);
  }

  public static int addMany(int x, int y, int... nums) {
    int sum = x + y;
    for (int num : nums) {
      sum += num;
    }
    return sum;
  }

  @Test
  public void testVarArgsPassthroughReferenceBindingProvidedArray() {
    ThreeVarArgs t = Java8Test::addMany;
    Assert.assertEquals(6, t.foo(1,2, new int[] {3}));
  }
}
