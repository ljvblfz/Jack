/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.java7.parser.literals.test002.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.java7.parser.literals.test002.jack.UnderscoreNotationTest;

/**
 * Test underscore notation.
 */
public class Tests {

  @Test
  public void test001() {
    Assert.assertEquals(25, new UnderscoreNotationTest().getValue1());
    Assert.assertEquals(-25, new UnderscoreNotationTest().getValue2(), 0);
    Assert.assertEquals(11001, new UnderscoreNotationTest().getValue3());
  }

}
