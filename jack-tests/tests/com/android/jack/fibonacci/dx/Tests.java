/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.fibonacci.dx;

import com.android.jack.fibonacci.jack.FibonacciThreeAddress;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void test0() {
    Assert.assertEquals(0, FibonacciThreeAddress.fibonacci(0));
  }

  @Test
  public void test1() {
    Assert.assertEquals(1, FibonacciThreeAddress.fibonacci(1));
  }

  @Test
  public void test2() {
    Assert.assertEquals(1, FibonacciThreeAddress.fibonacci(2));
  }

  @Test
  public void test3() {
    Assert.assertEquals(2, FibonacciThreeAddress.fibonacci(3));
  }

  @Test
  public void test4() {
    Assert.assertEquals(3, FibonacciThreeAddress.fibonacci(4));
  }

  @Test
  public void test5() {
    Assert.assertEquals(5, FibonacciThreeAddress.fibonacci(5));
  }

  @Test
  public void test10() {
    Assert.assertEquals(55, FibonacciThreeAddress.fibonacci(10));
  }
}
