/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.frontend;


import com.android.jack.TestTools;
import com.android.jack.util.filter.RejectAllMethods;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ConstantReuseTest {

  private static String CLASS_BINARY_NAME = "com/android/jack/constant/test002/jack/ConstantReuse";
  private static String CLASS_SIGNATURE = "L" + CLASS_BINARY_NAME + ";";
  private static File TEST_FILE = TestTools.getJackTestFromBinaryName(CLASS_BINARY_NAME);

  @Before
  public void setUp() throws Exception {
    ConstantReuseTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void intConstantReuse() throws Exception {
    TestTools.getJMethod(
        TEST_FILE,
        CLASS_SIGNATURE,
        "intConstantReuse()V", new RejectAllMethods());
  }

  @Test
  public void byteConstantReuse() throws Exception {
    TestTools.getJMethod(
        TEST_FILE,
        CLASS_SIGNATURE,
        "byteConstantReuse()V", new RejectAllMethods());
  }

  @Test
  public void shortConstantReuse() throws Exception {
    TestTools.getJMethod(
        TEST_FILE,
        CLASS_SIGNATURE,
        "shortConstantReuse()V", new RejectAllMethods());
  }

  @Test
  public void charConstantReuse() throws Exception {
    TestTools.getJMethod(
        TEST_FILE,
        CLASS_SIGNATURE,
        "charConstantReuse()V", new RejectAllMethods());
  }

  @Test
  public void floatConstantReuse() throws Exception {
    TestTools.getJMethod(
        TEST_FILE,
        CLASS_SIGNATURE,
        "floatConstantReuse()V", new RejectAllMethods());
  }

  @Test
  public void longConstantReuse() throws Exception {
    TestTools.getJMethod(
        TEST_FILE,
        CLASS_SIGNATURE,
        "longConstantReuse()V",new RejectAllMethods());
  }

  @Test
  public void doubleConstantReuse() throws Exception {
    TestTools.getJMethod(
        TEST_FILE,
        CLASS_SIGNATURE,
        "doubleConstantReuse()V",new RejectAllMethods());
  }

  @Test
  public void booleanConstantReuse() throws Exception {
    TestTools.getJMethod(
        TEST_FILE,
        CLASS_SIGNATURE,
        "booleanConstantReuse()V", new RejectAllMethods());
  }

  @Test
  public void nullConstantReuse() throws Exception {
    TestTools.getJMethod(
        TEST_FILE,
        CLASS_SIGNATURE,
        "nullConstantReuse()V", new RejectAllMethods());
  }
}
