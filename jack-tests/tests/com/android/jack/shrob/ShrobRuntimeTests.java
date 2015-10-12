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

package com.android.jack.shrob;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JillBasedToolchain;

import org.junit.Test;

public class ShrobRuntimeTests extends RuntimeTest {

  private RuntimeTestInfo TEST011_1 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.test011"),
      "com.android.jack.shrob.test011.dx.Tests").addProguardFlagsFileName("proguard.flags001")
      .addProguardFlagsFileName("../dontobfuscate.flags");

  private RuntimeTestInfo TEST011_2 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.test011"),
      "com.android.jack.shrob.test011.dx.Tests2").addProguardFlagsFileName("proguard.flags002");

  private RuntimeTestInfo TEST016 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.test016"),
      "com.android.jack.shrob.test016.dx.Tests").addProguardFlagsFileName("proguard.flags001")
      .addProguardFlagsFileName("applyMapping.flags");

  private RuntimeTestInfo TEST025 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.test025"),
      "com.android.jack.shrob.test025.dx.Tests").addProguardFlagsFileName("proguard.flags001");

  private RuntimeTestInfo TEST030 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.test030"),
      "com.android.jack.shrob.test030.dx.Tests").addProguardFlagsFileName("proguard.flags001")
      .addProguardFlagsFileName("../dontobfuscate.flags");

  private RuntimeTestInfo TEST046 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.test046"),
      "com.android.jack.shrob.test046.dx.Tests").addProguardFlagsFileName("proguard.flags001")
      .addProguardFlagsFileName("applyMapping.flags");

  private RuntimeTestInfo ANNOTATEDMETHOD1 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.annotatedmethod1"),
      "com.android.jack.shrob.annotatedmethod1.dx.Tests")
          .addProguardFlagsFileName("proguard.flags001")
          .addProguardFlagsFileName("applyMapping.flags");

  @Test
  public void test011_1() throws Exception {
    new RuntimeTestHelper(TEST011_1)
    .compileAndRunTest();
  }

  @Test
  public void test011_2() throws Exception {
    new RuntimeTestHelper(TEST011_2).addIgnoredCandidateToolchain(JillBasedToolchain.class)
     .compileAndRunTest();
  }

  @Test
  public void test016() throws Exception {
    new RuntimeTestHelper(TEST016)
    .compileAndRunTest();
  }

  @Test
  public void test025() throws Exception {
    new RuntimeTestHelper(TEST025)
    .compileAndRunTest();
  }

  @Test
  public void test030() throws Exception {
    new RuntimeTestHelper(TEST030)
    .compileAndRunTest();
  }

  @Test
  public void test046() throws Exception {
    new RuntimeTestHelper(TEST046)
    .compileAndRunTest();
  }

  @Test
  @KnownIssue
  public void testAnnotatedMethod1() throws Exception {
    new RuntimeTestHelper(ANNOTATEDMETHOD1)
    .compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
  }

}
