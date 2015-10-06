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

import com.android.jack.Options;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
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
    new RuntimeTestHelper(GWT_LAMBDA_TEST_1)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaCaptureLocal() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_2)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaCaptureLocalWithInnerClass() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_3)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaCaptureLocalAndFieldWithInnerClass() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_4)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaCaptureLocalAndField() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_5)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testCompileLambdaCaptureOuterInnerField() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_6)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testStaticReferenceBinding() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_7)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testInstanceReferenceBinding() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_8)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testImplicitQualifierReferenceBinding() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_9)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testConstructorReferenceBinding() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_10)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testStaticInterfaceMethod() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_11)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testArrayConstructorReference() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_12)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testArrayConstructorReferenceBoxed() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_13)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testVarArgsReferenceBinding() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_14)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testVarArgsPassthroughReferenceBinding() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_15)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testVarArgsPassthroughReferenceBindingProvidedArray() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_16)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testSuperReferenceExpression() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_17)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testSuperReferenceExpressionWithVarArgs() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_18)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testPrivateConstructorReference() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_19)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaCaptureParameter() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_20)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaNestingCaptureLocal() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_21)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaNestingCaptureField_InnerClassCapturingOuterClassVariable() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_22)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testInnerClassCaptureLocalFromOuterLambda() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_23)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaNestingCaptureField() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_24)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaMultipleNestingCaptureFieldAndLocal() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_25)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaMultipleNestingCaptureFieldAndLocalInnerClass() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_26)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testMethodRefWithSameName() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_27)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testDefaultInterfaceMethod() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_28)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  @KnownIssue
  public void testDefaultInterfaceMethodVirtualUpRef() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_29)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void DefaultInterfaceImplVirtualUpRefTwoInterfaces() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_30)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testDefenderMethodByInterfaceInstance() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_31)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testDefaultMethodReference() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_32)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testThisRefInDefenderMethod() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_33)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testClassImplementsTwoInterfacesWithSameDefenderMethod() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_34)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testAbstractClassImplementsInterface() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_35)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  @KnownIssue
  public void testSuperRefInDefenderMethod() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_36)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testSuperThisRefsInDefenderMethod() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_37)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testNestedInterfaceClass() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_38)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  @KnownIssue
  public void testBaseIntersectionCast() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_39)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  @KnownIssue
  public void testIntersectionCastWithLambdaExpr() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_40)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  @KnownIssue
  public void testIntersectionCastPolymorphism() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_41)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaNestingInAnonymousCaptureLocal() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_42)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaNestingInMultipleMixedAnonymousCaptureLocal() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_43)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaNestingInMultipleMixedAnonymousCaptureLocal_withInterference() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_44)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaNestingInMultipleMixedAnonymousCaptureLocalAndField() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_45)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testLambdaNestingInMultipleAnonymousCaptureLocal() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_46)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testMultipleDefaults_fromInterfaces_left() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_47)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testMultipleDefaults_fromInterfaces_right() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_48)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testMultipleDefaults_superclass_left() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_49)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testMultipleDefaults_superclass_right() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_50)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }

  @Test
  public void testInterfaceThis() throws Exception {
    new RuntimeTestHelper(GWT_LAMBDA_TEST_51)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .compileAndRunTest();
  }
}
