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

import com.android.jack.Options;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.test.TestsProperties;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * JUnit test for compilation of annotations.
 */
public class AnnotationTest {

  private RuntimeTestInfo ANNOTATION001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.annotation.test001"),
      "com.android.jack.java8.annotation.test001.jack.Tests");

  private RuntimeTestInfo ANNOTATION002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.annotation.test002"),
      "com.android.jack.java8.annotation.test002.jack.Tests");

  private RuntimeTestInfo ANNOTATION003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.annotation.test003"),
      "com.android.jack.java8.annotation.test003.jack.Tests");

  @Test
  @KnownIssue
  public void testAnnotation001() throws Exception {
    compileAndRun(ANNOTATION001);
  }

  @Test
  @KnownIssue
  public void testAnnotation002() throws Exception {
    compileAndRun(ANNOTATION002);
  }

  @Test
  @KnownIssue
  public void testAnnotation003() throws Exception {
    compileAndRun(ANNOTATION003);
  }

  /**
   * Check that source using repeatable can be compiled through jack library when repeatable
   * definition is into another library. Dex file generated from the source files is generated
   * through a jack library where the predex is used on the flow jack library -> dex file.
   */
  @Test
  @KnownIssue
  public void testAnnotation004() throws Exception {
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    File lib =
        AbstractTestTools.createTempFile("lib", toolchain.getLibraryExtension());
    File sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.annotation.test004.lib");
    toolchain.addToClasspath(defaultClasspath).srcToLib(lib, /* zipFiles = */ true,
        sourceDir);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File lixDexFolder = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(defaultClasspath)
    .libToExe(lib,  lixDexFolder, /* zipFiles = */ false);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File srclib = AbstractTestTools.createTempFile("srclib", toolchain.getLibraryExtension());
    sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.annotation.test004.jack");
    toolchain.addToClasspath(defaultClasspath)
    .addToClasspath(lib)
    .setSourceLevel(SourceLevel.JAVA_8)
    .srcToLib(srclib, /* zipFiles = */ true, sourceDir);


    toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File srcDexFolder = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(defaultClasspath)
    .setSourceLevel(SourceLevel.JAVA_8)
    .libToExe(srclib, srcDexFolder, /* zipFiles = */ false);

    run("com.android.jack.java8.annotation.test004.jack.Tests",
        new File[] {
            new File(TestsProperties.getJackRootDir(), "jack-tests/prebuilts/junit4-hostdex.jar"),
            new File(lixDexFolder, DexFileWriter.DEX_FILENAME),
            new File(srcDexFolder, DexFileWriter.DEX_FILENAME)});
  }

  /**
   * Check that source using repeatable can be compiled through jack library when repeatable
   * definition is into another library. Dex file generated from the source files is generated
   * through a jack library where the predex is not used on the flow jack library -> dex file.
   */
  @Test
  @KnownIssue
  public void testAnnotation005() throws Exception {
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    File lib =
        AbstractTestTools.createTempFile("lib", toolchain.getLibraryExtension());
    File sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.annotation.test004.lib");
    toolchain.addToClasspath(defaultClasspath).srcToLib(lib, /* zipFiles = */ true,
        sourceDir);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File lixDexFolder = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(defaultClasspath)
    .libToExe(lib,  lixDexFolder, /* zipFiles = */ false);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File srclib = AbstractTestTools.createTempFile("srclib", toolchain.getLibraryExtension());
    sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.annotation.test004.jack");
    toolchain.addToClasspath(defaultClasspath)
    .addToClasspath(lib)
    .setSourceLevel(SourceLevel.JAVA_8)
    .srcToLib(srclib, /* zipFiles = */ true, sourceDir);


    toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Options.USE_PREBUILT_FROM_LIBRARY.getName(), Boolean.FALSE.toString());
    File srcDexFolder = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(defaultClasspath)
    .setSourceLevel(SourceLevel.JAVA_8)
    .libToExe(srclib, srcDexFolder, /* zipFiles = */ false);

    run("com.android.jack.java8.annotation.test004.jack.Tests",
        new File[] {
            new File(TestsProperties.getJackRootDir(), "jack-tests/prebuilts/junit4-hostdex.jar"),
            new File(lixDexFolder, DexFileWriter.DEX_FILENAME),
            new File(srcDexFolder, DexFileWriter.DEX_FILENAME)});
  }

  @Nonnull
  private void run(@Nonnull String mainClass, @Nonnull File[] dexFiles) throws Exception {
    List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners(null);
    for (RuntimeRunner runner : runnerList) {
      Assert.assertEquals(0, runner.runJUnit(new String[0], AbstractTestTools.JUNIT_RUNNER_NAME,
          new String[] {mainClass}, dexFiles));
    }
  }

  private void compileAndRun(@Nonnull RuntimeTestInfo testInfo) throws Exception {
    new RuntimeTestHelper(testInfo)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addIgnoredCandidateToolchain(JackApiV01.class)
    .compileAndRunTest();
  }

}
