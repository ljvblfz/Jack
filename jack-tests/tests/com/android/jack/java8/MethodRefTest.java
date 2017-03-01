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
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.LegacyToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.Test;

import javax.annotation.Nonnull;


/**
 * JUnit test for compilation of method references.
 */
public class MethodRefTest {

  private RuntimeTestInfo METHODREF001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test001"),
      "com.android.jack.java8.methodref.test001.jack.Tests");

  private RuntimeTestInfo METHODREF002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test002"),
      "com.android.jack.java8.methodref.test002.jack.Tests");

  private RuntimeTestInfo METHODREF003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test003"),
      "com.android.jack.java8.methodref.test003.jack.Tests");

  private RuntimeTestInfo METHODREF004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test004"),
      "com.android.jack.java8.methodref.test004.jack.Tests");

  private RuntimeTestInfo METHODREF005 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test005"),
      "com.android.jack.java8.methodref.test005.jack.Tests");

  private RuntimeTestInfo METHODREF006 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test006"),
      "com.android.jack.java8.methodref.test006.jack.Tests");

  private RuntimeTestInfo METHODREF007 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test007"),
      "com.android.jack.java8.methodref.test007.jack.Tests");

  private RuntimeTestInfo METHODREF008 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test008"),
      "com.android.jack.java8.methodref.test008.jack.Tests");

  private RuntimeTestInfo METHODREF009 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test009"),
      "com.android.jack.java8.methodref.test009.jack.Tests");

  private RuntimeTestInfo METHODREF010 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test010"),
      "com.android.jack.java8.methodref.test010.jack.Tests");

  private RuntimeTestInfo METHODREF011 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test011"),
      "com.android.jack.java8.methodref.test011.jack.Tests");

  private RuntimeTestInfo METHODREF012 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test012"),
      "com.android.jack.java8.methodref.test012.jack.Tests");

  private RuntimeTestInfo METHODREF013 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test013"),
      "com.android.jack.java8.methodref.test013.jack.Tests");

  private RuntimeTestInfo METHODREF014 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test014"),
      "com.android.jack.java8.methodref.test014.jack.Tests");

  private RuntimeTestInfo METHODREF015 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.methodref.test015"),
      "com.android.jack.java8.methodref.test015.jack.Tests");

  @Test
  @Runtime
  public void testMethodRef001() throws Exception {
    run(METHODREF001);
  }

  @Test
  @Runtime
  public void testMethodRef002() throws Exception {
    run(METHODREF002);
  }

  @Test
  @Runtime
  public void testMethodRef003() throws Exception {
    run(METHODREF003);
  }

  @Test
  @Runtime
  public void testMethodRef004() throws Exception {
    run(METHODREF004);
  }

  @Test
  @Runtime
  public void testMethodRef005() throws Exception {
    run(METHODREF005);
  }

  @Test
  @Runtime
  public void testMethodRef006() throws Exception {
    run(METHODREF006);
  }

  @Test
  @Runtime
  public void testMethodRef007() throws Exception {
    run(METHODREF007);
  }

  @Test
  @Runtime
  public void testMethodRef008() throws Exception {
    run(METHODREF008);
  }

  @Test
  @Runtime
  public void testMethodRef009() throws Exception {
    run(METHODREF009);
  }

  @Test
  @Runtime
  public void testMethodRef010() throws Exception {
    run(METHODREF010);
  }

  @Test
  @Runtime
  public void testMethodRef011() throws Exception {
    run(METHODREF011);
  }

  @Test
  @Runtime
  public void testMethodRef012() throws Exception {
    run(METHODREF012);
  }

  @Test
  @Runtime
  public void testMethodRef013() throws Exception {
    run(METHODREF013);
  }

  @Test
  @Runtime
  public void testMethodRef014() throws Exception {
    new RuntimeTestHelper(METHODREF014)
        .setSourceLevel(SourceLevel.JAVA_8)
        // This test must be exclude from the Jill tool-chain because it does not compile with it
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .addIgnoredCandidateToolchain(LegacyToolchain.class)
        .compileAndRunTest();
  }

  @Test
  @Runtime
  public void testMethodRef015() throws Exception {
    run(METHODREF015);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .compileAndRunTest();
  }

}
