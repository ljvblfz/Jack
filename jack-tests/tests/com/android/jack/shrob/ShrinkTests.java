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

import com.android.jack.Main;
import com.android.jack.Options;
import com.android.jack.ProguardFlags;
import com.android.jack.shrob.shrink.ShrinkStructurePrinter;
import com.android.jack.test.comparator.ComparatorMapping;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.DummyToolchain;
import com.android.jack.test.toolchain.JackApiToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.util.TextUtils;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

public class ShrinkTests extends AbstractTest {

  private static File shrobTestsDir =
      AbstractTestTools.getTestRootDir("com.android.jack.shrob");

  private static ProguardFlags dontObfuscateFlagFile =
      new ProguardFlags(shrobTestsDir, "dontobfuscate.flags");

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

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

    env.runTest(new ComparatorMapping(
        new File(refFolder, "expected-" + flagNumber + ".txt"),
        candidateNodeListing));
  }

  @Test
  public void test020() throws Exception {
    File libOut = AbstractTestTools.createTempDir();
    File testOut = null;
    File shrinkOut = null;

    try {
      JackApiToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          libOut,
          /* zipFiles = */ false,
          new File(shrobTestsDir, "test020/lib"));

      toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
      testOut = AbstractTestTools.createTempDir();
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(libOut)
      .srcToLib(testOut,
          /* zipFiles = */ false,
          new File(shrobTestsDir, "test020/jack"));

      shrinkOut = AbstractTestTools.createTempDir();
      toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
      toolchain.addProguardFlags(
          dontObfuscateFlagFile,
          new ProguardFlags(new File(shrobTestsDir, "test020"),"proguard.flags"));
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
      JackApiToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          jackOut,
          /* zipFiles = */ false,
          new File(shrobTestsDir, "test021/jack"));

      toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
      toolchain.addProguardFlags(
          dontObfuscateFlagFile,
          new ProguardFlags(new File( shrobTestsDir, "test021"),"proguard.flags001"));
      shrinkOut = AbstractTestTools.createTempDir();
      toolchain.libToLib(jackOut, shrinkOut, /* zipFiles = */ false);

      dexOut = AbstractTestTools.createTempDir();
      toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
      toolchain.libToExe(shrinkOut, dexOut, /* zipFile = */ false);

    } catch (Exception e) {
      String message = "Not deleting temp files of failed ShrinkTest.test20 in:\n" +
          "- " + jackOut.getAbsolutePath();
      if (shrinkOut != null) {
        message += "\n- " + shrinkOut.getAbsolutePath();
      }
      if (dexOut != null) {
        message += "\n- " + dexOut.getAbsolutePath();
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
    runTest("042", "001", "");
  }

  @Test
  public void test42_002() throws Exception {
    runTest("042", "002", "");
  }

  @Test
  public void test42_003() throws Exception {
    runTest("042", "003", "");
  }
}
