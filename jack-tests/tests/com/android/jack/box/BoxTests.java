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

package com.android.jack.box;

import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

public class BoxTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.box.test001"),
    "com.android.jack.box.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.box.test002"),
      "com.android.jack.box.test002.jack.Java7Boxing");
  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.box.test003"),
      "com.android.jack.box.test003.jack.Java7Boxing");
  private RuntimeTestInfo TEST004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.box.test004"),
      "com.android.jack.box.test004.jack.Java7Boxing");
  private RuntimeTestInfo TEST005 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.box.test005"),
      "com.android.jack.box.test005.jack.Java7Boxing");

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).compileAndRunTest();
  }

  @Test
  @Runtime
  public void test002() throws Exception {
    new RuntimeTestHelper(TEST002).setSourceLevel(SourceLevel.JAVA_7).compileAndRunTest();
  }

  @Test
  @Runtime
  public void test002_1() throws Exception {
    checkCompilationFailOnJava6(TEST002);
  }

  @Test
  @Runtime
  public void test003() throws Exception {
    new RuntimeTestHelper(TEST003).setSourceLevel(SourceLevel.JAVA_7).compileAndRunTest();
  }

  @Test
  @Runtime
  public void test003_1() throws Exception {
    checkCompilationFailOnJava6(TEST003);
  }

  @Test
  @Runtime
  @KnownIssue
  public void test004() throws Exception {
    new RuntimeTestHelper(TEST004).setSourceLevel(SourceLevel.JAVA_7).compileAndRunTest();
  }

  @Test
  @Runtime
  public void test004_1() throws Exception {
    checkCompilationFailOnJava6(TEST004);
  }

  @Test
  @Runtime
  public void test005() throws Exception {
    new RuntimeTestHelper(TEST005).setSourceLevel(SourceLevel.JAVA_7).compileAndRunTest();
  }

  @Test
  @Runtime
  public void test005_1() throws Exception {
    checkCompilationFailOnJava6(TEST005);
  }

  private void checkCompilationFailOnJava6(@Nonnull RuntimeTestInfo test) throws Exception {
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.setSourceLevel(SourceLevel.JAVA_6);
    File tmpOut = AbstractTestTools.createTempFile("", ".jack");
    try {
      toolchain.srcToLib(tmpOut, /* zipFiles = */ true, test.directory);
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // OK
    }
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
  }
}
