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

import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.category.SlowTests;
import com.android.jack.config.id.JavaVersionPropertyId.JavaVersion;

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

  private static String corePath;

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
        "/out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/" + "classes.zip");

    GUAVA_JAR = TestTools
        .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/guava_intermediates/classes.zip");
    SERVICES_JAR = TestTools
        .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/services_intermediates/"
            + "classes.zip");
    ARITY_JAR = TestTools
        .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/libarity_intermediates/"
            + "classes.zip");
    PLAY_SERVICE_JAR = TestTools
        .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/"
            + "google-play-services-first-party_intermediates/classes.zip");

    File coreOut = TestTools.createTempFile("core", ".zip");
    Options options = new Options();
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), JavaVersion.JAVA_7.toString());
    TestTools.compileSourceToJack(options, CORE_SOURCELIST, null, coreOut, true);
    corePath = coreOut.getAbsolutePath();
  }

  @Test
  public void ext() throws Exception {
    File extJack = TestTools.createTempFile("ext", ".zip");
    TestTools.compileSourceToJack(new Options(), EXT_SOURCELIST, corePath, extJack, true);

    File extFolder = TestTools.createTempDir("ext", "dex");
    TestTools.compileJackToDex(new Options(), extJack, extFolder, false);
  }

  @Test
  public void frameworkFromJack() throws Exception {

    File conscryptJack = TestTools.createTempFile("conscrypt", ".zip");
    TestTools.compileSourceToJack(new Options(), CONSCRYPT_SOURCELIST, corePath, conscryptJack,
        true);
    File conscryptRenamedJack = TestTools.createTempFile("conscryptrenamed", ".zip");
    File conscyptRules =
        new JarJarRules(TestTools.getFromAndroidTree("external/conscrypt/jarjar-rules.txt"));
    TestTools.jarjarJackToJack(
        new Options(), conscryptJack, null, conscryptRenamedJack, conscyptRules, true);

    File okhttpJack = TestTools.createTempFile("okkttp", ".zip");
    TestTools.compileSourceToJack(new Options(), OKHTTP_SOURCELIST,
        corePath + File.pathSeparatorChar + conscryptRenamedJack.getAbsolutePath(), okhttpJack,
        true);
    File okhttpRenamedJack = TestTools.createTempFile("okhttprenamed", ".zip");
    File okhttpRules =
        new JarJarRules(TestTools.getFromAndroidTree("external/okhttp/jarjar-rules.txt"));
    TestTools.jarjarJackToJack(
        new Options(), okhttpJack, null, okhttpRenamedJack, okhttpRules, true);

    File extJack = TestTools.createTempFile("ext", ".zip");
    TestTools.compileSourceToJack(new Options(), EXT_SOURCELIST, corePath, extJack, true);

    File bouncyCastleJack = TestTools.createTempFile("bouncyjack", ".zip");
    TestTools.compileSourceToJack(new Options(), BOUNCY_SOURCELIST, corePath, bouncyCastleJack,
        true);

    File coreJunitJack = TestTools.createTempFile("corejunitjack", ".zip");
    TestTools.compileSourceToJack(new Options(), CORE_JUNIT_SOURCELIST, corePath, coreJunitJack,
        true);

    File bouncyCastleRenamedJack = TestTools.createTempFile("bouncyjackrenamed", ".zip");
    File jarjarRules =
        new JarJarRules(TestTools.getFromAndroidTree("external/bouncycastle/jarjar-rules.txt"));
    TestTools.jarjarJackToJack(
        new Options(), bouncyCastleJack, null, bouncyCastleRenamedJack, jarjarRules, true);

    String classpath = corePath + File.pathSeparatorChar + conscryptRenamedJack.getAbsolutePath()
        + File.pathSeparatorChar + okhttpRenamedJack.getAbsolutePath()
        + File.pathSeparatorChar + extJack.getAbsolutePath()
        + File.pathSeparatorChar + bouncyCastleRenamedJack.getAbsolutePath()
        + File.pathSeparatorChar + coreJunitJack.getAbsolutePath();
    File frameworkJackZip = TestTools.createTempFile("framework", ".zip");
    Options options = new Options();
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), JavaVersion.JAVA_7.toString());
    TestTools.compileSourceToJack(
       options, FRAMEWORK_SOURCELIST, classpath, frameworkJackZip, true);

    Options dexOptions = new Options();
    dexOptions.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
    File frameworkDexFolder = TestTools.createTempDir("framework", "dex");
    TestTools.compileJackToDex(dexOptions, frameworkJackZip, frameworkDexFolder, false);
  }

  @Test
  public void guava() throws Exception {
    File guavaDexFolder = TestTools.createTempDir("guava", "dex");
    TestTools.compileJackToDex(new Options(), GUAVA_JAR, guavaDexFolder, false);
  }

  @Test
  public void services() throws Exception {
    File servicesDexFolder = TestTools.createTempDir("service", "dex");
    TestTools.compileJackToDex(new Options(), SERVICES_JAR, servicesDexFolder, false);
  }

  @Test
  public void arity() throws Exception {
    File arityDexFolder = TestTools.createTempDir("arity", "dex");
    TestTools.compileJackToDex(new Options(), ARITY_JAR, arityDexFolder, false);
  }

  @Test
  public void playservices() throws Exception {
    File playServiceDexFolder = TestTools.createTempDir("playservices", "dex");
    TestTools.compileJackToDex(new Options(), PLAY_SERVICE_JAR, playServiceDexFolder, false);
  }
}
