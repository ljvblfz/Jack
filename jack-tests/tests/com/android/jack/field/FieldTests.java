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

package com.android.jack.field;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.comparator.ComparatorDex;
import com.android.jack.test.helper.CheckDexStructureTestHelper;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.annotation.Nonnull;

public class FieldTests extends RuntimeTest {

  private RuntimeTestInfo INSTANCE001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.field.instance001"),
    "com.android.jack.field.instance001.dx.Tests");

  private RuntimeTestInfo INSTANCE002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.field.instance002"),
    "com.android.jack.field.instance002.dx.Tests");

  private RuntimeTestInfo INSTANCE003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.field.instance003"),
    "com.android.jack.field.instance003.dx.Tests");

  private RuntimeTestInfo INSTANCE004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.field.instance004"),
    "com.android.jack.field.instance004.dx.Tests");

  private RuntimeTestInfo STATIC001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.field.static001"),
    "com.android.jack.field.static001.dx.Tests");

  private RuntimeTestInfo STATIC002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.field.static002"),
    "com.android.jack.field.static002.dx.Tests");

  private RuntimeTestInfo STATIC004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.field.static004"),
    "com.android.jack.field.static004.dx.Tests");

  private RuntimeTestInfo STATIC005 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.field.static005"),
    "com.android.jack.field.static005.dx.Tests");

  @BeforeClass
  public static void setUpClass() {
    FieldTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }
  @Test
  @Category(RuntimeRegressionTest.class)
  public void instance001() throws Exception {
    new RuntimeTestHelper(INSTANCE001).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void instance002() throws Exception {
    new RuntimeTestHelper(INSTANCE002).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void instance003() throws Exception {
    new RuntimeTestHelper(INSTANCE003).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void instance004() throws Exception {
    new RuntimeTestHelper(INSTANCE004).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void static001() throws Exception {
    new RuntimeTestHelper(STATIC001).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void static002() throws Exception {
    new RuntimeTestHelper(STATIC002).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void static004() throws Exception {
    new RuntimeTestHelper(STATIC004).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void static005() throws Exception {
    new RuntimeTestHelper(STATIC005).compileAndRunTest();
  }

  @Test
  public void testCompileInstance6() throws Exception {
    compileTest("instance005");
  }

  @Test
  public void testCompileStatic() throws Exception {
    compileTest("static003");
  }

  /**
   * Compiles StaticField.java into a {@code DexFile} and compares it to a dex file created
   * using a reference compiler and {@code dx}.
   */
  @Test
  public void testStatic() throws Exception {
    checkStructure("static003");
  }

  /**
   * Compiles InstanceField.java into a {@code DexFile} and compares it to a dex file created
   * using a reference compiler and {@code dx}.
   */
  @Test
  public void testInstance() throws Exception {
    checkStructure("instance005");
  }

  private void compileTest(@Nonnull String test) throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToExe(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.field." + test + ".jack"));
  }

  private void checkStructure(@Nonnull String test) throws Exception {
    SourceToDexComparisonTestHelper helper = new CheckDexStructureTestHelper(
        AbstractTestTools.getTestRootDir("com.android.jack.field." + test + ".jack"));
    helper.runTest(new ComparatorDex(helper.getCandidateDex(), helper.getReferenceDex()));
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(INSTANCE001);
    rtTestInfos.add(INSTANCE002);
    rtTestInfos.add(INSTANCE003);
    rtTestInfos.add(INSTANCE004);
    rtTestInfos.add(STATIC001);
    rtTestInfos.add(STATIC002);
    rtTestInfos.add(STATIC004);
    rtTestInfos.add(STATIC005);
  }
}
