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

package com.android.jack.tryfinally;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class TryfinallyTests extends RuntimeTest {

  private RuntimeTestInfo FINALLY002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.tryfinally.finally002"),
    "com.android.jack.tryfinally.finally002.dx.Tests");

  private RuntimeTestInfo FINALLY003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.tryfinally.finally003"),
    "com.android.jack.tryfinally.finally003.dx.Tests");

  private RuntimeTestInfo FINALLY004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.tryfinally.finally004"),
    "com.android.jack.tryfinally.finally004.dx.Tests");

  private RuntimeTestInfo FINALLYBLOCK = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.tryfinally.finallyblock"),
    "com.android.jack.tryfinally.finallyblock.dx.Tests");


  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void finally002() throws Exception {
    new RuntimeTestHelper(FINALLY002).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void finally003() throws Exception {
    new RuntimeTestHelper(FINALLY003).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void finally004() throws Exception {
    new RuntimeTestHelper(FINALLY004).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void finallyblock() throws Exception {
    new RuntimeTestHelper(FINALLYBLOCK).compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(FINALLY002);
    rtTestInfos.add(FINALLY003);
    rtTestInfos.add(FINALLY004);
    rtTestInfos.add(FINALLYBLOCK);
  }
}
