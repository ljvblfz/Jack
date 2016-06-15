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

package com.android.jack.assign;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class AssignTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 =
      new RuntimeTestInfo(AbstractTestTools.getTestRootDir("com.android.jack.assign.test001"),
          "com.android.jack.assign.test001.dx.Tests");

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).compileAndRunTest();
  }

  @Test
  public void test002() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.assign.test002");
    File outFolder = AbstractTestTools.createTempDir();
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToExe(outFolder,
        /* zipFile = */false, testFolder);
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
  }
}
