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

package com.android.jack.annotation;

import com.android.jack.test.category.KnownBugs;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.CheckDexStructureTestHelper;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class AnnotationTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test001"),
    "com.android.jack.annotation.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test002"),
    "com.android.jack.annotation.test002.dx.Tests");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test003"),
    "com.android.jack.annotation.test003.dx.Tests");

  private RuntimeTestInfo TEST005 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test005"),
    "com.android.jack.annotation.test005.dx.Tests");

  private RuntimeTestInfo TEST006 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test006"),
    "com.android.jack.annotation.test006.dx.Tests");

  private RuntimeTestInfo TEST007 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test007"),
    "com.android.jack.annotation.test007.dx.Tests");

  private RuntimeTestInfo TEST008 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test008"),
    "com.android.jack.annotation.test008.dx.Tests");

  private RuntimeTestInfo TEST009 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test009"),
    "com.android.jack.annotation.test009.dx.Tests");

  private static final File ANNOTATION001_PATH =
      AbstractTestTools.getTestRootDir("com.android.jack.annotation.test001.jack");

  @BeforeClass
  public static void setUpClass() {
    AnnotationTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }
  @Test
  @Category(RuntimeRegressionTest.class)
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  public void test001_2() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        new File[] {
          new File(ANNOTATION001_PATH, "Annotation8.java"),
          new File(ANNOTATION001_PATH, "Annotated2.java")});
  }


  @Test
  @Category(RuntimeRegressionTest.class)
  public void test002() throws Exception {
    new RuntimeTestHelper(TEST002).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test003() throws Exception {
    new RuntimeTestHelper(TEST003).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  public void test003_1() throws Exception {
    CheckDexStructureTestHelper helper = new CheckDexStructureTestHelper(
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test003.jack"));
    helper.compare();
  }

  @Test
  public void test004() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.annotation.test004");
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToExe(
        AbstractTestTools.createTempDir(),
        /* zipFile = */false, testFolder);
  }

  @Test
  public void test004_1() throws Exception {
    CheckDexStructureTestHelper helper = new CheckDexStructureTestHelper(
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test004.jack"));
    helper.compare();
  }

  @Test
  // Annotation on package are not supported in dex format: http://code.google.com/p/android/issues/detail?id=16149
  @Category({RuntimeRegressionTest.class, KnownBugs.class})
  public void test005() throws Exception {
    new RuntimeTestHelper(TEST005).compileAndRunTest();
  }

  @Test
  public void test005_1() throws Exception {
    CheckDexStructureTestHelper helper = new CheckDexStructureTestHelper(
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test005.jack"));
    helper.compare();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test006() throws Exception {
    new RuntimeTestHelper(TEST006).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test007() throws Exception {
    new RuntimeTestHelper(TEST007).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test008() throws Exception {
    new RuntimeTestHelper(TEST008).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test009() throws Exception {
    new RuntimeTestHelper(TEST009).compileAndRunTest(/* checkStructure = */ true);
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
    rtTestInfos.add(TEST002);
    rtTestInfos.add(TEST003);
//    rtTestInfos.add(TEST005); // KnownBug
    rtTestInfos.add(TEST006);
    rtTestInfos.add(TEST007);
    rtTestInfos.add(TEST008);
    rtTestInfos.add(TEST009);
  }
}
