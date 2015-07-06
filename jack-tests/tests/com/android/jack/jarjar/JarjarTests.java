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

package com.android.jack.jarjar;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import org.junit.Test;

import java.io.File;
import java.util.Collections;

import javax.annotation.Nonnull;

public class JarjarTests {

  @Nonnull
  private RuntimeTestInfo JARJAR001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.jarjar.test001"),
      "com.android.jack.jarjar.test001.dx.Tests");

  @Nonnull
  private RuntimeTestInfo JARJAR003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.jarjar.test003"),
      "com.android.jack.jarjar.test003.dx.Tests");

  @Test
  public void jarjar001() throws Exception {
    new RuntimeTestHelper(JARJAR001)
    .compileAndRunTest();
  }

  @Test
  public void jarjar003() throws Exception {
    new RuntimeTestHelper(JARJAR003)
    .compileAndRunTest();
  }

  @Test
  public void jarjar003_1() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.setJarjarRules(
        Collections.singletonList(new File(JARJAR003.directory, "jarjar-rules.txt")));
    File lib = AbstractTestTools.createTempFile("jarjarTest003Jack", toolchain.getLibraryExtension());
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        lib,
        /* zipFiles = */ true,
        new File(JARJAR003.directory, "jack"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(lib)
    .srcToLib(AbstractTestTools.createTempFile("jarjarTest003dx", toolchain.getLibraryExtension()),
        /* zipFiles = */ true,
        new File(JARJAR003.directory, "dontcompile/TestWithRelocatedReference.java"));
  }

}
