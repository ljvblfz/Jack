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
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;

/**
 * JUnit test for compilation of lambda expressions.
 */
public class LambdaTest {

  private RuntimeTestInfo LAMBDA001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test001"),
      "com.android.jack.java8.lambda.test001.jack.Tests");

  private RuntimeTestInfo LAMBDA002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test002"),
      "com.android.jack.java8.lambda.test002.jack.Tests");

  private RuntimeTestInfo LAMBDA003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test003"),
      "com.android.jack.java8.lambda.test003.jack.Tests");

  private RuntimeTestInfo LAMBDA004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test004"),
      "com.android.jack.java8.lambda.test004.jack.Tests");

  private RuntimeTestInfo LAMBDA005 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test005"),
      "com.android.jack.java8.lambda.test005.jack.Tests");

  private RuntimeTestInfo LAMBDA006 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test006"),
      "com.android.jack.java8.lambda.test006.jack.Tests");

  private RuntimeTestInfo LAMBDA007 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test007"),
      "com.android.jack.java8.lambda.test007.jack.Tests");

  private RuntimeTestInfo LAMBDA008 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test008"),
      "com.android.jack.java8.lambda.test008.jack.Tests");

  private RuntimeTestInfo LAMBDA009 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test009"),
      "com.android.jack.java8.lambda.test009.jack.Tests");

  private RuntimeTestInfo LAMBDA010 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test010"),
      "com.android.jack.java8.lambda.test010.jack.Tests")
          .addProguardFlagsFileName("proguard.flags");

  private RuntimeTestInfo LAMBDA011 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test011"),
      "com.android.jack.java8.lambda.test011.jack.Tests");

  private RuntimeTestInfo LAMBDA012 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test012"),
      "com.android.jack.java8.lambda.test012.jack.Tests");

  private RuntimeTestInfo LAMBDA013 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test013"),
      "com.android.jack.java8.lambda.test013.jack.Tests");

  private RuntimeTestInfo LAMBDA014 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test014"),
      "com.android.jack.java8.lambda.test014.jack.Tests");

  private RuntimeTestInfo LAMBDA015 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test015"),
      "com.android.jack.java8.lambda.test015.jack.Tests");

  private RuntimeTestInfo LAMBDA016 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test016"),
      "com.android.jack.java8.lambda.test016.jack.Tests");

  private RuntimeTestInfo LAMBDA017 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test017"),
      "com.android.jack.java8.lambda.test017.jack.Tests");

  private RuntimeTestInfo LAMBDA018 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test018"),
      "com.android.jack.java8.lambda.test018.jack.Tests");

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  public void testCompile(@Nonnull String testRootDir) throws Exception {
    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain();
    toolchain.setSourceLevel(SourceLevel.JAVA_8)
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir(testRootDir));
  }

  @Test
  public void testLamba001() throws Exception {
    new RuntimeTestHelper(LAMBDA001)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba002() throws Exception {
    new RuntimeTestHelper(LAMBDA002)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba003() throws Exception {
    new RuntimeTestHelper(LAMBDA003)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba004() throws Exception {
    new RuntimeTestHelper(LAMBDA004)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba005() throws Exception {
    new RuntimeTestHelper(LAMBDA005)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba006() throws Exception {
    new RuntimeTestHelper(LAMBDA006)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba007() throws Exception {
    new RuntimeTestHelper(LAMBDA007)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba008() throws Exception {
    new RuntimeTestHelper(LAMBDA008)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba009() throws Exception {
    new RuntimeTestHelper(LAMBDA009)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba010() throws Exception {
    new RuntimeTestHelper(LAMBDA010)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba011() throws Exception {
    new RuntimeTestHelper(LAMBDA011)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba012() throws Exception {
    new RuntimeTestHelper(LAMBDA012)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba013() throws Exception {
    new RuntimeTestHelper(LAMBDA013)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba014() throws Exception {
    new RuntimeTestHelper(LAMBDA014)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba015() throws Exception {
    new RuntimeTestHelper(LAMBDA015)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba016() throws Exception {
    new RuntimeTestHelper(LAMBDA016)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba017() throws Exception {
    new RuntimeTestHelper(LAMBDA017)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

  @Test
  public void testLamba018() throws Exception {
    new RuntimeTestHelper(LAMBDA018)
    .setSourceLevel(SourceLevel.JAVA_8)
    .compileAndRunTest();
  }

}
