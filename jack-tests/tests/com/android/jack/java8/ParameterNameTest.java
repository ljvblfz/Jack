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

import com.android.jack.TestTools;
import com.android.jack.backend.dex.annotations.ParameterMetadataAnnotationsAdder;
import com.android.jack.test.helper.FileChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.junit.RuntimeVersion;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.LegacyBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import junit.framework.Assert;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.StringIdItem;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;


/**
 * JUnit test for parameter metadata.
 */
public class ParameterNameTest {

  private File extraJavaFile =
      new File(AbstractTestTools.getTestRootDir("com.android.jack.java8.parameter.common"),
          "ParameterTestModifier.java");

  private RuntimeTestInfo PARAMETER001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.parameter.test001"),
      "com.android.jack.java8.parameter.test001.Tests").setSrcDirName("")
          .addCandidateExtraSources(extraJavaFile);

  private RuntimeTestInfo PARAMETER002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.parameter.test002"),
      "com.android.jack.java8.parameter.test002.Tests").setSrcDirName("")
          .addCandidateExtraSources(extraJavaFile);

  private RuntimeTestInfo PARAMETER003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.parameter.test003"),
      "com.android.jack.java8.parameter.test003.Tests").setSrcDirName("")
          .addCandidateExtraSources(extraJavaFile);

  private RuntimeTestInfo PARAMETER004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.parameter.test004"),
      "com.android.jack.java8.parameter.test004.Tests").setSrcDirName("")
          .addCandidateExtraSources(extraJavaFile);

  private RuntimeTestInfo PARAMETER005 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.parameter.test005"),
      "com.android.jack.java8.parameter.test005.Tests").setSrcDirName("")
          .addCandidateExtraSources(extraJavaFile);

  private RuntimeTestInfo PARAMETER006 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.parameter.test006"),
      "com.android.jack.java8.parameter.test006.Tests").setSrcDirName("")
          .addCandidateExtraSources(extraJavaFile);

  private RuntimeTestInfo PARAMETER007 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.parameter.test007"),
      "com.android.jack.java8.parameter.test007.Tests").setSrcDirName("")
          .addCandidateExtraSources(extraJavaFile);

  private RuntimeTestInfo PARAMETER008 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.parameter.test008"),
      "com.android.jack.java8.parameter.test008.Tests").setSrcDirName("")
          .addCandidateExtraSources(extraJavaFile);

  private RuntimeTestInfo PARAMETER009 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.parameter.test009"),
      "com.android.jack.java8.parameter.test009.Tests").setSrcDirName("")
          .addCandidateExtraSources(extraJavaFile);

  private RuntimeTestInfo PARAMETER010 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.parameter.test010"),
      "com.android.jack.java8.parameter.test010.Tests").setSrcDirName("")
          .addCandidateExtraSources(extraJavaFile);

  @Test
  @Runtime(from=RuntimeVersion.O)
  public void testParametere001() throws Exception {
    run(PARAMETER001);
  }

  @Test
  @Runtime(from = RuntimeVersion.O)
  public void testParametere002() throws Exception {
    new RuntimeTestHelper(PARAMETER002)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .addIgnoredCandidateToolchain(LegacyBasedToolchain.class)
        .addProperty(ParameterMetadataAnnotationsAdder.PARAMETER_ANNOTATION.getName(), "true")
        .setWithDebugInfos(true).compileAndRunTest();
  }

  @Test
  @Runtime(from=RuntimeVersion.O)
  public void testParametere003() throws Exception {
    run(PARAMETER003);
  }

  @Test
  @Runtime(from=RuntimeVersion.O)
  public void testParametere004() throws Exception {
    new RuntimeTestHelper(PARAMETER004)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .addIgnoredCandidateToolchain(LegacyBasedToolchain.class)
        .addProperty(ParameterMetadataAnnotationsAdder.PARAMETER_ANNOTATION.getName(), "true")
        .setWithDebugInfos(true).compileAndRunTest();
  }

  @Test
  @Runtime(from=RuntimeVersion.O)
  public void testParametere005() throws Exception {
    new RuntimeTestHelper(PARAMETER005.addFileChecker(new FileChecker() {

      @Override
      public void check(@Nonnull File file) throws Exception {
        DexFile dexFile = new DexFile(file);
        EncodedMethod em = TestTools.getEncodedMethod(dexFile,
            "Lcom/android/jack/java8/parameter/test005/Tests;", "m1", "(III)I");
        StringIdItem[] parameterNames = em.codeItem.getDebugInfo().getParameterNames();
        Assert.assertEquals("p1", parameterNames[0].getStringValue());
        Assert.assertEquals("p2", parameterNames[1].getStringValue());
        Assert.assertEquals("p3", parameterNames[2].getStringValue());
      }
    })).setSourceLevel(SourceLevel.JAVA_8)
       .addIgnoredCandidateToolchain(JackApiV01.class)
       .addIgnoredCandidateToolchain(LegacyBasedToolchain.class)
       .addProperty(ParameterMetadataAnnotationsAdder.PARAMETER_ANNOTATION.getName(), "true")
       .setWithDebugInfos(true)
       .compileAndRunTest();
  }

  @Test
  @Runtime(from=RuntimeVersion.O)
  public void testParametere005_1() throws Exception {
    new RuntimeTestHelper(PARAMETER005.addFileChecker(new FileChecker() {

      @Override
      public void check(@Nonnull File file) throws Exception {
        DexFile dexFile = new DexFile(file);
        EncodedMethod em = TestTools.getEncodedMethod(dexFile,
            "Lcom/android/jack/java8/parameter/test005/Tests;", "m1", "(III)I");
        StringIdItem[] parameterNames = em.codeItem.getDebugInfo().getParameterNames();
        Assert.assertNull(parameterNames[0]);
        Assert.assertNull(parameterNames[1]);
        Assert.assertNull(parameterNames[2]);
      }
    })).setSourceLevel(SourceLevel.JAVA_8)
       .addIgnoredCandidateToolchain(JackApiV01.class)
       .addIgnoredCandidateToolchain(LegacyBasedToolchain.class)
       .addProperty(ParameterMetadataAnnotationsAdder.PARAMETER_ANNOTATION.getName(), "true")
       .setWithDebugInfos(false)
       .compileAndRunTest();
  }

  /**
   * Test behavior with a getter accessor.
   */
  @Test
  @Runtime(from=RuntimeVersion.O)
  public void testParametere006() throws Exception {
    run(PARAMETER006);
  }

  /**
   * Test behavior with a setter accessor.
   */
  @Test
  @Runtime(from=RuntimeVersion.O)
  public void testParametere007() throws Exception {
    run(PARAMETER007);
  }

  /**
   * Test behavior with a wrapper.
   */
  @Test
  @Runtime(from=RuntimeVersion.O)
  public void testParametere008() throws Exception {
    run(PARAMETER008);
  }

  /**
   * Test behavior with a synthetic bridge.
   */
  @Test
  @Runtime(from=RuntimeVersion.O)
  public void testParametere009() throws Exception {
    run(PARAMETER009);
  }

  /**
   * Test behavior with method mixing 32-bit and 64-bit parameters.
   */
  @Test
  @Runtime(from=RuntimeVersion.O)
  public void testParametere010() throws Exception {
    run(PARAMETER010);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .addIgnoredCandidateToolchain(LegacyBasedToolchain.class)
        .addProperty(ParameterMetadataAnnotationsAdder.PARAMETER_ANNOTATION.getName(), "true")
        .setWithDebugInfos(true)
        .compileAndRunTest();
  }
}
