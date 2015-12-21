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

import com.android.jack.test.TestsProperties;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

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

  @Nonnull
  private RuntimeTestInfo JARJAR004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.jarjar.test004"),
      "com.android.jack.jarjar.test004.dx.Tests");

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

  @Test
  public void jarjar004() throws Exception {

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    File libToBeRenamed =
        AbstractTestTools.createTempFile("jarjarTest004Lib", toolchain.getLibraryExtension());
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
        .srcToLib(libToBeRenamed,
            /* zipFiles = */ true, new File(JARJAR004.directory, "lib"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.setJarjarRules(
        Collections.singletonList(new File(JARJAR004.directory, "jarjar-rules.txt")));
    File libReferencingLibToBeRenamed =
        AbstractTestTools.createTempFile("jarjarTest004Jack", toolchain.getLibraryExtension());
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
        .addToClasspath(libToBeRenamed)
        .srcToLib(libReferencingLibToBeRenamed,
            /* zipFiles = */ true, new File(JARJAR004.directory, "jack"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    File renamedLib =
        AbstractTestTools.createTempFile("jarjarTest004Lib", toolchain.getLibraryExtension());
    toolchain.setJarjarRules(
        Collections.singletonList(new File(JARJAR004.directory, "jarjar-rules.txt")));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
        .srcToLib(renamedLib,
            /* zipFiles = */ true, new File(JARJAR004.directory, "lib"));


    // Build dex files for runtime
    File dex1 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.setJarjarRules(
        Collections.singletonList(new File(JARJAR004.directory, "jarjar-rules.txt")));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(libToBeRenamed)
    .srcToExe(
        dex1,
        /* zipFiles = */ true,
        new File(JARJAR004.directory, "jack"));

    File dex2 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(renamedLib)
    .addToClasspath(libReferencingLibToBeRenamed)
    .srcToExe(dex2,
        /* zipFiles = */ true,
        new File(JARJAR004.directory, "dontcompile/TestWithRelocatedReference.java"));

    File dex3 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.setJarjarRules(
        Collections.singletonList(new File(JARJAR004.directory, "jarjar-rules.txt")));
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        dex3,
        /* zipFiles = */ true,
        new File(JARJAR004.directory, "lib"));

    File dex4 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        dex4,
        /* zipFiles = */ true,
        new File(JARJAR004.directory, "lib"));

    List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners(null);
    String[] names = {"com.android.jack.jarjar.test004.dontcompile.TestWithRelocatedReference"};
    for (RuntimeRunner runner : runnerList) {
      Assert.assertEquals(
          0,
          runner.runJUnit(new String[] {}, AbstractTestTools.JUNIT_RUNNER_NAME, names, new File[] {
              new File(TestsProperties.getJackRootDir(), "jack-tests/prebuilts/junit4-hostdex.jar"),
              new File(dex1, "classes.dex"), new File(dex2, "classes.dex"),
              new File(dex3, "classes.dex"), new File(dex4, "classes.dex")}));
    }

  }


}
