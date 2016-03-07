/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.android.jack.Options;
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.test.comparator.ComparatorMapping;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;

import org.junit.Test;

import java.io.File;
import java.util.Collections;

import javax.annotation.Nonnull;

public class ObfuscationWithoutMappingTests extends AbstractTest {



  @Override
  protected void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {

    File testFolder = getShrobTestRootDir(testNumber);

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File refFolder = new File(testFolder, "refsObfuscationWithoutMapping");

    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
    toolchain.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");

    File candidateOutputMapping = AbstractTestTools.createTempFile("mapping", ".txt");
    File refOutputMapping = new File(refFolder, "expected-" + flagNumber + ".txt");

    File proguardFlagsFile = addOptionsToFlagsFile(
        new File(testFolder, "proguard.flags" + flagNumber),
        testFolder,
        " -printmapping " + candidateOutputMapping.getAbsolutePath());

    toolchain.addProguardFlags(proguardFlagsFile);

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));

    env.setCandidateTestTools(toolchain);
    env.setReferenceTestTools(new DummyToolchain());

    env.runTest(new ComparatorMapping(refOutputMapping, candidateOutputMapping));
  }

  @Override
  @Test
  @KnownIssue
  public void test33_001() throws Exception {
    super.test33_001();
  }

  @Override
  @Test
  @KnownIssue
  public void test34_001() throws Exception {
    super.test34_001();
  }

  @Override
  @Test
  @KnownIssue
  public void test35_001() throws Exception {
    super.test35_001();
  }

  @Override
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void test44_001() throws Exception {
    super.test44_001();
  }

  @Override
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void test5_001() throws Exception {
    super.test5_001();
  }

  @Override
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void test15_001() throws Exception {
    super.test15_001();
  }

  /**
   * Test Obfuscation when a whole package is missing from the classpath.
   */
  @Test
  public void test54() throws Exception {
    File testRootDir = getShrobTestRootDir("054");

    // Build the lib
    File libLib = AbstractTestTools.createTempFile("shrob54", "lib.jack");
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(libLib, /* zipFiles = */ true, new File (testRootDir,"lib"));
    }
    File libDex = AbstractTestTools.createTempFile("shrob54", "lib.dex.zip");
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .libToExe(libLib, libDex, /* zipFile = */ true);
    }

    // Build the jack as a lib
    File jackLib = AbstractTestTools.createTempFile("shrob54", "jack.jack");
    File jackDir = new File(testRootDir, "jack");
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(libLib)
      .srcToLib(
          jackLib,
          /* zipFiles = */ true,
          jackDir);
    }

    // Build the jack as a dex from the lib but without classpath
    File jackDex = AbstractTestTools.createTempFile("shrob54", "jack.dex.zip");
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addProguardFlags(new File(jackDir, "proguard.flags001"))
      .libToExe(jackLib, jackDex, /* zipFile = */ true);
    }


    File testDex = AbstractTestTools.createTempFile("shrob54", "test.dex.zip");
    {
      IToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(libLib)
      .addToClasspath(jackLib)
      .srcToExe(testDex, /* zipFiles = */ true, new File(testRootDir, "dx"));
    }

    RuntimeTestHelper.runOnRuntimeEnvironments(
        Collections.singletonList("com.android.jack.shrob.test054.dx.Tests"),
        RuntimeTestHelper.getJunitDex(), libDex, jackDex, testDex);

  }
}
