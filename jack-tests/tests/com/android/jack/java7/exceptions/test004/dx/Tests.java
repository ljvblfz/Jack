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

package com.android.jack.java7.exceptions.test004.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.java7.exceptions.test004.jack.ExceptionTest;

/**
 * Test multi-catch.
 */
public class Tests {

  @Test
  public void test001() {
    Assert.assertEquals("Except1", new ExceptionTest().except001(1));
    Assert.assertEquals("2", new ExceptionTest().except001(2));
    Assert.assertEquals("Except3", new ExceptionTest().except001(3));
    Assert.assertEquals("No except", new ExceptionTest().except001(4));
  }

}
