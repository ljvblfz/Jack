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

package com.android.jack.java8;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.Test;

public class BridgeTestPreN {

  private RuntimeTestInfo BRIDGE001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.bridges.test001"),
      "com.android.jack.java8.bridges.test001.jack.Tests");

  @Test
  public void testBridge001() throws Exception {
    new RuntimeTestHelper(BRIDGE001).setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .addIgnoredCandidateToolchain(JillBasedToolchain.class).compileAndRunTest();
  }

}
