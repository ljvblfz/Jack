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

package com.android.jack.java8.bridges.test002.jack;

import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

interface Base {
  Object apply();
}

interface Sub extends Base {
  @Override
  default int[] apply() { return new int[]{1,2,3}; }
}

abstract class Abs implements Base { }

class X extends Abs implements Sub { }


public class Tests {

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void test001() {
    X x = new X();
    Base b = x;
    Assert.assertEquals("[1, 2, 3]", Arrays.toString((int[])b.apply()));
  }

}
