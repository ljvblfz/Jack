/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.conditional.test005.jack;

public class Conditional005 {

  public static class A {
    protected int i = 1;
  }

  public static class B extends A{
    public B() {
      super();
      i = -1;
    }
  }

  // Check that type of '?' expression is an array
  public static int test001(A[] ai) {
    B[] ai1 = new B[]{new B()};
    return (ai == null ? ai1 : ai)[0].i;
  }

  // Check that type of '?' expression is an array
  public static int test002(A[][] ai) {
    B[][] ai1 = new B[][]{{new B(), new B()}};
    return (ai == null ? ai1 : ai)[0][0].i;
  }

  // Check that type of '?' expression is an array
  public static int test003(A[] ai) {
    B[][] ai1 = new B[][]{{new B(), new B()}};
    return (ai == null ? ai1 : ai).length;
  }

  // Check that type of '?' expression is an array
  public static int test004(int[] ai) {
    int[] ai1 = new int[]{1};
    return (ai == null ? ai1 : ai)[0] + 1;
  }

  // Check that type of '?' expression is an array
  public static Object test005(int[] ai) {
    byte[] ai1 = new byte[]{1};
    return (ai == null ? ai1 : ai);
  }
}
