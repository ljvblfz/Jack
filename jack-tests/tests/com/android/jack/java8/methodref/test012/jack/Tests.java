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

package com.android.jack.java8.methodref.test012.jack;

import org.junit.Assert;
import org.junit.Test;

interface I {
  Adder getAdd();
}


abstract class Adder {
  abstract int getValue();
}


public class Tests {

  public static int test(int a, int b) {
    class Add extends Adder {
      int val1;

      Add() {
        val1 = a;
      }

      public int getValue() {
        return val1 + b;
      }
    }

    I i = (I) Add::new;
    return i.getAdd().getValue();
  }

  @Test
  public void test001() {
    Assert.assertEquals(8, Tests.test(3, 5));
  }
}
