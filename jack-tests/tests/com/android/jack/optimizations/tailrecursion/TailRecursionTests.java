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

package com.android.jack.optimizations.tailrecursion;

import com.android.jack.Options;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.Test;

public class TailRecursionTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.optimizations.tailrecursion.test001"),
    "com.android.jack.optimizations.tailrecursion.test001.dx.Tests");

  private RuntimeTestHelper createHelper(RuntimeTestInfo test) {
    RuntimeTestHelper helper = new RuntimeTestHelper(test);
    helper.addProperty(Options.OPTIMIZE_TAIL_RECURSION.getName(), "true");
    return helper;
  }

  @Test
  public void test001() throws Exception {
    createHelper(TEST001).compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
  }
}
