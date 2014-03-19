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

import com.android.jack.category.SlowTests;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

@Category(SlowTests.class)
public class NoClasspathTest {

  private static final Sourcelist CORE_SOURCELIST = TestTools.getTargetLibSourcelist("core");

  private static final Sourcelist BOUNCY_SOURCELIST = TestTools.getTargetLibSourcelist("bouncycastle");

  private static final Sourcelist EXT_SOURCELIST = TestTools.getTargetLibSourcelist("ext");

  private static final Sourcelist CORE_JUNIT_SOURCELIST =
      TestTools.getTargetLibSourcelist("core-junit");

  private static final Sourcelist FRAMEWORK_SOURCELIST =
      TestTools.getTargetLibSourcelist("framework");

  protected static final File FRAMEWORK_JAR = TestTools
      .getFromAndroidTree("/out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/"
          + "classes.jar");

  private static final File GUAVA_JAR = TestTools
      .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/guava_intermediates/"
          + "classes.jar");

  private static final File SERVICES_JAR = TestTools
      .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/services_intermediates/"
          + "classes.jar");

  private static final File ARITY_JAR = TestTools
      .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/libarity_intermediates/"
          + "classes.jar");

  private static final File PLAY_SERVICE_JAR = TestTools
      .getFromAndroidTree("out/target/common/obj/JAVA_LIBRARIES/google-play-services-first-party_intermediates/"
          + "classes.jar");

  private static String corePath;

  @BeforeClass
  public static void setup() throws Exception {
    File coreOut = TestTools.createTempFile("core", ".zip");
    TestTools.compileSourceToJack(new Options(), CORE_SOURCELIST, null, coreOut, true);
    corePath = coreOut.getAbsolutePath();
  }

  @Test
  public void ext() throws Exception {
    File extJack = TestTools.createTempFile("ext", ".zip");
    TestTools.compileSourceToJack(new Options(), EXT_SOURCELIST, corePath, extJack, true);

    File ext = TestTools.createTempFile("ext", ".dex");
    TestTools.compileJackToDex(new Options(), extJack, ext, false);
  }

  @Test
  public void frameworkFromJack() throws Exception {
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

    String classpath = corePath + File.pathSeparatorChar + extJack.getAbsolutePath()
        + File.pathSeparatorChar + bouncyCastleRenamedJack.getAbsolutePath()
        + File.pathSeparatorChar + coreJunitJack.getAbsolutePath();
    File frameworkJackZip = TestTools.createTempFile("framework", ".zip");
    TestTools.compileSourceToJack(
        new Options(), FRAMEWORK_SOURCELIST, classpath, frameworkJackZip, true);

    File frameworkDex = TestTools.createTempFile("framework", ".dex");
    TestTools.compileJackToDex(new Options(), frameworkJackZip, frameworkDex, false);
  }

  @Test
  public void guava() throws Exception {
    File guavaDex = TestTools.createTempFile("guava", ".dex");
    TestTools.compileJackToDex(new Options(), GUAVA_JAR, guavaDex, false);
  }

  @Test
  public void services() throws Exception {
    File servicesDex = TestTools.createTempFile("service", ".dex");
    TestTools.compileJackToDex(new Options(), SERVICES_JAR, servicesDex, false);
  }

  @Test
  public void arity() throws Exception {
    File arityDex = TestTools.createTempFile("arity", ".dex");
    TestTools.compileJackToDex(new Options(), ARITY_JAR, arityDex, false);
  }

  @Test
  public void playservices() throws Exception {
    File playServiceDex = TestTools.createTempFile("playservices", ".dex");
    TestTools.compileJackToDex(new Options(), PLAY_SERVICE_JAR, playServiceDex, false);
  }
}
