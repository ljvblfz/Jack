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

package com.android.jack.java8.gwt.test004.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {
  int local = 42;

  interface Lambda<T> {
    T run(int a, int b);
  }

  class AcceptsLambda<T> {
    public T accept(Lambda<T> foo) {
      return foo.run(10, 20);
    }
  }

  @Test
  public void testLambdaCaptureLocalAndFieldWithInnerClass() {
    int x = 10;
    Lambda<Integer> l = (a,b) -> new Lambda<Integer>() {
      @Override public Integer run(int j, int k) {
        int t = x;
        int s = local;
        return t + s + a + b;
      }
    }.run(a,b);
    Assert.assertEquals(82, new AcceptsLambda<Integer>().accept(l).intValue());
  }
}
