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

package com.android.jack.java8.gwt.test022.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  static class TestLambda_ClassA {
    int[] f = new int[] {42};

    class B {
      void m() {
        Runnable r = () -> f[0] = f[0] + 1;
        r.run();
      }
    }

    int a() {
      B b = new B();
      b.m();
      return f[0];
    }
  }

  @Test
  public void testLambdaNestingCaptureField_InnerClassCapturingOuterClassVariable() {
    TestLambda_ClassA a = new TestLambda_ClassA();
    Assert.assertEquals(43, a.a());
  }
}
