/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.java8;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.junit.RuntimeVersion;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;
import com.android.jack.util.AndroidApiLevel;

import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;


/**
 * JUnit tests imported from RetroLambda.
 */
public class RetroLambdaTests {

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_001 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test001"),
          "com.android.jack.java8.retrolambda.defaultmethods.test001.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_002 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test002"),
          "com.android.jack.java8.retrolambda.defaultmethods.test002.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_003 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test003"),
          "com.android.jack.java8.retrolambda.defaultmethods.test003.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_004 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test004"),
          "com.android.jack.java8.retrolambda.defaultmethods.test004.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_005 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test005"),
          "com.android.jack.java8.retrolambda.defaultmethods.test005.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_006 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test006"),
          "com.android.jack.java8.retrolambda.defaultmethods.test006.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_007 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test007"),
          "com.android.jack.java8.retrolambda.defaultmethods.test007.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_008 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test008"),
          "com.android.jack.java8.retrolambda.defaultmethods.test008.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_009 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test009"),
          "com.android.jack.java8.retrolambda.defaultmethods.test009.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_010 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test010"),
          "com.android.jack.java8.retrolambda.defaultmethods.test010.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_011 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test011"),
          "com.android.jack.java8.retrolambda.defaultmethods.test011.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_012 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test012"),
          "com.android.jack.java8.retrolambda.defaultmethods.test012.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_013 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test013"),
          "com.android.jack.java8.retrolambda.defaultmethods.test013.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_014 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test014"),
          "com.android.jack.java8.retrolambda.defaultmethods.test014.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_015 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test015"),
          "com.android.jack.java8.retrolambda.defaultmethods.test015.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_016 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test016"),
          "com.android.jack.java8.retrolambda.defaultmethods.test016.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_017 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test017"),
          "com.android.jack.java8.retrolambda.defaultmethods.test017.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_018 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test018"),
          "com.android.jack.java8.retrolambda.defaultmethods.test018.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_019 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test019"),
          "com.android.jack.java8.retrolambda.defaultmethods.test019.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_020 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test020"),
          "com.android.jack.java8.retrolambda.defaultmethods.test020.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_021 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test021"),
          "com.android.jack.java8.retrolambda.defaultmethods.test021.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_022 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test022"),
          "com.android.jack.java8.retrolambda.defaultmethods.test022.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_023 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test023"),
          "com.android.jack.java8.retrolambda.defaultmethods.test023.jack.Tests")
      .addCandidateExtraSources(
          new File(AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.anotherpackage.jack"),
              "UsesLambdasInAnotherPackage.java"));

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_024 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test024"),
          "com.android.jack.java8.retrolambda.defaultmethods.test024.jack.Tests")
      .addCandidateExtraSources(
          new File(AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.anotherpackage.jack"),
              "UsesLambdasInAnotherPackage.java"))
      .addCandidateExtraSources(
          new File(AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.inmainsources.jack"),
              "InMainSources.java"));


  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_025 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test025"),
          "com.android.jack.java8.retrolambda.defaultmethods.test025.jack.Tests")
      .addCandidateExtraSources(
          new File(AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.anotherpackage.jack"),
              "UsesLambdasInAnotherPackage.java"))
      .addCandidateExtraSources(
          new File(AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.inmainsources.jack"),
              "InMainSources.java"));

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_026 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test026"),
          "com.android.jack.java8.retrolambda.defaultmethods.test026.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_DEFAULTMETHODS_027 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.defaultmethods.test027"),
          "com.android.jack.java8.retrolambda.defaultmethods.test027.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_INTERFACE_BRIDGE_METHODS_001 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.interfacebridgemethods.test001"),
          "com.android.jack.java8.retrolambda.interfacebridgemethods.test001.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_INTERFACE_STATICMETHODS_001 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.interfacestaticmethods.test001"),
          "com.android.jack.java8.retrolambda.interfacestaticmethods.test001.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_INTERFACE_STATICMETHODS_002 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.interfacestaticmethods.test002"),
          "com.android.jack.java8.retrolambda.interfacestaticmethods.test002.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_INTERFACE_STATICMETHODS_003 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.interfacestaticmethods.test003"),
          "com.android.jack.java8.retrolambda.interfacestaticmethods.test003.jack.Tests");

  private RuntimeTestInfo RETROLAMBDA_INTERFACE_STATICMETHODS_004 =
      new RuntimeTestInfo(
          AbstractTestTools.getTestRootDir("com.android.jack.java8.retrolambda.interfacestaticmethods.test004"),
          "com.android.jack.java8.retrolambda.interfacestaticmethods.test004.jack.Tests");

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest001() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_001);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest002() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_002);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest003() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_003);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest004() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_004);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest005() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_005);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest006() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_006);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest007() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_007);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest008() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_008);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest009() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_009);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest010() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_010);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest011() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_011);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest012() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_012);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest013() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_013);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest014() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_014);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest015() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_015);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest016() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_016);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest017() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_017);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest018() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_018);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest019() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_019);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest020() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_020);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest021() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_021);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest022() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_022);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest023() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_023);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest024() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_024);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest025() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_025);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest026() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_026);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaDefaultMethodsTest027() throws Exception {
    run(RETROLAMBDA_DEFAULTMETHODS_027);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaInterfaceBridgeMethodsTest001() throws Exception {
    run(RETROLAMBDA_INTERFACE_BRIDGE_METHODS_001);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaInterfaceStaticMethodsTest001() throws Exception {
    run(RETROLAMBDA_INTERFACE_STATICMETHODS_001);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaInterfaceStaticMethodsTest002() throws Exception {
    run(RETROLAMBDA_INTERFACE_STATICMETHODS_002);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaInterfaceStaticMethodsTest003() throws Exception {
    run(RETROLAMBDA_INTERFACE_STATICMETHODS_003);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void retroLambdaInterfaceStaticMethodsTest004() throws Exception {
    run(RETROLAMBDA_INTERFACE_STATICMETHODS_004);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti)
        .setSourceLevel(SourceLevel.JAVA_8)
        .setAndroidMinApiLevel(String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()))
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .compileAndRunTest();
  }

}
