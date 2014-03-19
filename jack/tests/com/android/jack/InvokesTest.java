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

import java.io.File;

/**
 * JUnit test for compilation of invoke tests.
 */
public class InvokesTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void testCompile() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("invoke/test001")));
  }

  @Test
  public void testCompile002() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("invoke/test002")));
  }

  @Test
  public void testCompile003() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("invoke/test003")));
  }

  @Test
  public void testCompile004() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("invoke/test004")));
  }

  @Test
  public void testCompile005() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(new File[] {
        TestTools.getJackTestsWithJackFolder("invoke/test005"),
        TestTools.getJackTestLibFolder("invoke/test005")}));
  }

  @Test
  public void testCompile006() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
            TestTools.getJackTestsWithJackFolder("invoke/test006")));
  }
}
