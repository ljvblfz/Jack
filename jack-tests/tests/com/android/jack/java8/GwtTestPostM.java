/*
 * Copyright (C) 2016 The Android Open Source Project
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

import com.android.jack.Options;
import com.android.jack.backend.dex.compatibility.AndroidCompatibilityChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.junit.RuntimeVersion;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.Test;

import javax.annotation.Nonnull;



/**
 * JUnit test for Java 8 forked from GWT. These tests require a post N runtime.
 */
public class GwtTestPostM {

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
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultInterfaceMethod() throws Exception {
    run(GWT_LAMBDA_TEST_28);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  @KnownIssue
  public void testDefaultInterfaceMethodVirtualUpRef() throws Exception {
    run(GWT_LAMBDA_TEST_29);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void DefaultInterfaceImplVirtualUpRefTwoInterfaces() throws Exception {
    run(GWT_LAMBDA_TEST_30);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefenderMethodByInterfaceInstance() throws Exception {
    run(GWT_LAMBDA_TEST_31);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testDefaultMethodReference() throws Exception {
    run(GWT_LAMBDA_TEST_32);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testThisRefInDefenderMethod() throws Exception {
    run(GWT_LAMBDA_TEST_33);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testClassImplementsTwoInterfacesWithSameDefenderMethod() throws Exception {
    run(GWT_LAMBDA_TEST_34);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testAbstractClassImplementsInterface() throws Exception {
    run(GWT_LAMBDA_TEST_35);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testSuperRefInDefenderMethod() throws Exception {
    run(GWT_LAMBDA_TEST_36);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testSuperThisRefsInDefenderMethod() throws Exception {
    run(GWT_LAMBDA_TEST_37);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testNestedInterfaceClass() throws Exception {
    run(GWT_LAMBDA_TEST_38);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testBaseIntersectionCast() throws Exception {
    run(GWT_LAMBDA_TEST_39);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testIntersectionCastWithLambdaExpr() throws Exception {
    run(GWT_LAMBDA_TEST_40);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testIntersectionCastPolymorphism() throws Exception {
    run(GWT_LAMBDA_TEST_41);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testMultipleDefaults_fromInterfaces_left() throws Exception {
    run(GWT_LAMBDA_TEST_47);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testMultipleDefaults_fromInterfaces_right() throws Exception {
    run(GWT_LAMBDA_TEST_48);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testMultipleDefaults_superclass_left() throws Exception {
    run(GWT_LAMBDA_TEST_49);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testMultipleDefaults_superclass_right() throws Exception {
    run(GWT_LAMBDA_TEST_50);
  }

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testInterfaceThis() throws Exception {
    run(GWT_LAMBDA_TEST_51);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti).setSourceLevel(SourceLevel.JAVA_8)
        .addProperty(Options.ANDROID_MIN_API_LEVEL.getName(),
            String.valueOf(AndroidCompatibilityChecker.N_API_LEVEL))
        .compileAndRunTest();
  }

}
