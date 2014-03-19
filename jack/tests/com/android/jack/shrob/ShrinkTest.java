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
import com.android.jack.TestTools;
import com.android.jack.util.TextUtils;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class ShrinkTest extends AbstractTest {

  private static ProguardFlags dontObfuscateFlagFile =
      new ProguardFlags(TestTools.getJackTestFolder("shrob"), "dontobfuscate.flags");

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Override
  protected void runTest(
      @CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath,
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {
    String testName = "shrob/test" + testNumber;
    File testFolder = TestTools.getJackTestFolder(testName);
    ProguardFlags[] proguardflagsFiles = new ProguardFlags[] {
        dontObfuscateFlagFile,
        new ProguardFlags(TestTools.getJackTestFolder("shrob"),"keepAllAttributes.flags"),
        new ProguardFlags(testFolder, "proguard.flags" + flagNumber)};
    File refFolder = new File(testFolder, "refsShrinking");
    TestTools.checkListing(bootclasspath, classpath, TestTools.getJackTestsWithJackFolder(testName),
        proguardflagsFiles, new File(refFolder, "expected-" + flagNumber + ".txt"));
  }

  @Test
  public void test020() throws Exception {
    File libOut = TestTools.createTempDir("ShrinkTest", "lib");
    File testOut = null;
    File shrinkOut = null;

    try {
      Options libOptions = TestTools.buildCommandLineArgs(
          TestTools.getJackTestLibFolder("shrob/test020"));
      libOptions.setJayceOutputDir(libOut);
      TestTools.runCompilation(libOptions);

      testOut = TestTools.createTempDir("ShrinkTest", "test");
      Options testOptions = TestTools.buildCommandLineArgs(
          TestTools.getJackTestsWithJackFolder("shrob/test020"));
      testOptions.setJayceOutputDir(testOut);
      testOptions.setClasspath(libOut.getAbsolutePath());
      TestTools.runCompilation(testOptions);

      Options shrinkOption = new Options();
      shrinkOption.addProguardFlagsFile(dontObfuscateFlagFile);
      shrinkOption.addProguardFlagsFile(
          new ProguardFlags(TestTools.getJackTestFolder("shrob/test020"),"proguard.flags"));

      shrinkOut = TestTools.createTempDir("ShrinkTest", "shrink");
      shrinkOption.setJayceOutputDir(shrinkOut);
      shrinkOption.addJayceImport(testOut);
      TestTools.runCompilation(shrinkOption);
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
    File jackOut = TestTools.createTempDir("ShrinkTest", "jayce");
    File shrinkOut = null;
    File dexOut = null;

    try {
      Options libOptions = TestTools.buildCommandLineArgs(
          TestTools.getJackTestsWithJackFolder("shrob/test021"));
      libOptions.setJayceOutputDir(jackOut);
      TestTools.runCompilation(libOptions);

      Options shrinkOption = new Options();
      shrinkOption.addProguardFlagsFile(dontObfuscateFlagFile);
      shrinkOption.addProguardFlagsFile(
          new ProguardFlags(TestTools.getJackTestFolder("shrob/test021"),"proguard.flags001"));

      shrinkOut = TestTools.createTempDir("ShrinkTest", "test");
      shrinkOption.setJayceOutputDir(shrinkOut);
      shrinkOption.addJayceImport(jackOut);
      TestTools.runCompilation(shrinkOption);

      dexOut = TestTools.createTempFile("ShrinkTest", ".dex");
      Options dxOption = new Options();
      dxOption.addJayceImport(shrinkOut);
      dxOption.setOutputFile(dexOut);
      TestTools.runCompilation(dxOption);

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
    File testFolder = TestTools.getJackTestFolder("shrob/test028");
    File jackar = null;
    File shrinkedjackar = null;
    jackar = TestTools.createTempFile("jackar", ".zip");
    shrinkedjackar = TestTools.createTempFile("shrinkedjackar", ".zip");
    ProguardFlags flags = new ProguardFlags(testFolder, "proguard.flags001");
    String classpath = TestTools.getDefaultBootclasspathString();

    TestTools.compileSourceToJack(new Options(), testFolder, classpath, jackar, true);

    TestTools.shrobJackToJack(new Options(),
        jackar,
        classpath,
        shrinkedjackar,
        Collections.singletonList(flags),
        true);
  }
}
