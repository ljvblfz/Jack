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

package com.android.jack.java8.lambda.test037.jack;

import org.junit.Test;

import java.io.Serializable;

interface I {
  int getLength();
}

/**
 * Test to check that a warning is emited on Serializable lambdas.
 */
public class Tests {

  @Test
  public void test001() {
    I i = (I & Serializable) () -> 10;
  }

  interface I1 extends Serializable {
    int getLength();
  }

  @Test
  public void test002() {
    I1 i = () -> 10;
  }

  interface I3 extends Serializable {
  }

  interface I2 extends I3 {
    int getLength();
  }

  @Test
  public void test003() {
    I2 i = () -> 10;
  }
}
