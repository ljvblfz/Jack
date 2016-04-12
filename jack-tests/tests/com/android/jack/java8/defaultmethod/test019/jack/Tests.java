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

package com.android.jack.java8.defaultmethod.test019.jack;

import org.junit.Assert;
import org.junit.Test;

enum E {
  ONE, TWO, THREE;
}


interface I {
  default int getValue(E e) {
    switch (e) {
      case ONE:
        return 1;
      case TWO:
        return 2;
      case THREE:
        return 3;
    }
    return 4;
  }
}


/**
 * Test with a default method that use switch on enum.
 */
public class Tests {

  class A implements I {
  }

  @Test
  public void test001() throws Exception {
    Assert.assertEquals(1, new A().getValue(E.ONE));
  }
}
