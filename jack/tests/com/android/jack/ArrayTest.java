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

package com.android.jack;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for compilation of field access.
 */
public class ArrayTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Verifies that a source containing new array can compile to dex file.
   */
  @Test
  public void testNew001() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("newarray/test001")));
  }

  /**
   * Verifies that a source containing new array can compile to dex file.
   */
  @Test
  public void testNew002() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("newarray/test002")));
  }

  /**
   * Verifies that a source containing new array can compile to dex file.
   */
  @Test
  public void testNew003() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("newarray/test003")));
  }

  /**
   * Verifies that a source containing new array can compile to dex file.
   */
  @Test
  public void testNew004() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("newarray/test004")));
  }

  @Test
  public void testNew005() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("newarray/test005")));
  }

  @Test
  public void testArray001() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("array/test001")));
  }
}
