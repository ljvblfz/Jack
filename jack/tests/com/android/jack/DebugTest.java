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
import org.junit.Ignore;
import org.junit.Test;

public class DebugTest {
  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void test001() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test001"), true);
  }

  @Test
  public void test002() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test002"), true);
  }

  @Test
  public void test003_1() throws Exception {
    Options compilerArgs =
        TestTools.buildCommandLineArgs(TestTools.getJackTestsWithJackFolder("debug/test003"));
    compilerArgs.emitLocalDebugInfo = true;
    TestTools.runCompilation(compilerArgs);
  }

  @Test
  public void test003_2() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test003"), true);
  }

  @Test
  public void test004_1() throws Exception {
    Options compilerArgs =
        TestTools.buildCommandLineArgs(TestTools.getJackTestsWithJackFolder("debug/test004"));
    compilerArgs.emitLocalDebugInfo = true;
    TestTools.runCompilation(compilerArgs);
  }

  @Test
  public void test004_2() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test004"), true);
  }

  @Test
  public void test005() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test005"), true);
  }

  @Test
  @Ignore()
  public void test006() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test006"), true);
  }

  @Test
  public void test007() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test007"), true);
  }

  @Test
  public void test008() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test008"), true);
  }

  @Test
  public void test009() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test009"), true);
  }

  @Test
  public void test010() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test010"), true);
  }

  @Test
  public void test011() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test011"), true);
  }

  @Test
  public void test012() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test012"), true);
  }

  @Test
  public void test013() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test013"), true);
  }

  @Test
  public void test014() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test014"), true);
  }

  @Test
  @Ignore("Debug comparison fails because of local variable default initialization")
  public void test019() throws Exception {
    TestTools.checkStructure(
        null, null, TestTools.getJackTestsWithJackFolder("debug/test019"), true);
  }
}
