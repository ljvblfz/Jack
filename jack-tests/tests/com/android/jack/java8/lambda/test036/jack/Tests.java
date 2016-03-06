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

package com.android.jack.java8.lambda.test036.jack;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

interface I {
  int getLength();
}

/**
 * Test that trigger a bug where enclosing instance of a lambda expression is not captured.
 */
public class Tests {

  interface I {
    Inner makeInner();
  }

  class Inner {
    int i = 10;
  }

  private Tests fieldT;

  @Test
  public void test001() {
    Tests t = new Tests();
    I i = () -> t.new Inner();
    Assert.assertEquals(10, i.makeInner().i);
  }

  @Test
  public void test002() {
    fieldT = new Tests();
    I i = () -> fieldT.new Inner();
    Assert.assertEquals(10, i.makeInner().i);
  }
}
