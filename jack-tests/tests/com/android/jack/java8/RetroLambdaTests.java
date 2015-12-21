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
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.Test;

import java.io.File;


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
  public void retroLambdaDefaultMethodsTest001() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_001)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest002() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_002)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest003() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_003)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest004() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_004)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest005() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_005)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest006() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_006)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest007() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_007)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest008() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_008)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest009() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_009)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest010() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_010)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest011() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_011)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest012() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_012)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest013() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_013)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest014() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_014)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest015() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_015)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest016() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_016)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest017() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_017)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest018() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_018)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest019() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_019)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest020() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_020)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest021() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_021)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest022() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_022)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest023() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_023)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest024() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_024)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest025() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_025)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest026() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_026)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaDefaultMethodsTest027() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_DEFAULTMETHODS_027)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaInterfaceBridgeMethodsTest001() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_INTERFACE_BRIDGE_METHODS_001)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaInterfaceStaticMethodsTest001() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_INTERFACE_STATICMETHODS_001)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaInterfaceStaticMethodsTest002() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_INTERFACE_STATICMETHODS_002)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaInterfaceStaticMethodsTest003() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_INTERFACE_STATICMETHODS_003)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void retroLambdaInterfaceStaticMethodsTest004() throws Exception {
    new RuntimeTestHelper(RETROLAMBDA_INTERFACE_STATICMETHODS_004)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

}