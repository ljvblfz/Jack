/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.optimizations.usedef;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

public class UseDefTests extends RuntimeTest {
  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.optimizations.usedef.test001"),
      "com.android.jack.optimizations.usedef.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.optimizations.usedef.test002"),
      "com.android.jack.optimizations.usedef.test002.Tests").setSrcDirName("");

    @Test
    @Runtime
    @Category(RuntimeRegressionTest.class)
    public void test001() throws Exception {
      new RuntimeTestHelper(TEST001).compileAndRunTest();
    }

    @Test
    @Runtime
    @Category(RuntimeRegressionTest.class)
    public void test002() throws Exception {
      new RuntimeTestHelper(TEST002).compileAndRunTest();
    }

    @Override
    protected void fillRtTestInfos() {
      rtTestInfos.add(TEST001);
      rtTestInfos.add(TEST002);
    }
}
