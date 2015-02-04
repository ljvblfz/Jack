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

package com.android.jack.returnstatement;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class ReturnstatementTests extends RuntimeTest {

  private RuntimeTestInfo RETURNS = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.returnstatement.returns"),
    "com.android.jack.returnstatement.returns.dx.Tests");

  private RuntimeTestInfo RETURNVOID = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.returnstatement.returnvoid"),
    "com.android.jack.returnstatement.returnvoid.dx.Tests");

  @BeforeClass
  public static void setUpClass() {
    ReturnstatementTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }
  @Test
  @Category(RuntimeRegressionTest.class)
  public void returns() throws Exception {
    new RuntimeTestHelper(RETURNS).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void returnvoid() throws Exception {
    new RuntimeTestHelper(RETURNVOID).compileAndRunTest();
  }

  @Test
  public void ropBuildMethodWithSameReturnReg() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();

    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir = AbstractTestTools.getTestRootDir("com.android.jack.analysis.dfa.reachingdefs.test001");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);

  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(RETURNS);
    rtTestInfos.add(RETURNVOID);
  }
}
