/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests that test different steps of the Jack toolchain.
 * These tests only rely on the Android tree for source lists.
 * They are supposed to be run in test-jack (ideally before each commit).
 */
public class ToolchainTest {

  private static final Sourcelist CORE_SOURCELIST = TestTools.getTargetLibSourcelist("core");

  private static final Sourcelist BOUNCY_SOURCELIST =
      TestTools.getTargetLibSourcelist("bouncycastle");

  private static final Sourcelist JUNIT_SOURCELIST = TestTools.getHostLibSourcelist("junit4-jack");

  private static File corePath;

  @BeforeClass
  public static void setup() throws Exception {
    File coreOut = TestTools.createTempFile("core", ".zip");
    TestTools.compileSourceToJack(new Options(), CORE_SOURCELIST, null, coreOut, true);
    corePath = coreOut;
  }

  @Test
  public void shrobTestNoZip() throws Exception {
    File shrobTestJackOut = TestTools.createTempDir("shrobtest", "dir");
    String testName = "shrob/test001";
    String classpath = corePath.getAbsolutePath();
    File sourceDir = TestTools.getJackTestsWithJackFolder(testName);
    TestTools.compileSourceToJack(
        new Options(), sourceDir, classpath, shrobTestJackOut, false);

    File shrobTestShrunkOut = TestTools.createTempDir("shrobshrunk", "dir");
    List<ProguardFlags> flagFiles = new ArrayList<ProguardFlags>();
    File testFolder = TestTools.getJackTestFolder(testName);
    flagFiles.add(new ProguardFlags(testFolder, "proguard.flags001"));
    flagFiles.add(new ProguardFlags(testFolder, "proguard.flags004"));
    TestTools.shrobJackToJack(
        new Options(), shrobTestJackOut, classpath, shrobTestShrunkOut, flagFiles, false);

    File shrobTestDexOut = TestTools.createTempFile("shrobbed", ".dex");
    TestTools.compileJackToDex(new Options(), shrobTestShrunkOut, shrobTestDexOut, false);
  }

  @Test
  public void shrobTestZip() throws Exception {
    File shrobTestJackOut = TestTools.createTempFile("shrobtest", ".zip");
    String testName = "shrob/test001";
    String classpath = corePath.getAbsolutePath();
    File sourceDir = TestTools.getJackTestsWithJackFolder(testName);
    TestTools.compileSourceToJack(
        new Options(), sourceDir, classpath, shrobTestJackOut, true);

    File shrobTestShrunkOut = TestTools.createTempFile("shrunk", ".zip");
    List<ProguardFlags> flagFiles = new ArrayList<ProguardFlags>();
    File testFolder = TestTools.getJackTestFolder(testName);
    flagFiles.add(new ProguardFlags(testFolder, "proguard.flags001"));
    flagFiles.add(new ProguardFlags(testFolder, "proguard.flags004"));
    TestTools.shrobJackToJack(
        new Options(), shrobTestJackOut, classpath, shrobTestShrunkOut, flagFiles, true);

    File shrobTestDexOut = TestTools.createTempFile("shrobbed", ".dex.zip");
    TestTools.compileJackToDex(new Options(), shrobTestShrunkOut, shrobTestDexOut, true);
  }

  @Test
  public void bouncyCastle() throws Exception {
    File bouncyCastleJack = TestTools.createTempFile("bouncyjack", ".zip");
    TestTools.compileSourceToJack(
        new Options(), BOUNCY_SOURCELIST, corePath.getAbsolutePath(), bouncyCastleJack, true);

    File bouncyCastle = TestTools.createTempFile("bouncy", ".dex");
    TestTools.compileJackToDex(new Options(), bouncyCastleJack, bouncyCastle, false);
  }

  @Test
  public void core() throws Exception {
    File coreDex = TestTools.createTempFile("core", ".dex");
    TestTools.compileJackToDex(new Options(), corePath, coreDex, false);
  }

  @Test
  public void junit() throws Exception {
    File junitJack = TestTools.createTempFile("junit", ".zip");
    TestTools.compileSourceToJack(
        new Options(), JUNIT_SOURCELIST, corePath.getAbsolutePath() + File.pathSeparator +
        TestTools.getFromAndroidTree(
            "out/host/common/obj/JAVA_LIBRARIES/hamcrest-core-jack_intermediates/classes.jar"), junitJack, true);

    File junit = TestTools.createTempFile("junit", ".dex");
    TestTools.compileJackToDex(new Options(), junitJack, junit, false);
  }

  @Test
  public void libOfLib() throws Exception {

    String corePathString = corePath.getAbsolutePath();
    File libOfLibOut = TestTools.createTempFile("libOfLibOut", ".zip");
    String testName = "liboflib/lib2";
    File sourceDir = TestTools.getJackTestsWithJackFolder(testName);
    TestTools.compileSourceToJack(
        new Options(), sourceDir, corePathString, libOfLibOut, true);

    File libOut = TestTools.createTempFile("libOut", ".zip");
    String testName2 = "liboflib/lib";
    String classpath = corePathString + File.pathSeparatorChar + libOfLibOut.getAbsolutePath();
    File sourceDir2 = TestTools.getJackTestsWithJackFolder(testName2);
    TestTools.compileSourceToJack(
        new Options(), sourceDir2, classpath, libOut, true);

    File mainOut = TestTools.createTempFile("mainOut", ".zip");
    String testName3 = "liboflib/main";
    classpath = corePathString + File.pathSeparatorChar + libOut.getAbsolutePath();
    File sourceDir3 = TestTools.getJackTestsWithJackFolder(testName3);
    TestTools.compileSourceToJack(
        new Options(), sourceDir3, classpath, mainOut, true);
  }

  @Test
  public void jarjarTest() throws Exception {
    File jarjarTestJackOut = TestTools.createTempFile("jarjartest", ".zip");
    String testName = "jarjar/test003";
    String classpath = corePath.getAbsolutePath();
    File sourceDir = TestTools.getJackTestsWithJackFolder(testName);
    TestTools.compileSourceToJack(
        new Options(), sourceDir, classpath, jarjarTestJackOut, true);

    File dalvikAnnotations = TestTools.getFromAndroidTree("libcore/dalvik/src/main/java/");
    File dalvikAnnotationsJackOut = TestTools.createTempFile("dalvikannotations", ".zip");
    TestTools.compileSourceToJack(
        new Options(), dalvikAnnotations, classpath, dalvikAnnotationsJackOut, true);

    File jarjarTestRenamedOut = TestTools.createTempFile("jarjartestrenamed", ".zip");
    File jarjarRules = new File(TestTools.getJackTestFolder(testName), "jarjar-rules.txt");
    TestTools.jarjarJackToJack(
        new Options(), jarjarTestJackOut, classpath, jarjarTestRenamedOut, jarjarRules, true);

    File jarjarTestDexOut = TestTools.createTempFile("jarjared", ".dex");
    TestTools.compileJackToDex(new Options(), jarjarTestRenamedOut, jarjarTestDexOut, false);
  }
}
