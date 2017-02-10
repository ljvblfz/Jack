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

package com.android.jack.java8;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.Test;

import javax.annotation.Nonnull;


/**
 * JUnit test for compilation of explicit receiver usage.
 */
public class ExplicitReceiverTest {

  private final RuntimeTestInfo EXPLICIT_RECEIVER_001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.explicitreceiver.test001"),
      "com.android.jack.java8.explicitreceiver.test001.Tests").setSrcDirName("");

  private final RuntimeTestInfo EXPLICIT_RECEIVER_002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.explicitreceiver.test002"),
      "com.android.jack.java8.explicitreceiver.test002.Tests").setSrcDirName("");

  @Test
  @Runtime
  public void testExplicitReceiver001() throws Exception {
    run(EXPLICIT_RECEIVER_001);
  }

  @Test
  @Runtime
  public void testExplicitReceiver002() throws Exception {
    run(EXPLICIT_RECEIVER_002);
  }


  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti)
      .setSourceLevel(SourceLevel.JAVA_8)
      .addIgnoredCandidateToolchain(JackApiV01.class)
      .compileAndRunTest();
  }
}
