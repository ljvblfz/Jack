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
import com.android.jack.ProguardFlags;
import com.android.jack.shrob.shrink.ShrinkStructurePrinter;
import com.android.jack.test.category.SlowTests;
import com.android.jack.test.comparator.ComparatorMapping;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.util.TextUtils;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

public class ShrinkTests extends AbstractTest {

  private static File shrobTestsDir =
      AbstractTestTools.getTestRootDir("com.android.jack.shrob");

  private static ProguardFlags dontObfuscateFlagFile =
      new ProguardFlags(shrobTestsDir, "dontobfuscate.flags");



  @Override
  protected void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {
    File testFolder = new File(shrobTestsDir, "test" + testNumber);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);

    File refFolder = new File(testFolder, "refsShrinking");

    File candidateNodeListing = AbstractTestTools.createTempFile("nodeListing", ".txt");
    toolchain.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING.getName(), "true");
    toolchain.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING_FILE.getName(),
        candidateNodeListing.getPath());
    toolchain.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");
    toolchain.disableDxOptimizations();

    File outFolder = AbstractTestTools.createTempDir();

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));

    // Otherwise test10_001 cannot work with jill based toolchains
    env.setWithDebugInfo(true);
    env.setCandidateTestTools(toolchain);
    env.setReferenceTestTools(new DummyToolchain());
    env.setProguardFlags(
        dontObfuscateFlagFile,
        new ProguardFlags(shrobTestsDir,"keepAllAttributes.flags"),
        new ProguardFlags(testFolder, "proguard.flags" + flagNumber));

    env.runTest(new ComparatorMapping(new File(refFolder, "expected-" + flagNumber + ".txt"),
        candidateNodeListing));
  }

  private void runTestWithLib(@Nonnull String testNumber, @Nonnull String flagNumber,
      boolean importLib) throws Exception {
    File testFolder = new File(shrobTestsDir, "test" + testNumber);

    File libOut = AbstractTestTools.createTempDir();
    JackApiToolchainBase toolchainLib =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchainLib.addToClasspath(toolchainLib.getDefaultBootClasspath()).srcToLib(libOut,
    /* zipFiles = */false, new File(shrobTestsDir, "test" + testNumber + "/lib"));

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);

    File refFolder = new File(testFolder, "refsShrinking");

    File candidateNodeListing = AbstractTestTools.createTempFile("nodeListing", ".txt");
    toolchain.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING.getName(), "true");
    toolchain.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING_FILE.getName(),
        candidateNodeListing.getPath());
    toolchain.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");
    toolchain.disableDxOptimizations();
    if (importLib) {
      toolchain.addStaticLibs(libOut);
    } else {
      toolchain.addToClasspath(libOut);
    }

    File outFolder = AbstractTestTools.createTempDir();

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));

    env.setWithDebugInfo(true);
    env.setCandidateTestTools(toolchain);
    env.setReferenceTestTools(new DummyToolchain());
    env.setProguardFlags(dontObfuscateFlagFile,
        new ProguardFlags(testFolder, "proguard.flags" + flagNumber));

    env.runTest(new ComparatorMapping(new File(refFolder, "expected-" + flagNumber + ".txt"),
        candidateNodeListing));
  }

  @Test
  public void test003_001() throws Exception {
    runTestWithLib("003", "001", false);
  }

  @Test
  public void test003_002() throws Exception {
    runTestWithLib("003", "002", true);
  }

  @Test
  public void test020() throws Exception {
    File libOut = AbstractTestTools.createTempDir();
    File testOut = null;
    File shrinkOut = null;

    try {
      JackApiToolchainBase toolchain =
          AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          libOut,
          /* zipFiles = */ false,
          new File(shrobTestsDir, "test020/lib"));

      toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
      testOut = AbstractTestTools.createTempDir();
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(libOut)
      .srcToLib(testOut,
          /* zipFiles = */ false,
          new File(shrobTestsDir, "test020/jack"));

      shrinkOut = AbstractTestTools.createTempDir();
      toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
      toolchain.addProguardFlags(
          dontObfuscateFlagFile,
          new ProguardFlags(new File(shrobTestsDir, "test020"),"proguard.flags"));
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
      toolchain.libToLib(
          testOut,
          shrinkOut,
          /* zipFile = */ false);
    } catch (Exception e) {
      String message = "Not deleting temp files of failed test ShrinkTest.test20 in:" +
          TextUtils.LINE_SEPARATOR +
          "- " + libOut.getAbsolutePath();
      if (testOut != null) {
        message += TextUtils.LINE_SEPARATOR + "- " + testOut.getAbsolutePath();
      }
      if (shrinkOut != null) {
        message += TextUtils.LINE_SEPARATOR + "- " + shrinkOut.getAbsolutePath();
      }
      System.err.println();
      System.err.println(message);
      throw e;
    }
  }

  @Test
  public void test021() throws Exception {
    File jackOut = AbstractTestTools.createTempDir();
    File shrinkOut = null;
    File dexOut = null;

    try {
      JackApiToolchainBase toolchain =
          AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          jackOut,
          /* zipFiles = */ false,
          new File(shrobTestsDir, "test021/jack"));

      toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

      File candidateNodeListing = AbstractTestTools.createTempFile("nodeListing", ".txt");
      toolchain.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING.getName(), "true");
      toolchain.addProperty(ShrinkStructurePrinter.STRUCTURE_PRINTING_FILE.getName(),
          candidateNodeListing.getPath());
      toolchain.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");
      toolchain.disableDxOptimizations();


      toolchain.addProguardFlags(
          dontObfuscateFlagFile,
          new ProguardFlags(new File( shrobTestsDir, "test021"),"proguard.flags001"));
      shrinkOut = AbstractTestTools.createTempDir();
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
      // This test does not use the SourceToDexComparisonTestHelper since it
      // calls libToLib instead of srcToExe.
      toolchain.libToLib(jackOut, shrinkOut, /* zipFiles = */ false);

      new ComparatorMapping(candidateNodeListing,
          new File(shrobTestsDir, "test021/refsShrinking/expected-001.txt")).compare();

      dexOut = AbstractTestTools.createTempDir();
      toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
      toolchain.libToExe(shrinkOut, dexOut, /* zipFile = */ false);

    } catch (Exception e) {
      String message =
          "Not deleting temp files of failed ShrinkTest.test21 in:" + TextUtils.LINE_SEPARATOR
          + "- " + jackOut.getAbsolutePath();
      if (shrinkOut != null) {
        message += TextUtils.LINE_SEPARATOR + "- " + shrinkOut.getAbsolutePath();
      }
      if (dexOut != null) {
        message += TextUtils.LINE_SEPARATOR + "- " + dexOut.getAbsolutePath();
      }
      System.err.println();
      System.err.println(message);
      throw e;
    }
  }

  /**
   * The only purpose of this test is to use jack shrink capabilities and to have no reference to
   * java/lang/Class. This test will make Jack fail if java/lang/Class methods like getField and
   * getMethod cannot be looked up (i.e. at the time this test is written, if the structure of
   * java/lang/Class has not been preloaded).
   */
  @Test
  public void test028() throws Exception {
    File testFolder = new File(shrobTestsDir, "test028");
    File jackar = null;
    File shrinkedjackar = null;

    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);

    jackar = AbstractTestTools.createTempFile("jackar", toolchain.getLibraryExtension());
    shrinkedjackar = AbstractTestTools.createTempFile("shrinkedjackar", toolchain.getLibraryExtension());
    ProguardFlags flags = new ProguardFlags(testFolder, "proguard.flags001");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackar,
        /* zipFiles = */ true,
        testFolder);

    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.libToLib(jackar, shrinkedjackar, /* zipFiles = */ true);
  }

  @Test
  public void test42_001() throws Exception {
    checkToolchainIsNotJillBased();
    runTest("042", "001", "");
  }

  @Test
  public void test42_002() throws Exception {
    checkToolchainIsNotJillBased();
    runTest("042", "002", "");
  }

  @Test
  public void test42_003() throws Exception {
    checkToolchainIsNotJillBased();
    runTest("042", "003", "");
  }

  @Test
  public void test43_001() throws Exception {
    runTest("043", "001", "");
  }

  @Test
  @Category(SlowTests.class)
  @Override
  public void test1_009() throws Exception {
    checkToolchainIsNotJillBased();
    super.test1_009();
  }

  @Test
  @Category(SlowTests.class)
  @Override
  public void test1_010() throws Exception {
    checkToolchainIsNotJillBased();
    super.test1_010();
  }

  @Test
  @Category(SlowTests.class)
  @Override
  public void test1_011() throws Exception {
    checkToolchainIsNotJillBased();
    super.test1_011();
  }

  @Test
  @Category(SlowTests.class)
  @Override
  public void test1_012() throws Exception {
    checkToolchainIsNotJillBased();
    super.test1_012();
  }

  @Test
  @Category(SlowTests.class)
  @Override
  public void test1_014() throws Exception {
    checkToolchainIsNotJillBased();
    super.test1_014();
  }

  @Test
  @Category(SlowTests.class)
  @Override
  public void test1_015() throws Exception {
    checkToolchainIsNotJillBased();
    super.test1_015();
  }

  @Test
  @Category(SlowTests.class)
  @Override
  public void test1_016() throws Exception {
    checkToolchainIsNotJillBased();
    super.test1_016();
  }

  @Override
  public void test8_001() throws Exception {
    checkToolchainIsNotJillBased();
    super.test8_001();
  }

  @Test
  @Category(SlowTests.class)
  @Override
  public void test11_002() throws Exception {
    checkToolchainIsNotJillBased();
    super.test11_002();
  }

  @Override
  public void test17_001() throws Exception {
    checkToolchainIsNotJillBased();
    super.test17_001();
  }

  @Override
  public void test19_001() throws Exception {
    checkToolchainIsNotJillBased();
    super.test19_001();
  }

  @Override
  public void test36_001() throws Exception {
    checkToolchainIsNotJillBased();
    super.test36_001();
  }

  @Override
  public void test38_001() throws Exception {
    checkToolchainIsNotJillBased();
    super.test38_001();
  }

  @Test
  @Category(SlowTests.class)
  @Override
  public void test40_001() throws Exception {
    checkToolchainIsNotJillBased();
    super.test40_001();
  }

  @Test
  public void test053() throws Exception {
    RuntimeTestInfo runtimeTestInfo = new RuntimeTestInfo(
        new File(shrobTestsDir, "test053"),
        "com.android.jack.shrob.test053.dx.Tests");
    runtimeTestInfo.addProguardFlagsFileName("proguard.flags001");
    new RuntimeTestHelper(runtimeTestInfo).compileAndRunTest(/* checkStructure = */ false);
  }

}
