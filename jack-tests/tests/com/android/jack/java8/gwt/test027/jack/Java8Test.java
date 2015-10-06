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

package com.android.jack.java8.gwt.test027.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface Function<T> {
    T apply();
  }
  private String f(Function<String> arg) {
    return arg.apply();
  }
  private int g(Function<Integer> arg) {
    return arg.apply().intValue();
  }

  static class TestMF_A {
    public static String getId() {
      return "A";
    }
    public int getIdx() {
      return 1;
    }
  }

  static class TestMF_B {
    public static String getId() {
      return "B";
    }
    public int getIdx() {
      return 2;
    }
  }

  @Test
  public void testMethodRefWithSameName() {
    Assert.assertEquals("A", f(TestMF_A::getId));
    Assert.assertEquals("B", f(TestMF_B::getId));
    TestMF_A a = new TestMF_A();
    TestMF_B b = new TestMF_B();
    Assert.assertEquals(1, g(a::getIdx));
    Assert.assertEquals(2, g(b::getIdx));
  }
}
