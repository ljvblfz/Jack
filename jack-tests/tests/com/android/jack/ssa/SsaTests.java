/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.ssa;

import com.android.jack.Options;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.Test;

import java.security.cert.PKIXRevocationChecker.Option;

/**
 * A list of tests to prevent regression in certain corner cases when using Jack's SSA.
 *
 * Note that these tests are not meant to be comprehensive verification of the SSA pipeline. It is
 * used mostly to cover certain cases that requires extra attention. It should be used with
 * conjunction with the rest of the jack-tests.
 */
public class SsaTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 =
      new RuntimeTestInfo(AbstractTestTools.getTestRootDir("com.android.jack.ssa.test001"),
          "com.android.jack.ssa.test001.dx.Tests");

  @Test
  @Runtime
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).addProperty(Options.UseJackSsaIR.ENABLE.getName(), "true")
        .compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
  }
}
