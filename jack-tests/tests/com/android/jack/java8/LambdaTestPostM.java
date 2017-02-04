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
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.junit.RuntimeVersion;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;
import com.android.jack.util.AndroidApiLevel;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;


/**
 * JUnit test for compilation of lambda expressions.
 */
public class LambdaTestPostM {

  /**
   * The test checks that an inner representing a lambda and implementing an interface that contains
   * bridges contains also the same number of bridges. The goal of this test is to detect a change
   * into the ECJ behavior where it does not longer generate bridge into lambda when they already
   * exists into an interface. Generate a jack library compiled with source 1.8 such as interface
   * must contains bridges. Thereafter, compile source code with a lambda implementing the interface
   * that contains bridges.
   */
  @Test
  @Runtime(from=RuntimeVersion.N)
  @KnownIssue(candidate = IncrementalToolchain.class)
  public void testLamba041() throws Exception {
    List<Class<? extends IToolchain>> excludedToolchains =
        new ArrayList<Class<? extends IToolchain>>();
    excludedToolchains.add(JackApiV01.class);
    excludedToolchains.add(JillBasedToolchain.class);

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    File lib = AbstractTestTools.createTempFile("lib", toolchain.getLibraryExtension());
    File sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test041.lib");
    toolchain.addToClasspath(defaultClasspath).setSourceLevel(SourceLevel.JAVA_8).srcToLib(lib,
        /* zipFiles = */ true, sourceDir);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File libDexFolder = AbstractTestTools.createTempDir();
    toolchain.addProperty(Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()));
    toolchain.addToClasspath(defaultClasspath).setSourceLevel(SourceLevel.JAVA_8).libToExe(lib,
        libDexFolder, /* zipFiles = */ false);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File srcDexFolder = AbstractTestTools.createTempDir();
    sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test041.jack");
    toolchain.addToClasspath(defaultClasspath).addToClasspath(lib)
        .setSourceLevel(SourceLevel.JAVA_8)
        .srcToExe(srcDexFolder, /* zipFiles = */ false, sourceDir);

    run("com.android.jack.java8.lambda.test041.jack.Tests",
        new File[] {
            new File(TestsProperties.getJackRootDir(), "jack-tests/prebuilts/junit4-hostdex.jar"),
            new File(libDexFolder, DexFileWriter.DEX_FILENAME),
            new File(srcDexFolder, DexFileWriter.DEX_FILENAME)});
  }

  private void run(@Nonnull String mainClass, @Nonnull File[] dexFiles) throws Exception {
    List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners(null);
    for (RuntimeRunner runner : runnerList) {
      Assert.assertEquals(0, runner.runJUnit(new String[0], AbstractTestTools.JUNIT_RUNNER_NAME,
          new String[] {mainClass}, dexFiles));
    }
  }
}
