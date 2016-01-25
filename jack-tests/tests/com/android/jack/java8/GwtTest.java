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
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JillBasedToolchain;
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

  private RuntimeTestInfo GWT_LAMBDA_TEST_28 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test028"),
      "com.android.jack.java8.gwt.test028.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_29 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test029"),
      "com.android.jack.java8.gwt.test029.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_30 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test030"),
      "com.android.jack.java8.gwt.test030.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_31 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test031"),
      "com.android.jack.java8.gwt.test031.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_32 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test032"),
      "com.android.jack.java8.gwt.test032.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_33 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test033"),
      "com.android.jack.java8.gwt.test033.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_34 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test034"),
      "com.android.jack.java8.gwt.test034.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_35 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test035"),
      "com.android.jack.java8.gwt.test035.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_36 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test036"),
      "com.android.jack.java8.gwt.test036.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_37 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test037"),
      "com.android.jack.java8.gwt.test037.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_38 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test038"),
      "com.android.jack.java8.gwt.test038.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_39 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test039"),
      "com.android.jack.java8.gwt.test039.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_40 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test040"),
      "com.android.jack.java8.gwt.test040.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_41 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test041"),
      "com.android.jack.java8.gwt.test041.jack.Java8Test");

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

  private RuntimeTestInfo GWT_LAMBDA_TEST_47 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test047"),
      "com.android.jack.java8.gwt.test047.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_48 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test048"),
      "com.android.jack.java8.gwt.test048.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_49 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test049"),
      "com.android.jack.java8.gwt.test049.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_50 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test050"),
      "com.android.jack.java8.gwt.test050.jack.Java8Test");

  private RuntimeTestInfo GWT_LAMBDA_TEST_51 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.gwt.test051"),
      "com.android.jack.java8.gwt.test051.jack.Java8Test");

  @Test
  public void testLambdaNoCapture() throws Exception {
    run(GWT_LAMBDA_TEST_1);
  }

  @Test
  public void testLambdaCaptureLocal() throws Exception {
    run(GWT_LAMBDA_TEST_2);
  }

  @Test
  public void testLambdaCaptureLocalWithInnerClass() throws Exception {
    run(GWT_LAMBDA_TEST_3);
  }

  @Test
  public void testLambdaCaptureLocalAndFieldWithInnerClass() throws Exception {
    run(GWT_LAMBDA_TEST_4);
  }

  @Test
  public void testLambdaCaptureLocalAndField() throws Exception {
    run(GWT_LAMBDA_TEST_5);
  }

  @Test
  public void testCompileLambdaCaptureOuterInnerField() throws Exception {
    run(GWT_LAMBDA_TEST_6);
  }

  @Test
  public void testStaticReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_7);
  }

  @Test
  public void testInstanceReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_8);
  }

  @Test
  public void testImplicitQualifierReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_9);
  }

  @Test
  public void testConstructorReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_10);
  }

  @Test
  public void testStaticInterfaceMethod() throws Exception {
    run(GWT_LAMBDA_TEST_11);
  }

  @Test
  public void testArrayConstructorReference() throws Exception {
    run(GWT_LAMBDA_TEST_12);
  }

  @Test
  public void testArrayConstructorReferenceBoxed() throws Exception {
    run(GWT_LAMBDA_TEST_13);
  }

  @Test
  public void testVarArgsReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_14);
  }

  @Test
  public void testVarArgsPassthroughReferenceBinding() throws Exception {
    run(GWT_LAMBDA_TEST_15);
  }

  @Test
  public void testVarArgsPassthroughReferenceBindingProvidedArray() throws Exception {
    run(GWT_LAMBDA_TEST_16);
  }

  @Test
  public void testSuperReferenceExpression() throws Exception {
    run(GWT_LAMBDA_TEST_17);
  }

  @Test
  public void testSuperReferenceExpressionWithVarArgs() throws Exception {
    run(GWT_LAMBDA_TEST_18);
  }

  @Test
  public void testPrivateConstructorReference() throws Exception {
    run(GWT_LAMBDA_TEST_19);
  }

  @Test
  public void testLambdaCaptureParameter() throws Exception {
    run(GWT_LAMBDA_TEST_20);
  }

  @Test
  public void testLambdaNestingCaptureLocal() throws Exception {
    run(GWT_LAMBDA_TEST_21);
  }

  @Test
  public void testLambdaNestingCaptureField_InnerClassCapturingOuterClassVariable() throws Exception {
    run(GWT_LAMBDA_TEST_22);
  }

  @Test
  public void testInnerClassCaptureLocalFromOuterLambda() throws Exception {
    run(GWT_LAMBDA_TEST_23);
  }

  @Test
  public void testLambdaNestingCaptureField() throws Exception {
    run(GWT_LAMBDA_TEST_24);
  }

  @Test
  public void testLambdaMultipleNestingCaptureFieldAndLocal() throws Exception {
    run(GWT_LAMBDA_TEST_25);
  }

  @Test
  public void testLambdaMultipleNestingCaptureFieldAndLocalInnerClass() throws Exception {
    run(GWT_LAMBDA_TEST_26);
  }

  @Test
  public void testMethodRefWithSameName() throws Exception {
    run(GWT_LAMBDA_TEST_27);
  }

  @Test
  public void testDefaultInterfaceMethod() throws Exception {
    run(GWT_LAMBDA_TEST_28);
  }

  @Test
  @KnownIssue
  public void testDefaultInterfaceMethodVirtualUpRef() throws Exception {
    run(GWT_LAMBDA_TEST_29);
  }

  @Test
  public void DefaultInterfaceImplVirtualUpRefTwoInterfaces() throws Exception {
    run(GWT_LAMBDA_TEST_30);
  }

  @Test
  public void testDefenderMethodByInterfaceInstance() throws Exception {
    run(GWT_LAMBDA_TEST_31);
  }

  @Test
  public void testDefaultMethodReference() throws Exception {
    run(GWT_LAMBDA_TEST_32);
  }

  @Test
  public void testThisRefInDefenderMethod() throws Exception {
    run(GWT_LAMBDA_TEST_33);
  }

  @Test
  public void testClassImplementsTwoInterfacesWithSameDefenderMethod() throws Exception {
    run(GWT_LAMBDA_TEST_34);
  }

  @Test
  public void testAbstractClassImplementsInterface() throws Exception {
    run(GWT_LAMBDA_TEST_35);
  }

  @Test
  public void testSuperRefInDefenderMethod() throws Exception {
    run(GWT_LAMBDA_TEST_36);
  }

  @Test
  public void testSuperThisRefsInDefenderMethod() throws Exception {
    run(GWT_LAMBDA_TEST_37);
  }

  @Test
  public void testNestedInterfaceClass() throws Exception {
    run(GWT_LAMBDA_TEST_38);
  }

  @Test
  public void testBaseIntersectionCast() throws Exception {
    run(GWT_LAMBDA_TEST_39);
  }

  @Test
  public void testIntersectionCastWithLambdaExpr() throws Exception {
    run(GWT_LAMBDA_TEST_40);
  }

  @Test
  public void testIntersectionCastPolymorphism() throws Exception {
    run(GWT_LAMBDA_TEST_41);
  }

  @Test
  public void testLambdaNestingInAnonymousCaptureLocal() throws Exception {
    run(GWT_LAMBDA_TEST_42);
  }

  @Test
  public void testLambdaNestingInMultipleMixedAnonymousCaptureLocal() throws Exception {
    run(GWT_LAMBDA_TEST_43);
  }

  @Test
  public void testLambdaNestingInMultipleMixedAnonymousCaptureLocal_withInterference() throws Exception {
    run(GWT_LAMBDA_TEST_44);
  }

  @Test
  public void testLambdaNestingInMultipleMixedAnonymousCaptureLocalAndField() throws Exception {
    run(GWT_LAMBDA_TEST_45);
  }

  @Test
  public void testLambdaNestingInMultipleAnonymousCaptureLocal() throws Exception {
    run(GWT_LAMBDA_TEST_46);
  }

  @Test
  public void testMultipleDefaults_fromInterfaces_left() throws Exception {
    run(GWT_LAMBDA_TEST_47);
  }

  @Test
  public void testMultipleDefaults_fromInterfaces_right() throws Exception {
    run(GWT_LAMBDA_TEST_48);
  }

  @Test
  public void testMultipleDefaults_superclass_left() throws Exception {
    run(GWT_LAMBDA_TEST_49);
  }

  @Test
  public void testMultipleDefaults_superclass_right() throws Exception {
    run(GWT_LAMBDA_TEST_50);
  }

  @Test
  public void testInterfaceThis() throws Exception {
    run(GWT_LAMBDA_TEST_51);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .compileAndRunTest();
  }

}
