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
package com.android.jack.java8.gwt.test025.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface TestLambda_Inner {
    void f();
  }
  interface TestLambda_Outer {
    void accept(TestLambda_Inner t);
  }

  static class TestLambda_Class {
    public int[] s = new int[] {0};
    public void call(TestLambda_Outer a) {
      a.accept(() -> { });
    }
    class TestLambda_InnerClass {
      public int[] s = new int[] {0};
      public int test() {
        int[] s = new int[] {0};
        TestLambda_Class.this.call(
            sam0 -> TestLambda_Class.this.call(
                sam1 -> {
                  TestLambda_Class.this.call(
                    sam2 -> {
                      TestLambda_Class.this.s[0] = 10;
                      this.s[0] = 20;
                      s[0] = 30;
                    });
                  }));
        return s[0];
      }
    }
  }

  @Test
  public void testLambdaMultipleNestingCaptureFieldAndLocal() {
    TestLambda_Class a = new TestLambda_Class();
    TestLambda_Class b = new TestLambda_Class();
    int [] s = new int [] {0};
    b.call(sam0 -> a.call(sam1 -> { a.call(sam2 -> { a.s[0] = 20; b.s[0] = 30; s[0] = 40; }); }));
    Assert.assertEquals(20, a.s[0]);
    Assert.assertEquals(30, b.s[0]);
    Assert.assertEquals(40, s[0]);
  }
}
