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

package com.android.jack.noclasspath;

import com.android.jack.JarJarRules;
import com.android.jack.Sourcelist;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.category.SlowTests;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.AndroidToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

@Ignore("Tree")
@Category(SlowTests.class)
public class NoClasspathTest {

  private static Sourcelist CORE_SOURCELIST;

  private static Sourcelist BOUNCY_SOURCELIST;

  private static Sourcelist CONSCRYPT_SOURCELIST;

  private static Sourcelist OKHTTP_SOURCELIST;

  private static Sourcelist EXT_SOURCELIST;

  private static Sourcelist CORE_JUNIT_SOURCELIST;

  private static Sourcelist FRAMEWORK_SOURCELIST;

  protected static File FRAMEWORK_JAR ;

  private static File GUAVA_JAR;

  private static File SERVICES_JAR;

  private static File ARITY_JAR;

  private static File PLAY_SERVICE_JAR;

  private static File corePath;

  @BeforeClass
  public static void setup() throws Exception {
    CORE_SOURCELIST = TestTools.getTargetLibSourcelist("core-libart");
    BOUNCY_SOURCELIST = TestTools.getTargetLibSourcelist("bouncycastle");
    CONSCRYPT_SOURCELIST = TestTools.getTargetLibSourcelist("conscrypt");
    OKHTTP_SOURCELIST = TestTools.getTargetLibSourcelist("okhttp");
    EXT_SOURCELIST = TestTools.getTargetLibSourcelist("ext");
    CORE_JUNIT_SOURCELIST =
        TestTools.getTargetLibSourcelist("core-junit");
    FRAMEWORK_SOURCELIST =
        TestTools.getTargetLibSourcelist("framework");

    FRAMEWORK_JAR = TestTools.getFromAndroidTree(
        "/out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/" + "classes.jack");

    GUAVA_JAR = TestTools
        .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/guava_intermediates/classes.jack");
    SERVICES_JAR = TestTools
        .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/services_intermediates/"
            + "classes.jack");
    ARITY_JAR = TestTools
        .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/libarity_intermediates/"
            + "classes.jack");
    PLAY_SERVICE_JAR = TestTools
        .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/"
            + "google-play-services-first-party_intermediates/classes.jack");


    // File coreOut = AbstractTestTools.createTempFile("core", ".jack");
    File corePath = new File("/tmp/ + core.jack");
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.setSourceLevel(SourceLevel.JAVA_7);
    toolchain.srcToLib(corePath,
        /* zipFile = */ true,
        CORE_SOURCELIST);
//    corePath = coreOut.getAbsolutePath();
  }

  @Test
  public void ext() throws Exception {
    File extJack = AbstractTestTools.createTempFile("ext", ".jack");

    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.addToClasspath(corePath)
    .srcToLib(extJack, /* zipFiles = */ true, EXT_SOURCELIST);

    File extFolder = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToExe(extJack, extFolder, /* zipFile = */ false);
  }

  @Test
  public void frameworkFromJack() throws Exception {
    File conscryptJack = AbstractTestTools.createTempFile("conscrypt", ".jack");
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addToClasspath(corePath)
    .srcToLib(conscryptJack, /* zipFiles = */ true, CONSCRYPT_SOURCELIST);

    File conscryptRenamedJack = AbstractTestTools.createTempFile("conscryptrenamed", ".jack");
    File conscyptRules =
        new JarJarRules(TestTools.getFromAndroidTree("external/conscrypt/jarjar-rules.txt"));
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.setJarjarRules(conscyptRules);
    toolchain.libToLib(conscryptJack, conscryptRenamedJack, /* zipFiles = */ true);

    File okhttpJack = AbstractTestTools.createTempFile("okkttp", ".jack");
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addToClasspath(corePath)
    .addToClasspath(conscryptRenamedJack)
    .srcToLib(okhttpJack, /* zipFiles = */ true, OKHTTP_SOURCELIST);

    File okhttpRenamedJack = AbstractTestTools.createTempFile("okhttprenamed", ".jack");
    File okhttpRules =
        new JarJarRules(TestTools.getFromAndroidTree("external/okhttp/jarjar-rules.txt"));
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.setJarjarRules(okhttpRules);
    toolchain.libToLib(okhttpJack, okhttpRenamedJack, /* zipFiles = */ true);

    File extJack = AbstractTestTools.createTempFile("ext", ".jack");
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addToClasspath(corePath)
    .srcToLib(extJack, /* zipFiles = */ true, EXT_SOURCELIST);

    File bouncyCastleJack = AbstractTestTools.createTempFile("bouncyjack", ".jack");
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addToClasspath(corePath)
    .srcToLib(bouncyCastleJack, /* zipFiles = */ true, BOUNCY_SOURCELIST);

    File coreJunitJack = AbstractTestTools.createTempFile("corejunitjack", ".jack");
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addToClasspath(corePath)
    .srcToLib(coreJunitJack, /* zipFiles = */ true, CORE_JUNIT_SOURCELIST);

    File bouncyCastleRenamedJack = AbstractTestTools.createTempFile("bouncyjackrenamed", ".jack");
    File jarjarRules =
        new JarJarRules(TestTools.getFromAndroidTree("external/bouncycastle/jarjar-rules.txt"));
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.setJarjarRules(jarjarRules);
    toolchain.libToLib(bouncyCastleJack, bouncyCastleRenamedJack, /* zipFiles = */ true);

//    String classpath = corePath + File.pathSeparatorChar + conscryptRenamedJack.getAbsolutePath()
//        + File.pathSeparatorChar + okhttpRenamedJack.getAbsolutePath()
//        + File.pathSeparatorChar + extJack.getAbsolutePath()
//        + File.pathSeparatorChar + bouncyCastleRenamedJack.getAbsolutePath()
//        + File.pathSeparatorChar + coreJunitJack.getAbsolutePath();

    File[] classpath = new File[] {corePath,
        conscryptRenamedJack,
        okhttpRenamedJack,
        extJack,
        bouncyCastleRenamedJack,
        coreJunitJack};

    File frameworkJackZip = AbstractTestTools.createTempFile("framework", ".jack");
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.setSourceLevel(SourceLevel.JAVA_7);
    toolchain.addToClasspath(classpath)
    .srcToLib(frameworkJackZip, /* zipFiles = */ true, FRAMEWORK_SOURCELIST);

    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
    File frameworkDexFolder = TestTools.createTempDir("framework", "dex");
    toolchain.libToExe(frameworkJackZip, frameworkDexFolder, /* zipFile = */ false);
  }

  @Test
  public void guava() throws Exception {
    File guavaDexFolder = AbstractTestTools.createTempDir();
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToExe(GUAVA_JAR, guavaDexFolder, /* zipFile = */ false);
  }

  @Test
  public void services() throws Exception {
    File servicesDexFolder = AbstractTestTools.createTempDir();
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToExe(SERVICES_JAR, servicesDexFolder, /* zipFile = */ false);
  }

  @Test
  public void arity() throws Exception {
    File arityDexFolder = AbstractTestTools.createTempDir();
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToExe(ARITY_JAR, arityDexFolder, /* zipFile = */ false);
  }

  @Test
  public void playservices() throws Exception {
    File playServiceDexFolder = AbstractTestTools.createTempDir();
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.libToExe(PLAY_SERVICE_JAR, playServiceDexFolder, /* zipFile = */ false);
  }
}
