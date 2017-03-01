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

import org.junit.Test;

import javax.annotation.Nonnull;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.LegacyToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;



/**
 * JUnit test for Java 8 forked from GWT.
 */
public class GwtTest {

  private RuntimeTestInfo GWT_LAMBDA_TEST_1 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test001"),
      "com.android.jack.java8.gwt.test001.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_2 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test002"),
      "com.android.jack.java8.gwt.test002.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_3 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test003"),
      "com.android.jack.java8.gwt.test003.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_4 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test004"),
      "com.android.jack.java8.gwt.test004.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_5 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test005"),
      "com.android.jack.java8.gwt.test005.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_6 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test006"),
      "com.android.jack.java8.gwt.test006.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_7 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test007"),
      "com.android.jack.java8.gwt.test007.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_8 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test008"),
      "com.android.jack.java8.gwt.test008.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_9 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test009"),
      "com.android.jack.java8.gwt.test009.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_10 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test010"),
      "com.android.jack.java8.gwt.test010.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_11 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test011"),
      "com.android.jack.java8.gwt.test011.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_12 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test012"),
      "com.android.jack.java8.gwt.test012.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_13 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test013"),
      "com.android.jack.java8.gwt.test013.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_14 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test014"),
      "com.android.jack.java8.gwt.test014.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_15 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test015"),
      "com.android.jack.java8.gwt.test015.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_16 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test016"),
      "com.android.jack.java8.gwt.test016.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_17 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test017"),
      "com.android.jack.java8.gwt.test017.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_18 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test018"),
      "com.android.jack.java8.gwt.test018.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_19 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test019"),
      "com.android.jack.java8.gwt.test019.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_20 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test020"),
      "com.android.jack.java8.gwt.test020.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_21 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test021"),
      "com.android.jack.java8.gwt.test021.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_22 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test022"),
      "com.android.jack.java8.gwt.test022.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_23 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test023"),
      "com.android.jack.java8.gwt.test023.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_24 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test024"),
      "com.android.jack.java8.gwt.test024.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_25 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test025"),
      "com.android.jack.java8.gwt.test025.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_26 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test026"),
      "com.android.jack.java8.gwt.test026.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_27 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test027"),
      "com.android.jack.java8.gwt.test027.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_42 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test042"),
      "com.android.jack.java8.gwt.test042.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_43 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test043"),
      "com.android.jack.java8.gwt.test043.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_44 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test044"),
      "com.android.jack.java8.gwt.test044.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_45 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test045"),
      "com.android.jack.java8.gwt.test045.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_46 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test046"),
      "com.android.jack.java8.gwt.test046.jack.Java8Test");

  @Test
  @Runtime
  public void testLambdaNoCapture() throws Exception {
    run(GWT_LAMBDA_TEST_1);
  }

  @Test
  @Runtime
  public void testLambdaCaptureLocal() throws Exception {
    run(GWT_LAMBDA_TEST_2);
  }

  @Test
  @Runtime
  public void testLambdaCaptureLocalWithInnerClass() throws Exception {
    run(GWT_LAMBDA_TEST_3);
  }

  @Test
  @Runtime
  public void testLambdaCaptureLocalAndFieldWithInnerClass() throws Exception {
    run(GWT_LAMBDA_TEST_4);
  }

  @Test
  @Runtime
  public void testLambdaCaptureLocalAndField() throws Exception {
    run(GWT_LAMBDA_TEST_5);
  }

  @Test
  @Runtime
  public void testCompileLambdaCaptureOuterInnerField() throws Exception {
    run(GWT_LAMBDA_TEST_6);
  }

  @Test
  @Runtime
  public void testStaticReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_7);
  }

  @Test
  @Runtime
  public void testInstanceReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_8);
  }

  @Test
  @Runtime
  public void testImplicitQualifierReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_9);
  }

  @Test
  @Runtime
  public void testConstructorReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_10);
  }

  @Test
  @Runtime
  public void testStaticInterfaceMethod() throws Exception {
    run(GWT_LAMBDA_TEST_11);
  }

  @Test
  @Runtime
  public void testArrayConstructorReference() throws Exception {
    run(GWT_LAMBDA_TEST_12);
  }

  @Test
  @Runtime
  public void testArrayConstructorReferenceBoxed() throws Exception {
    run(GWT_LAMBDA_TEST_13);
  }

  @Test
  @Runtime
  public void testVarArgsReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_14);
  }

  @Test
  @Runtime
  public void testVarArgsPassthroughReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_15);
  }

  @Test
  @Runtime
  public void testVarArgsPassthroughReferenceBindingProvidedArray() throws Exception {
    run(GWT_LAMBDA_TEST_16);
  }

  @Test
  @Runtime
  public void testSuperReferenceExpression() throws Exception {
    run(GWT_LAMBDA_TEST_17);
  }

  @Test
  @Runtime
  public void testSuperReferenceExpressionWithVarArgs() throws Exception {
    run(GWT_LAMBDA_TEST_18);
  }

  @Test
  @Runtime
  public void testPrivateConstructorReference() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_19)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addIgnoredCandidateToolchain(JackApiV01.class)
    // This test must be exclude from the Jill tool-chain because it does not compile with it
    .addIgnoredCandidateToolchain(JillBasedToolchain.class)
    .addIgnoredCandidateToolchain(LegacyToolchain.class)
    .compileAndRunTest();
  }

  @Test
  @Runtime
  public void testLambdaCaptureParameter() throws Exception {
    run(GWT_LAMBDA_TEST_20);
  }

  @Test
  @Runtime
  public void testLambdaNestingCaptureLocal() throws Exception {
    run(GWT_LAMBDA_TEST_21);
  }

  @Test
  @Runtime
  public void testLambdaNestingCaptureField_InnerClassCapturingOuterClassVariable() throws Exception {
    run(GWT_LAMBDA_TEST_22);
  }

  @Test
  @Runtime
  public void testInnerClassCaptureLocalFromOuterLambda() throws Exception {
    run(GWT_LAMBDA_TEST_23);
  }

  @Test
  @Runtime
  public void testLambdaNestingCaptureField() throws Exception {
    run(GWT_LAMBDA_TEST_24);
  }

  @Test
  @Runtime
  public void testLambdaMultipleNestingCaptureFieldAndLocal() throws Exception {
    run(GWT_LAMBDA_TEST_25);
  }

  @Test
  @Runtime
  public void testLambdaMultipleNestingCaptureFieldAndLocalInnerClass() throws Exception {
    run(GWT_LAMBDA_TEST_26);
  }

  @Test
  @Runtime
  public void testMethodRefWithSameName() throws Exception {
    run(GWT_LAMBDA_TEST_27);
  }

  @Test
  @Runtime
  public void testLambdaNestingInAnonymousCaptureLocal() throws Exception {
    run(GWT_LAMBDA_TEST_42);
  }

  @Test
  @Runtime
  public void testLambdaNestingInMultipleMixedAnonymousCaptureLocal() throws Exception {
    run(GWT_LAMBDA_TEST_43);
  }

  @Test
  @Runtime
  public void testLambdaNestingInMultipleMixedAnonymousCaptureLocal_withInterference() throws Exception {
    run(GWT_LAMBDA_TEST_44);
  }

  @Test
  @Runtime
  public void testLambdaNestingInMultipleMixedAnonymousCaptureLocalAndField() throws Exception {
    run(GWT_LAMBDA_TEST_45);
  }

  @Test
  @Runtime
  public void testLambdaNestingInMultipleAnonymousCaptureLocal() throws Exception {
    run(GWT_LAMBDA_TEST_46);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .compileAndRunTest();
  }

}
