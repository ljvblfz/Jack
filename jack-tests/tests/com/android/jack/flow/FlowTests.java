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

package com.android.jack.flow;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.Test;
import org.junit.experimental.categories.Category;

public class FlowTests extends RuntimeTest {

  private RuntimeTestInfo CFG001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.flow.cfg001"),
    "com.android.jack.flow.cfg001.dx.Tests");

  private RuntimeTestInfo LOOP = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.flow.loop"),
    "com.android.jack.flow.loop.dx.Tests");


  @Test
  @Category(RuntimeRegressionTest.class)
  public void cfg001() throws Exception {
    new RuntimeTestHelper(CFG001).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void loop() throws Exception {
    new RuntimeTestHelper(LOOP).compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(CFG001);
    rtTestInfos.add(LOOP);
  }
}
