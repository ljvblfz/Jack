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

package com.android.jack.toolchain;

import com.android.jack.ProguardFlags;
import com.android.jack.Sourcelist;
import com.android.jack.TestTools;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.AndroidToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

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

  private static File coreOut;

  @BeforeClass
  public static void setup() throws Exception {
    CORE_SOURCELIST = TestTools.getTargetLibSourcelist("core-libart");
    BOUNCY_SOURCELIST =
        TestTools.getTargetLibSourcelist("bouncycastle");
    JUNIT_SOURCELIST = TestTools.getHostLibSourcelist("junit4-hostdex-jack");

    coreOut = AbstractTestTools.createTempFile("core", ".jack");
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.setSourceLevel(SourceLevel.JAVA_7);
    toolchain.srcToLib(coreOut, /* zipFiles = */ true, CORE_SOURCELIST);
  }

  @Test
  public void shrobTestNoZip() throws Exception {
    File shrobTestJackOut = AbstractTestTools.createTempDir();
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.shrob.test001");
    File sourceDir = new File(testFolder, "jack");
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.addToClasspath(coreOut)
    .srcToLib(shrobTestJackOut, /* zipFiles = */ false, sourceDir);

    File shrobTestShrunkOut = AbstractTestTools.createTempDir();
    List<ProguardFlags> flagFiles = new ArrayList<ProguardFlags>();
    flagFiles.add(new ProguardFlags(testFolder, "proguard.flags001"));
    flagFiles.add(new ProguardFlags(testFolder, "proguard.flags004"));
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToLib(shrobTestJackOut, shrobTestShrunkOut, /* zipFiles = */ false);

    File shrobTestDexOutFolder = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToExe(shrobTestShrunkOut, shrobTestDexOutFolder, /* zipFile = */ false);
  }

  @Test
  public void shrobTestZip() throws Exception {
    File shrobTestJackOut = AbstractTestTools.createTempFile("shrobtest", ".jack");
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.shrob.test001");
    File sourceDir = new File(testFolder, "jack");

    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.addToClasspath(coreOut)
    .srcToLib(shrobTestJackOut, /* zipFiles = */ true, sourceDir);

    File shrobTestShrunkOut = AbstractTestTools.createTempFile("shrunk", ".jack");
    List<ProguardFlags> flagFiles = new ArrayList<ProguardFlags>();
    flagFiles.add(new ProguardFlags(testFolder, "proguard.flags001"));
    flagFiles.add(new ProguardFlags(testFolder, "proguard.flags004"));
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToLib(shrobTestJackOut, shrobTestShrunkOut, /* zipFiles = */ true);

    File shrobTestDexOut = AbstractTestTools.createTempFile("shrobbed", ".dex.zip");
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToExe(shrobTestShrunkOut, shrobTestDexOut, /* zipFile = */ true);
  }

  @Test
  public void bouncyCastle() throws Exception {
    File bouncyCastleJack = AbstractTestTools.createTempFile("bouncyjack", ".jack");
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.addToClasspath(coreOut)
    .srcToLib(bouncyCastleJack, /* zipFiles = */ true, BOUNCY_SOURCELIST);

    File bouncyCastleOutFolder = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToExe(bouncyCastleJack, bouncyCastleOutFolder, /* zipFile = */ false);
  }

  @Test
  public void core() throws Exception {
    File coreOutFolder = AbstractTestTools.createTempDir();
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToExe(coreOut, coreOutFolder, /* zipFile = */ false);
  }

  @Test
  public void junit() throws Exception {
    File junitJack = AbstractTestTools.createTempFile("junit", ".zip");
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.addToClasspath(coreOut)
    .addToClasspath(TestTools.getFromAndroidTree(
            "out/host/common/obj/JAVA_LIBRARIES/hamcrest-core-hostdex-jack_intermediates/classes.jack"))
    .srcToLib(junitJack, /* zipFiles = */ true, JUNIT_SOURCELIST);

    File junitOutFolder = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToExe(junitJack, junitOutFolder, /* zipFile = */ false);
  }

  @Test
  public void jarjarTest() throws Exception {
    File jarjarTestJackOut = AbstractTestTools.createTempFile("jarjartest", ".jack");
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.jarjar.test003");
    File sourceDir = new File(testFolder, "jack");
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.addToClasspath(coreOut)
    .srcToLib(jarjarTestJackOut, /* zipFiles = */ true, sourceDir);

    File dalvikAnnotations = TestTools.getFromAndroidTree("libcore/dalvik/src/main/java/");
    File dalvikAnnotationsJackOut = AbstractTestTools.createTempFile("dalvikannotations", ".jack");
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.addToClasspath(coreOut)
    .srcToLib(dalvikAnnotationsJackOut, /* zipFiles = */ true, dalvikAnnotations);

    File jarjarTestRenamedOut = AbstractTestTools.createTempFile("jarjartestrenamed", ".jack");
    File jarjarRules = new File(testFolder, "jarjar-rules.txt");
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.setJarjarRules(jarjarRules);
    toolchain.libToLib(jarjarTestJackOut, jarjarTestRenamedOut, /* zipFiles = */ true);

    File jarjarTestDexOutFolder = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToExe(jarjarTestRenamedOut, jarjarTestDexOutFolder, /* zipFile = */ false);
  }
}
