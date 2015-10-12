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
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.compatibility.AndroidCompatibilityChecker;
import com.android.jack.test.TestsProperties;
import com.android.jack.test.helper.FileChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import junit.framework.Assert;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
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

  private RuntimeTestInfo LAMBDA036 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test036"),
      "com.android.jack.java8.lambda.test036.jack.Tests");

  private RuntimeTestInfo LAMBDA038 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test038"),
      "com.android.jack.java8.lambda.test038.jack.Tests");

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
    new RuntimeTestHelper(LAMBDA019)
    .setSourceLevel(SourceLevel.JAVA_8)
    // This test must be exclude from the Jill tool-chain because it does not compile with it
    .addIgnoredCandidateToolchain(JillBasedToolchain.class)
    .addIgnoredCandidateToolchain(JackApiV01.class)
    .setWithDebugInfos(true)
    .compileAndRunTest();
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

  @Test
  public void testLamba036() throws Exception {
    run(LAMBDA036);
  }

  /**
   * Test that warnings are printed when compiling a Serializable lambda directly to a dex.
   */
  @Test
  public void testLamba037Direct() throws Exception {
    List<Class<? extends IToolchain>> excludedToolchains =
        new ArrayList<Class<? extends IToolchain>>();
    excludedToolchains.add(JackApiV01.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);

    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setErrorStream(err);
    toolchain.setSourceLevel(SourceLevel.JAVA_8);

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToExe(
        AbstractTestTools.createTempDir(), /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test037.jack"));
    String errString = err.toString();
    Assert.assertTrue(errString.contains("Tests.java:34: Serializable lambda is not supported"));
    Assert.assertTrue(errString.contains("Tests.java:43: Serializable lambda is not supported"));
    Assert.assertTrue(errString.contains("Tests.java:55: Serializable lambda is not supported"));
  }

  /**
   * Test that warnings are NOT printed when compiling a Serializable lambda directly to a Jack
   * library but are printed when compiling the library to a dex.
   */
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testLamba037ThroughLib() throws Exception {
    List<Class<? extends IToolchain>> excludedToolchains =
        new ArrayList<Class<? extends IToolchain>>();
    excludedToolchains.add(JackApiV01.class);

    // src to lib
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File lib = AbstractTestTools.createTempFile("lib", toolchain.getLibraryExtension());
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setErrorStream(err);
    toolchain.setSourceLevel(SourceLevel.JAVA_8);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToLib(lib,
        /* zipFile = */ true,
        AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test037.jack"));
    String errString = err.toString();
    Assert.assertTrue(errString.isEmpty());

    // lib to dex
    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    err = new ByteArrayOutputStream();
    toolchain.setErrorStream(err);
    toolchain.setSourceLevel(SourceLevel.JAVA_8);

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).libToExe(lib,
        AbstractTestTools.createTempDir(), /* zipFile = */ false);
    errString = err.toString();
    Assert.assertTrue(errString.contains("Tests.java:34: Serializable lambda is not supported"));
    Assert.assertTrue(errString.contains("Tests.java:43: Serializable lambda is not supported"));
    Assert.assertTrue(errString.contains("Tests.java:55: Serializable lambda is not supported"));
  }

  /**
   * Test that warnings are printed when compiling a Serializable lambda directly to a Jack
   * library with checks manually enabled, as well as when compiling the library to a dex.
   */
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testLamba037ThroughLibPlusCheck() throws Exception {
    List<Class<? extends IToolchain>> excludedToolchains =
        new ArrayList<Class<? extends IToolchain>>();
    // This test can not be used with JillBasedToolchain because generated library will be a jar
    // file and not jack library, consequently Jack warning will not be print.
    excludedToolchains.add(JillBasedToolchain.class);
    excludedToolchains.add(JackApiV01.class);

    File lib = AbstractTestTools.createTempFile("lib", ".jack");

    // src to lib
    {
      JackBasedToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      toolchain.setErrorStream(err);
      toolchain.setSourceLevel(SourceLevel.JAVA_8);
      // we need to enable the compatibility check manually since we haven't specified an Android
      // API level or a dex output
      toolchain.addProperty(AndroidCompatibilityChecker.CHECK_COMPATIBILITY.getName(), "true");

      toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToLib(lib,
          /* zipFile = */ true,
          AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test037.jack"));
      String errString = err.toString();
      Assert.assertTrue(errString.contains("Tests.java:34: Serializable lambda is not supported"));
      Assert.assertTrue(errString.contains("Tests.java:43: Serializable lambda is not supported"));
      Assert.assertTrue(errString.contains("Tests.java:55: Serializable lambda is not supported"));
    }

    // lib to dex
    {
      JackBasedToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      toolchain.setErrorStream(err);
      toolchain.setSourceLevel(SourceLevel.JAVA_8);

      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
          .libToExe(lib, AbstractTestTools.createTempDir(), /* zipFile = */ false);
      String errString = err.toString();
      Assert.assertTrue(errString.contains("Tests.java:34: Serializable lambda is not supported"));
      Assert.assertTrue(errString.contains("Tests.java:43: Serializable lambda is not supported"));
      Assert.assertTrue(errString.contains("Tests.java:55: Serializable lambda is not supported"));
    }
  }

  @Test
  public void testLamba038() throws Exception {
    run(LAMBDA038);
  }

  /**
   * Check that source using lambda can be compiled through jack library when SAM interface is into
   * another library. Dex file generated from the source files is done through a jack library
   *  where the predex is used.
   */
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testLamba039() throws Exception {
    List<Class<? extends IToolchain>> excludedToolchains = new ArrayList<Class<? extends IToolchain>>();
    excludedToolchains.add(JackApiV01.class);
    excludedToolchains.add(JillBasedToolchain.class);

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    File lib =
        AbstractTestTools.createTempFile("lib", toolchain.getLibraryExtension());
    File sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test039.lib");
    toolchain.addToClasspath(defaultClasspath).srcToLib(lib, /* zipFiles = */ true,
        sourceDir);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File libDexFolder = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(defaultClasspath)
    .setSourceLevel(SourceLevel.JAVA_8)
    .libToExe(lib,  libDexFolder, /* zipFiles = */ false);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File srclib = AbstractTestTools.createTempFile("srclib", toolchain.getLibraryExtension());
    sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test039.jack");
    toolchain.addToClasspath(defaultClasspath)
    .addToClasspath(lib)
    .setSourceLevel(SourceLevel.JAVA_8)
    .srcToLib(srclib, /* zipFiles = */ true, sourceDir);


    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File srcDexFolder = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(defaultClasspath)
    .setSourceLevel(SourceLevel.JAVA_8)
    .libToExe(srclib, srcDexFolder, /* zipFiles = */ false);

    run("com.android.jack.java8.lambda.test039.jack.Tests",
        new File[] {
            new File(TestsProperties.getJackRootDir(), "jack-tests/prebuilts/junit4-hostdex.jar"),
            new File(libDexFolder, DexFileWriter.DEX_FILENAME),
            new File(srcDexFolder, DexFileWriter.DEX_FILENAME)});
  }

  /**
   * Check that source using lambda can be compiled through jack library when SAM interface is into
   * another library. Dex file generated from the source files is done through a jack library
   *  where the predex is not used.
   */
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testLamba039_bis() throws Exception {
    List<Class<? extends IToolchain>> excludedToolchains = new ArrayList<Class<? extends IToolchain>>();
    excludedToolchains.add(JackApiV01.class);
    excludedToolchains.add(JillBasedToolchain.class);

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    File lib =
        AbstractTestTools.createTempFile("lib", toolchain.getLibraryExtension());
    File sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test039.lib");
    toolchain.addToClasspath(defaultClasspath).srcToLib(lib, /* zipFiles = */ true,
        sourceDir);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File libDexFolder = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(defaultClasspath)
    .setSourceLevel(SourceLevel.JAVA_8)
    .libToExe(lib,  libDexFolder, /* zipFiles = */ false);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File srclib = AbstractTestTools.createTempFile("srclib", toolchain.getLibraryExtension());
    sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test039.jack");
    toolchain.addToClasspath(defaultClasspath)
    .addToClasspath(lib)
    .setSourceLevel(SourceLevel.JAVA_8)
    .srcToLib(srclib, /* zipFiles = */ true, sourceDir);


    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    toolchain.addProperty(Options.USE_PREBUILT_FROM_LIBRARY.getName(), Boolean.FALSE.toString());
    File srcDexFolder = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(defaultClasspath)
    .setSourceLevel(SourceLevel.JAVA_8)
    .libToExe(srclib, srcDexFolder, /* zipFiles = */ false);

    run("com.android.jack.java8.lambda.test039.jack.Tests",
        new File[] {
            new File(TestsProperties.getJackRootDir(), "jack-tests/prebuilts/junit4-hostdex.jar"),
            new File(libDexFolder, DexFileWriter.DEX_FILENAME),
            new File(srcDexFolder, DexFileWriter.DEX_FILENAME)});
  }

  /**
   * Add a test using a lambda that required an inner accessor to be generated and verify that it
   * works correctly even if a library is missing on its classpath during the workflow lib2dex.
   */
  @Test
  @KnownIssue(candidate = IncrementalToolchain.class)
  public void testLamba040() throws Exception {
    List<Class<? extends IToolchain>> excludedToolchains =
        new ArrayList<Class<? extends IToolchain>>();
    excludedToolchains.add(JackApiV01.class);
    excludedToolchains.add(JillBasedToolchain.class);

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    File lib = AbstractTestTools.createTempFile("lib", toolchain.getLibraryExtension());
    File sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test040.lib");
    toolchain.addToClasspath(defaultClasspath).srcToLib(lib, /* zipFiles = */ true, sourceDir);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File libDexFolder = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(defaultClasspath).setSourceLevel(SourceLevel.JAVA_8).libToExe(lib,
        libDexFolder, /* zipFiles = */ false);

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    File srclib = AbstractTestTools.createTempFile("srclib", toolchain.getLibraryExtension());
    sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.java8.lambda.test040.jack");
    toolchain.addToClasspath(defaultClasspath).addToClasspath(lib)
        .setSourceLevel(SourceLevel.JAVA_8).srcToLib(srclib, /* zipFiles = */ true, sourceDir);



    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludedToolchains);
    toolchain.addProperty(Options.USE_PREBUILT_FROM_LIBRARY.getName(), Boolean.FALSE.toString());
    File srcDexFolder = AbstractTestTools.createTempDir();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    toolchain.setErrorStream(baos);
    toolchain.addToClasspath(defaultClasspath).setSourceLevel(SourceLevel.JAVA_8).libToExe(srclib,
        srcDexFolder, /* zipFiles = */ false);

    run("com.android.jack.java8.lambda.test040.jack.Tests",
        new File[] {
            new File(TestsProperties.getJackRootDir(), "jack-tests/prebuilts/junit4-hostdex.jar"),
            new File(libDexFolder, DexFileWriter.DEX_FILENAME),
            new File(srcDexFolder, DexFileWriter.DEX_FILENAME)});
  }

  private void run(@Nonnull String mainClass, @Nonnull File[] dexFiles) throws Exception {
    List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners(null);
    for (RuntimeRunner runner : runnerList) {
      Assert.assertEquals(0, runner.runJUnit(new String[0], AbstractTestTools.JUNIT_RUNNER_NAME,
          new String[] {mainClass}, dexFiles));
    }
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .setWithDebugInfos(true)
        .compileAndRunTest();
  }
}
