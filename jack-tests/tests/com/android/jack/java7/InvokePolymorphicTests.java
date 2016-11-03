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

package com.android.jack.java7;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.Test;

import javax.annotation.Nonnull;

/**
 * JUnit test for usage of invoke polymorphic.
 */
public class InvokePolymorphicTests {

  private RuntimeTestInfo INVOKE_POLYMORPHIC_001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test001"),
      "com.android.jack.java7.invokepolymorphic.test001.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_POLYMORPHIC_002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test002"),
      "com.android.jack.java7.invokepolymorphic.test002.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_POLYMORPHIC_003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test003"),
      "com.android.jack.java7.invokepolymorphic.test003.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_POLYMORPHIC_004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test004"),
      "com.android.jack.java7.invokepolymorphic.test004.Tests").setSrcDirName("");

  private RuntimeTestInfo INVOKE_POLYMORPHIC_005 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.invokepolymorphic.test005"),
      "com.android.jack.java7.invokepolymorphic.test005.Tests").setSrcDirName("");

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic001() throws Exception {
    run(INVOKE_POLYMORPHIC_001);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic002() throws Exception {
    run(INVOKE_POLYMORPHIC_002);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic003() throws Exception {
    run(INVOKE_POLYMORPHIC_003);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic004() throws Exception {
    run(INVOKE_POLYMORPHIC_004);
  }

  @Test
  @Runtime
  @KnownIssue
  public void testInvokePolymorphic005() throws Exception {
    run(INVOKE_POLYMORPHIC_005);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti).setSourceLevel(SourceLevel.JAVA_7).compileAndRunTest();
  }
}