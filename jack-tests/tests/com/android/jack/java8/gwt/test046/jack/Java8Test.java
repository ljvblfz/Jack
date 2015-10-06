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

package com.android.jack.java8.gwt.test046.jack;

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
  public void testLambdaNestingInMultipleAnonymousCaptureLocal() {
    // checks that lambda has access to local variable and arguments when placed in local anonymous
    // class with multile nesting
    int[] x = new int[] {42};
    int result = new I() {
      public int foo(Integer i1) {
        return new I() {
          public int foo(Integer i2) {
            return new I() {
              public int foo(Integer i3) {
                Lambda<Integer> l = (a, b) -> x[0] = x[0] + a + b + i1 + i2 + i3;
                return l.run(1, 2);
              }
            }.foo(3);
          }
        }.foo(2);
      }
    }.foo(1);
    Assert.assertEquals(51, x[0]);
  }
}
