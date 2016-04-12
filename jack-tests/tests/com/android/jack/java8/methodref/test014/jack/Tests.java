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

package com.android.jack.java8.methodref.test014.jack;

import org.junit.Assert;
import org.junit.Test;

interface I1 {
  void getAdd(int i, Object o1, Object o2);
}


interface I {
  Adder getAdd();
}


abstract class Adder {
  abstract int getValue();
}


public class Tests {

  int result;

  public int test(int a, int b) {
    class Add0 {

      public Add0(int i, Object... rest) {
        result = i + a + b;
      }

      public int getAdd() {
        return 0;
      }
    }

    I1 i1 = Add0::new;
    i1.getAdd(15, new Object(), new Object());
    return result;
  }

  @Test
  public void test() {
    Assert.assertEquals(23, new Tests().test(3, 5));
  }
}
