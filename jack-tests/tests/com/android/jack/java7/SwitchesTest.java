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

package com.android.jack.java7;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * JUnit test for compilation of Java 7 features
 */
public class SwitchesTest {

  @Nonnull
  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.switches.test001"),
      "com.android.jack.java7.switches.test001.dx.Tests");

  @Nonnull
  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.switches.test002"),
      "com.android.jack.java7.switches.test002.dx.Tests");

  @Nonnull
  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java7.switches.test003"),
      "com.android.jack.java7.switches.test003.dx.Tests");

  @Test
  @Runtime
  public void java7Switches001() throws Exception {
    new RuntimeTestHelper(TEST001).setSourceLevel(SourceLevel.JAVA_7).compileAndRunTest();
  }

  @Test
  @Runtime
  public void java7Switches002() throws Exception {
    new RuntimeTestHelper(TEST002).setSourceLevel(SourceLevel.JAVA_7).compileAndRunTest();
  }

  @Test
  @Runtime
  public void java7Switches003() throws Exception {
    new RuntimeTestHelper(TEST003).setSourceLevel(SourceLevel.JAVA_7).compileAndRunTest();
  }

  @Test
  @Runtime
  public void java7Switches004() throws Exception {

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain();

    File jackZipFile = AbstractTestTools.createTempFile("tmp", toolchain.getLibraryExtension());

    toolchain.setSourceLevel(SourceLevel.JAVA_7)
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackZipFile,
        /* zipFiles = */ true,
        AbstractTestTools.getTestRootDir("com.android.jack.java7.switches.test001.jack"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.setSourceLevel(SourceLevel.JAVA_7).libToExe(
        jackZipFile,
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false);
  }

}
