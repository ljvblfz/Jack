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

package com.android.jack.ifstatement;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class IfstatementTests extends RuntimeTest {

  private RuntimeTestInfo ADVANCEDTEST = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.ifstatement.advancedTest"),
    "com.android.jack.ifstatement.advancedTest.dx.Tests");

  private RuntimeTestInfo CFGTEST = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.ifstatement.cfgTest"),
    "com.android.jack.ifstatement.cfgTest.dx.Tests");

  private RuntimeTestInfo FASTPATH = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.ifstatement.fastpath"),
    "com.android.jack.ifstatement.fastpath.dx.Tests");

  private RuntimeTestInfo SIMPLETEST = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.ifstatement.simpleTest"),
    "com.android.jack.ifstatement.simpleTest.dx.Tests");


  @Test
  @Category(RuntimeRegressionTest.class)
  public void advancedTest() throws Exception {
    new RuntimeTestHelper(ADVANCEDTEST).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void cfgTest() throws Exception {
    new RuntimeTestHelper(CFGTEST).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void fastpath() throws Exception {
    new RuntimeTestHelper(FASTPATH).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void simpleTest() throws Exception {
    new RuntimeTestHelper(SIMPLETEST).compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(ADVANCEDTEST);
    rtTestInfos.add(CFGTEST);
    rtTestInfos.add(FASTPATH);
    rtTestInfos.add(SIMPLETEST);
  }
}
