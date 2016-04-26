/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.java8.bridges.test005.jack;

import org.junit.Assert;
import org.junit.Test;

interface A<T> {
  T m(T t);
}

interface B extends A<String> {
  String m(String s);
}

interface A1<T> {
  T m(T t);
}

interface B1 extends A1<String> {
  String m(String s);
}

interface C extends B, B1 {
}

/**
 * Test to check that bridges are correctly generated into the inner class implementing a lambda.
 */
public class Tests {

  @Test
  public void test001() {
    C c = (s) -> { return "Hello " + s; };
    A a = c;

    try {
        a.m(new Object());
        Assert.fail();
    } catch (ClassCastException e) {

    }
  }
}
