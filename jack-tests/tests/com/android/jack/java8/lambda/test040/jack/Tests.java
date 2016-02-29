/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.java8.lambda.test040.jack;

import com.android.jack.java8.lambda.test040.jack.subpkg.A;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  class B extends A {
    class C {
      private int test() {
        I i = () -> field;
        return i.getCst() + field;
      }
    }
  }

  @Test
  public void test001() {
    Assert.assertEquals(20, new Tests().new B().new C().test());
  }
}
