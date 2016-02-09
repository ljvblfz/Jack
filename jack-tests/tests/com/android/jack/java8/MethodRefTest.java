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
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JillBasedToolchain;
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

  @Test
  public void testMethodRef001() throws Exception {
    run(METHODREF001);
  }

  @Test
  public void testMethodRef002() throws Exception {
    run(METHODREF002);
  }

  @Test
  public void testMethodRef003() throws Exception {
    run(METHODREF003);
  }

  @Test
  public void testMethodRef004() throws Exception {
    run(METHODREF004);
  }

  @Test
  public void testMethodRef005() throws Exception {
    run(METHODREF005);
  }

  @Test
  public void testMethodRef006() throws Exception {
    run(METHODREF006);
  }

  @Test
  public void testMethodRef007() throws Exception {
    run(METHODREF007);
  }

  @Test
  public void testMethodRef008() throws Exception {
    run(METHODREF008);
  }

  @Test
  public void testMethodRef009() throws Exception {
    run(METHODREF009);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .compileAndRunTest();
  }

}
