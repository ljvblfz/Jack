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

package com.android.jack.cast;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.annotation.Nonnull;

public class CastTests extends RuntimeTest {

  private RuntimeTestInfo EXPLICIT001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.cast.explicit001"),
    "com.android.jack.cast.explicit001.dx.Tests");

  private RuntimeTestInfo IMPLICIT001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.cast.implicit001"),
    "com.android.jack.cast.implicit001.dx.Tests");

  private RuntimeTestInfo IMPLICIT002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.cast.implicit002"),
    "com.android.jack.cast.implicit002.dx.Tests");

  private RuntimeTestInfo IMPLICIT003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.cast.implicit003"),
    "com.android.jack.cast.implicit003.dx.Tests");

  private RuntimeTestInfo IMPLICIT004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.cast.implicit004"),
    "com.android.jack.cast.implicit004.dx.Tests");

  private RuntimeTestInfo USELESS001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.cast.useless001"),
    "com.android.jack.cast.useless001.dx.Tests");

  private RuntimeTestInfo USELESS002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.cast.useless002"),
    "com.android.jack.cast.useless002.dx.Tests");

  @BeforeClass
  public static void setUpClass() {
    CastTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }
  @Test
  @Category(RuntimeRegressionTest.class)
  public void explicit001() throws Exception {
    new RuntimeTestHelper(EXPLICIT001).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void implicit001() throws Exception {
    new RuntimeTestHelper(IMPLICIT001).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void implicit002() throws Exception {
    new RuntimeTestHelper(IMPLICIT002).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void implicit003() throws Exception {
    new RuntimeTestHelper(IMPLICIT003).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void implicit004() throws Exception {
    new RuntimeTestHelper(IMPLICIT004).compileAndRunTest();
  }

  @Test
  public void implicitCast005() throws Exception {
    compileTest("implicit005");
  }

  @Test
  public void implicitCast006() throws Exception {
    compileTest("implicit006");
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void useless001() throws Exception {
    new RuntimeTestHelper(USELESS001).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void useless002() throws Exception {
    new RuntimeTestHelper(USELESS002).compileAndRunTest();
  }

  /**
   * Verifies that the test source can compile from source to dex file.
   */
  private void compileTest(@Nonnull String name) throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToExe(AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        AbstractTestTools.createTempDir(),
        /* zipFile = */false,
        AbstractTestTools.getTestRootDir("com.android.jack.cast." + name + ".jack"));
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(EXPLICIT001);
    rtTestInfos.add(IMPLICIT001);
    rtTestInfos.add(IMPLICIT002);
    rtTestInfos.add(IMPLICIT003);
    rtTestInfos.add(IMPLICIT004);
    rtTestInfos.add(USELESS001);
    rtTestInfos.add(USELESS002);
  }
}
