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

import com.android.jack.category.RedundantTests;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class InnerTest {

  private static final File[] BOOTCLASSPATH = TestTools.getDefaultBootclasspath();

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testCompile() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test001")));
  }

  @Test
  public void testCompile2() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test002")));
  }

  @Test
  public void testCompile3() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test003")));
  }

  @Test
  public void testCompile4() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test004")));
  }

  @Test
  public void testCompile5() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test005")));
  }

  @Test
  public void testCompile6() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test006")));
  }

  @Test
  public void testCompile7() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test007")));
  }

  @Test
  public void testCompile8() throws Exception {
    String testName = "inner/test008";
    TestTools.runCompilation(TestTools.buildCommandLineArgs(new File[] {
        TestTools.getJackTestsWithJackFolder(testName),
        TestTools.getJackTestLibFolder(testName)}));
  }

  @Test
  public void testCompile9() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test009")));
  }

  @Test
  public void testCompile10() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test010")));
  }

  @Test
  public void testCompile11() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test011")));
  }

  @Test
  public void testCompile12() throws Exception {
    String testName = "inner/test012";
    TestTools.runCompilation(TestTools.buildCommandLineArgs(new File[] {
        TestTools.getJackTestsWithJackFolder(testName),
        TestTools.getJackTestLibFolder(testName)}));
  }

  @Test
  public void testCompile13() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test013")));
  }

  @Test
  public void testCompile14() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
            TestTools.getJackTestsWithJackFolder("inner/test014")));
  }

  @Test
  public void testCompile15() throws Exception {
    String testName = "inner/test015";
    TestTools.runCompilation(TestTools.buildCommandLineArgs(new File[] {
        TestTools.getJackTestsWithJackFolder(testName),
        TestTools.getJackTestLibFolder(testName)}));
  }

  @Test
  public void testCompile16() throws Exception {
    String testName = "inner/test016";
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder(testName)));
  }

  @Test
  public void testCompile17() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test017")));
  }

  @Test
  public void testCompile18() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test018")));
  }

  @Test
  public void testCompile19() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test019")));
  }

  @Test
  @Category(RedundantTests.class)
  public void testCompile20() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test020")));
  }

  @Test
  public void testCheckStructure20() throws Exception {
    //TODO: find out why debug info check fails
    TestTools.checkStructure(BOOTCLASSPATH, null,
        TestTools.getJackTestsWithJackFolder("inner/test020"), false /*withDebugInfo*/);
  }

  @Test
  @Category(RedundantTests.class)
  public void testCompile21() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test021")));
  }

  @Test
  public void testCheckStructure21() throws Exception {
    TestTools.checkStructure(BOOTCLASSPATH, null,
        TestTools.getJackTestsWithJackFolder("inner/test021"), false /*withDebugInfo*/);
  }

  @Test
  public void testCompile22() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test022")));
  }

  @Test
  public void testCompile23() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test023")));
  }

  @Test
  public void testCompile24() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test024")));
  }

  @Test
  public void testCompile25() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("inner/test025")));
  }
}
