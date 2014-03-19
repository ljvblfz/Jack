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

import com.android.jack.category.ExtraTests;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * JUnit test for compilation of bridges.
 */
public class BridgeTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void test001() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("bridge/test001")));
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void test002() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("bridge/test002")));
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void test003() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("bridge/test003")));
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void test004() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("bridge/test004")));
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void test005() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("bridge/test005")));
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void test006() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("bridge/test006")));
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  @Category(ExtraTests.class)
  public void test007() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("bridge/test007")));
  }
}
