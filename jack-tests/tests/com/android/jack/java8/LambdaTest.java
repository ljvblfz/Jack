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

import com.android.jack.Options;
import com.android.jack.backend.dex.compatibility.AndroidCompatibilityChecker;
import com.android.jack.test.helper.FileChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import junit.framework.Assert;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  private RuntimeTestInfo LAMBDA019 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test019"),
      "com.android.jack.java8.lambda.test019.jack.Tests");

  private RuntimeTestInfo LAMBDA020 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test020"),
      "com.android.jack.java8.lambda.test020.jack.Tests");

  private RuntimeTestInfo LAMBDA021 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test021"),
      "com.android.jack.java8.lambda.test021.jack.Tests");

  private RuntimeTestInfo LAMBDA022 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test022"),
      "com.android.jack.java8.lambda.test022.jack.Tests");

  private RuntimeTestInfo LAMBDA023 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test023"),
      "com.android.jack.java8.lambda.test023.jack.Tests");

  private RuntimeTestInfo LAMBDA024 =
      new RuntimeTestInfo(AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test024"),
          "com.android.jack.java8.lambda.test024.jack.Tests")
              .addProguardFlagsFileName("proguard.flags").addFileChecker(
                  new ShrinkFileChecker().addKeptFile("I1.java").addRemovedFile("I2.java"));

  private RuntimeTestInfo LAMBDA025 =
      new RuntimeTestInfo(AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test025"),
          "com.android.jack.java8.lambda.test025.jack.Tests")
              .addProguardFlagsFileName("proguard.flags").addFileChecker(
                  new ShrinkFileChecker().addKeptFile("I1.java").addKeptFile("I2.java"));

  private RuntimeTestInfo LAMBDA026 =
      new RuntimeTestInfo(AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test026"),
          "com.android.jack.java8.lambda.test026.jack.Tests")
              .addProguardFlagsFileName("proguard.flags").addFileChecker(
                  new ShrinkFileChecker().addKeptFile("I1.java").addRemovedFile("I2.java"));

  private RuntimeTestInfo LAMBDA027 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test027"),
      "com.android.jack.java8.lambda.test027.jack.Tests");

  private RuntimeTestInfo LAMBDA028 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test028"),
      "com.android.jack.java8.lambda.test028.jack.Tests");

  private RuntimeTestInfo LAMBDA029 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test029"),
      "com.android.jack.java8.lambda.test029.jack.Tests");

  private RuntimeTestInfo LAMBDA030 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test030"),
      "com.android.jack.java8.lambda.test030.jack.Tests");

  private RuntimeTestInfo LAMBDA031 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test031"),
      "com.android.jack.java8.lambda.test031.jack.Tests");

  private RuntimeTestInfo LAMBDA032 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test032"),
      "com.android.jack.java8.lambda.test032.jack.Tests");

  private RuntimeTestInfo LAMBDA033 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test033"),
      "com.android.jack.java8.lambda.test033.jack.Tests")
          .addProguardFlagsFileName("proguard.flags");

  private RuntimeTestInfo LAMBDA034 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test034"),
      "com.android.jack.java8.lambda.test034.jack.Tests");

  private RuntimeTestInfo LAMBDA035 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test035"),
      "com.android.jack.java8.lambda.test035.jack.Tests");

  @Test
  public void testLamba001() throws Exception {
    run(LAMBDA001);
  }

  @Test
  public void testLamba002() throws Exception {
    run(LAMBDA002);
  }

  @Test
  public void testLamba003() throws Exception {
    run(LAMBDA003);
  }

  @Test
  public void testLamba004() throws Exception {
    run(LAMBDA004);
  }

  @Test
  public void testLamba005() throws Exception {
    run(LAMBDA005);
  }

  @Test
  public void testLamba006() throws Exception {
    run(LAMBDA006);
  }

  @Test
  public void testLamba007() throws Exception {
    run(LAMBDA007);
  }

  @Test
  public void testLamba008() throws Exception {
    run(LAMBDA008);
  }

  @Test
  public void testLamba009() throws Exception {
    run(LAMBDA009);
  }

  @Test
  public void testLamba010() throws Exception {
    run(LAMBDA010);
  }

  @Test
  public void testLamba011() throws Exception {
    run(LAMBDA011);
  }

  @Test
  public void testLamba012() throws Exception {
    run(LAMBDA012);
  }

  @Test
  public void testLamba013() throws Exception {
    run(LAMBDA013);
  }

  @Test
  public void testLamba014() throws Exception {
    run(LAMBDA014);
  }

  @Test
  public void testLamba015() throws Exception {
    run(LAMBDA015);
  }

  @Test
  public void testLamba016() throws Exception {
    run(LAMBDA016);
  }

  @Test
  public void testLamba017() throws Exception {
    run(LAMBDA017);
  }

  @Test
  public void testLamba018() throws Exception {
    run(LAMBDA018);
  }

  @Test
  public void testLamba019() throws Exception {
    run(LAMBDA019);
  }

  @Test
  public void testLamba020() throws Exception {
    run(LAMBDA020);
  }

  @Test
  public void testLamba021() throws Exception {
    run(LAMBDA021);
  }

  @Test
  public void testLamba022() throws Exception {
    run(LAMBDA022);
  }


  @Test
  public void testLamba023() throws Exception {
    run(LAMBDA023);
  }

  @Test
  public void testLamba024() throws Exception {
    run(LAMBDA024);
  }

  @Test
  public void testLamba025() throws Exception {
    run(LAMBDA025);
  }

  @Test
  public void testLamba026() throws Exception {
    run(LAMBDA026);
  }

  private class ShrinkFileChecker implements FileChecker {

    @Nonnull
    private List<String> keptFiles = new ArrayList<String>();

    @Nonnull
    private List<String> removedFiles = new ArrayList<String>();

    public ShrinkFileChecker addKeptFile(@Nonnull String fileName) {
      keptFiles.add(fileName);
      return this;
    }

    public ShrinkFileChecker addRemovedFile(@Nonnull String fileName) {
      removedFiles.add(fileName);
      return this;
    }

    @Override
    public void check(@Nonnull File file) throws Exception {
      DexFile dexFile = new DexFile(file);
      Set<String> sourceFileInDex = new HashSet<String>();

      for (ClassDefItem classDef : dexFile.ClassDefsSection.getItems()) {
        sourceFileInDex.add(classDef.getSourceFile().getStringValue());
      }

      for (String keptFile : keptFiles) {
        Assert.assertTrue(sourceFileInDex.contains(keptFile));
      }

      for (String removedFile : removedFiles) {
        Assert.assertTrue(!sourceFileInDex.contains(removedFile));
      }
    }
  }

  @Test
  public void testLamba027() throws Exception {
    run(LAMBDA027);
  }

  @Test
  public void testLamba028() throws Exception {
    run(LAMBDA028);
  }

  @Test
  public void testLamba029() throws Exception {
    run(LAMBDA029);
  }

  @Test
  public void testLamba030() throws Exception {
    run(LAMBDA030);
  }

  @Test
  public void testLamba031() throws Exception {
    run(LAMBDA031);
  }

  @Test
  public void testLamba032() throws Exception {
    run(LAMBDA032);
  }

  @Test
  public void testLamba033() throws Exception {
    run(LAMBDA033);
  }

  @Test
  public void testLamba034() throws Exception {
    run(LAMBDA034);
  }

  @Test
  public void testLamba035() throws Exception {
    run(LAMBDA035);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addProperty(
            Options.ANDROID_MIN_API_LEVEL.getName(),
            String.valueOf(AndroidCompatibilityChecker.N_API_LEVEL))
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .compileAndRunTest();
  }

}
