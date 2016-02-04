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
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JackCliToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import junit.framework.Assert;


/**
 * JUnit test for compilation of intersection type tests.
 */
public class IntersectionTypeTest {

  private RuntimeTestInfo INTERSECTION_TYPE_001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.intersectiontype.test001"),
      "com.android.jack.java8.intersectiontype.test001.jack.Tests");

  private RuntimeTestInfo INTERSECTION_TYPE_002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.intersectiontype.test002"),
      "com.android.jack.java8.intersectiontype.test002.jack.Tests");

  private RuntimeTestInfo INTERSECTION_TYPE_003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.intersectiontype.test003"),
      "com.android.jack.java8.intersectiontype.test003.jack.Tests")
          .addProguardFlagsFileName("proguard.flags");

  private RuntimeTestInfo INTERSECTION_TYPE_004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.intersectiontype.test004"),
      "com.android.jack.java8.intersectiontype.test004.jack.Tests");

  private RuntimeTestInfo INTERSECTION_TYPE_005 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.intersectiontype.test005"),
      "com.android.jack.java8.intersectiontype.test005.jack.Tests");

  @Test
  public void testIntersectionType001() throws Exception {
    new RuntimeTestHelper(INTERSECTION_TYPE_001)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .addIgnoredCandidateToolchain(JillBasedToolchain.class)
    .addIgnoredCandidateToolchain(JackApiV01.class)
    .compileAndRunTest();
  }

  @Test
  public void testIntersectionType002() throws Exception {
    new RuntimeTestHelper(INTERSECTION_TYPE_002)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .addIgnoredCandidateToolchain(JillBasedToolchain.class)
    .addIgnoredCandidateToolchain(JackApiV01.class)
    .compileAndRunTest();
  }

  @Test
  public void testIntersectionType003() throws Exception {
    new RuntimeTestHelper(INTERSECTION_TYPE_003)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
    .addIgnoredCandidateToolchain(JillBasedToolchain.class)
    .addIgnoredCandidateToolchain(JackApiV01.class)
    .compileAndRunTest();
  }

  @Test
  public void testIntersectionType004() throws Exception {
    try {
      new RuntimeTestHelper(INTERSECTION_TYPE_004).setSourceLevel(SourceLevel.JAVA_8)
          .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
          .addIgnoredCandidateToolchain(JackCliToolchain.class)
          .addIgnoredCandidateToolchain(JackApiV01.class)
          .compileAndRunTest();
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Compilation error is ok
    }
  }

  @Test
  public void testIntersectionType005() throws Exception {
    try {
      new RuntimeTestHelper(INTERSECTION_TYPE_005).setSourceLevel(SourceLevel.JAVA_8)
          .addProperty(Options.LAMBDA_TO_ANONYMOUS_CONVERTER.getName(), Boolean.TRUE.toString())
          .addIgnoredCandidateToolchain(JackCliToolchain.class)
          .addIgnoredCandidateToolchain(JackApiV01.class)
          .compileAndRunTest();
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Compilation error is ok
    }
  }
}
