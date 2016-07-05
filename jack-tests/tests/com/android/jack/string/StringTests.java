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

package com.android.jack.string;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class StringTests extends RuntimeTest {

  private RuntimeTestInfo CONCAT001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.string.concat001"),
    "com.android.jack.string.concat001.dx.Tests");

  private RuntimeTestInfo CONCAT002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.string.concat002"),
    "com.android.jack.string.concat002.dx.Tests");

  private RuntimeTestInfo CONCAT003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.string.concat003"),
    "com.android.jack.string.concat003.dx.Tests");



  @Test
  public void testCompileNewString() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.string.test001.jack"));
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void concat001() throws Exception {
    new RuntimeTestHelper(CONCAT001).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void concat002() throws Exception {
    new RuntimeTestHelper(CONCAT002).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void concat003() throws Exception {
    new RuntimeTestHelper(CONCAT003).compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(CONCAT001);
    rtTestInfos.add(CONCAT002);
    rtTestInfos.add(CONCAT003);
  }
}
