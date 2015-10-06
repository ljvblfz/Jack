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

package com.android.jack.java8.gwt.test023.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface I {
    int foo(Integer i);
  }

  interface Lambda<T> {
    T run(int a, int b);
  }

  @Test
  public void testInnerClassCaptureLocalFromOuterLambda() {
    int[] x = new int[] {42};
    Lambda<Integer> l = (a, b) -> {
      int[] x1 = new int[] {32};
      Lambda<Integer> r = (rA, rB) -> {
        int[] x2 = new int[] {22};
        I i = new I() {
          public int foo(Integer arg) {
            x1[0] = x1[0] + 1;
            x[0] = x[0] + 1;
            return x2[0] = x2[0] + rA + rB + a + b;
          }
        };
        return i.foo(1);
      };
      return r.run(3, 4) + x1[0];
    };

    // x1[0](32) + 1 + x2[0](22) + rA(3) + rB(4) + a(1) + b(2)
    Assert.assertEquals(65, l.run(1, 2).intValue());
    Assert.assertEquals(43, x[0]);
  }
}
