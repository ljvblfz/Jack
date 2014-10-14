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

import com.android.jack.config.id.JavaVersionPropertyId.JavaVersion;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests that test different steps of the Jack toolchain.
 * These tests only rely on the Android tree for source lists.
 * They are supposed to be run in test-jack (ideally before each commit).
 */
@Ignore("Tree")
public class ToolchainTest {

  private static Sourcelist CORE_SOURCELIST;

  private static Sourcelist BOUNCY_SOURCELIST;

  private static Sourcelist JUNIT_SOURCELIST;

  private static File corePath;

  @BeforeClass
  public static void setup() throws Exception {
    CORE_SOURCELIST = TestTools.getTargetLibSourcelist("core-libart");
    BOUNCY_SOURCELIST =
        TestTools.getTargetLibSourcelist("bouncycastle");
    JUNIT_SOURCELIST = TestTools.getHostLibSourcelist("junit4-hostdex-jack");

    File coreOut = TestTools.createTempFile("core", ".zip");
    Options options = new Options();
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), JavaVersion.JAVA_7.toString());
    TestTools.compileSourceToJack(options, CORE_SOURCELIST, null, coreOut, true);
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

    File shrobTestDexOutFolder = TestTools.createTempDir("shrobbed", "dex");
    TestTools.compileJackToDex(new Options(), shrobTestShrunkOut, shrobTestDexOutFolder, false);
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

    File bouncyCastleOutFolder = TestTools.createTempDir("bouncy", "dex");
    TestTools.compileJackToDex(new Options(), bouncyCastleJack, bouncyCastleOutFolder, false);
  }

  @Test
  public void core() throws Exception {
    File coreOutFolder = TestTools.createTempDir("core", "dex");
    TestTools.compileJackToDex(new Options(), corePath, coreOutFolder, false);
  }

  @Test
  public void junit() throws Exception {
    File junitJack = TestTools.createTempFile("junit", ".zip");
    TestTools.compileSourceToJack(
        new Options(), JUNIT_SOURCELIST, corePath.getAbsolutePath() + File.pathSeparator +
        TestTools.getFromAndroidTree(
            "out/host/common/obj/JAVA_LIBRARIES/hamcrest-core-hostdex-jack_intermediates/classes.zip"), junitJack, true);

    File junitOutFolder = TestTools.createTempDir("junit", "dex");
    TestTools.compileJackToDex(new Options(), junitJack, junitOutFolder, false);
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

    File jarjarTestDexOutFolder = TestTools.createTempDir("jarjared", "dex");
    TestTools.compileJackToDex(new Options(), jarjarTestRenamedOut, jarjarTestDexOutFolder, false);
  }
}
