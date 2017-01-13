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

import com.android.jack.JackAbortException;
import com.android.jack.Options;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.junit.RuntimeVersion;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JackApiV01Toolchain;
import com.android.jack.test.toolchain.JackApiV02Toolchain;
import com.android.jack.test.toolchain.JackApiV03Toolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;
import com.android.jack.util.AndroidApiLevel;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BridgeTestPostM {

  private RuntimeTestInfo BRIDGE002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.bridges.test002"),
      "com.android.jack.java8.bridges.test002.jack.Tests");

  @Test
  @Runtime(from=RuntimeVersion.N)
  public void testBridge002() throws Exception {
    new RuntimeTestHelper(BRIDGE002)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addProperty(Options.ANDROID_MIN_API_LEVEL.getName(),
            String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()))
        .addIgnoredCandidateToolchain(JackApiV01.class).compileAndRunTest();
  }

  /**
   * Ensure that we CANNOT compile a lib with min api 23 WITH predexing because it contains
   * default bridge methods that are not allowed in min api 23 (only starting from min api 24).
   */
  @Test
  @KnownIssue
  public void testBridge002_2_WithPredexing() throws Exception {
    runTestBridge002_2(true);
  }

  /**
   * Ensure that we can compile a lib with min api 23 WITHOUT predexing and then import it into
   * a dex with min api 24.
   */
  @Test
  @Runtime(from=RuntimeVersion.N)
  @KnownIssue
  public void testBridge002_2_WithoutPredexing() throws Exception {
    runTestBridge002_2(false);
  }

  private void runTestBridge002_2(boolean enablePredexing) throws Exception {
    List<Class<? extends IToolchain>> excludeClazz = new ArrayList<Class<? extends IToolchain>>(2);
    excludeClazz.add(JackApiV01.class);
    excludeClazz.add(JillBasedToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeClazz);
    boolean pre04 = (toolchain instanceof JackApiV01Toolchain ||
                     toolchain instanceof JackApiV02Toolchain ||
                     toolchain instanceof JackApiV03Toolchain);
    File lib23 =
        AbstractTestTools.createTempFile("lib23", toolchain.getLibraryExtension());
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(23))
    .addProperty(Options.GENERATE_DEX_IN_LIBRARY.getName(), Boolean.toString(enablePredexing))
    .setSourceLevel(SourceLevel.JAVA_8)
    .addToClasspath(toolchain.getDefaultBootClasspath());

    if (!pre04 && enablePredexing) {
      ByteArrayOutputStream errOut = new ByteArrayOutputStream();
      toolchain.setErrorStream(errOut);
      try {
        toolchain.srcToLib(lib23,
            /* zipFiles = */ true, new File(BRIDGE002.directory, BRIDGE002.srcDirName));
        Assert.fail();
      } catch (JackAbortException e) {
        Assert.assertTrue(
            errOut.toString().contains("not supported in Android API level less than 24"));
      }
    } else {
      toolchain.srcToLib(lib23,
          /* zipFiles = */ true, new File(BRIDGE002.directory, BRIDGE002.srcDirName));

      File dex24 = AbstractTestTools.createTempDir();
      toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeClazz);
      toolchain.addProperty(
          Options.ANDROID_MIN_API_LEVEL.getName(),
          String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()))
      .libToExe(lib23, dex24, /* zipFiles = */ false);

      // Run to check everything went as expected
      RuntimeTestHelper.runOnRuntimeEnvironments(
          Collections.singletonList(BRIDGE002.jUnit),
          RuntimeTestHelper.getJunitDex(), new File(dex24, "classes.dex"));
    }
  }

  /**
   * Ensure that we can compile as lib with min api 24 and then import it to
   * a dex with min api 24.
   */
  @Test
  @Runtime(from=RuntimeVersion.N)
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testBridge002_3() throws Exception {
    List<Class<? extends IToolchain>> excludeClazz = new ArrayList<Class<? extends IToolchain>>(2);
    excludeClazz.add(JackApiV01.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeClazz);
    File lib23 =
        AbstractTestTools.createTempFile("lib23", toolchain.getLibraryExtension());
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()))
    .setSourceLevel(SourceLevel.JAVA_8)
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(lib23,
        /* zipFiles = */ true, new File(BRIDGE002.directory, BRIDGE002.srcDirName));

    File dex24 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeClazz);
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(AndroidApiLevel.ReleasedLevel.N.getLevel()))
    .libToExe(lib23, dex24, /* zipFiles = */ false);

    // Run to check everything went as expected
    RuntimeTestHelper.runOnRuntimeEnvironments(
        Collections.singletonList(BRIDGE002.jUnit),
        RuntimeTestHelper.getJunitDex(), new File(dex24, "classes.dex"));

  }

  /**
   * Ensure that we report an error when compiling a lib WITH predexing that contains
   * default methods and is built with min api 23.
   */
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testBridge002_4_WithPredexing() throws Exception {
    runTestBridge002_4(true);
  }

  /**
   * Ensure that we can compile a lib WITHOUT predexing in min api 23 while it contains default
   * methods.
   * Then ensure that we report an error when importing that lib into a dex with min api 23.
   */
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testBridge002_4_WithoutPredexing() throws Exception {
    runTestBridge002_4(false);
  }

  private void runTestBridge002_4(boolean enablePredexing) throws Exception {
    List<Class<? extends IToolchain>> excludeClazz = new ArrayList<Class<? extends IToolchain>>(1);
    excludeClazz.add(JackApiV01.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeClazz);
    boolean pre04 = (toolchain instanceof JackApiV01Toolchain ||
                     toolchain instanceof JackApiV02Toolchain ||
                     toolchain instanceof JackApiV03Toolchain);
    File lib23 =
        AbstractTestTools.createTempFile("lib23", toolchain.getLibraryExtension());
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(23))
    .addProperty(Options.GENERATE_DEX_IN_LIBRARY.getName(), Boolean.toString(enablePredexing))
    .setSourceLevel(SourceLevel.JAVA_8)
    .addToClasspath(toolchain.getDefaultBootClasspath());

    if (!pre04 && enablePredexing) {
      ByteArrayOutputStream errOut = new ByteArrayOutputStream();
      toolchain.setErrorStream(errOut);
      try {
        toolchain.srcToLib(lib23,
            /* zipFiles = */ true, new File(BRIDGE002.directory, BRIDGE002.srcDirName));
        Assert.fail();
      } catch (JackAbortException e) {
        Assert.assertTrue(
            errOut.toString().contains("not supported in Android API level less than 24"));
      }
    } else {
      toolchain.srcToLib(lib23,
          /* zipFiles = */ true, new File(BRIDGE002.directory, BRIDGE002.srcDirName));

      ByteArrayOutputStream errOut = new ByteArrayOutputStream();
      File dex23 = AbstractTestTools.createTempDir();
      toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeClazz);
      toolchain.addProperty(
          Options.ANDROID_MIN_API_LEVEL.getName(),
          String.valueOf(23))
      .setErrorStream(errOut);
      try {
        toolchain.libToExe(lib23, dex23, /* zipFiles = */ false);
        Assert.fail();
      } catch (JackAbortException e) {
        Assert.assertTrue(
            errOut.toString().contains("not supported in Android API level less than 24"));
      }
    }
  }

  /**
   * Ensure that we report an error when compiling a lib with default method built with min api 24
   * into a dex with min api 23.
   */
  @Test
  @KnownIssue(candidate=IncrementalToolchain.class)
  public void testBridge002_5() throws Exception {
    List<Class<? extends IToolchain>> excludeClazz = new ArrayList<Class<? extends IToolchain>>(1);
    excludeClazz.add(JackApiV01.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeClazz);
    File lib24 =
        AbstractTestTools.createTempFile("lib24", toolchain.getLibraryExtension());
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(24))
    .setSourceLevel(SourceLevel.JAVA_8)
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(lib24,
        /* zipFiles = */ true, new File(BRIDGE002.directory, BRIDGE002.srcDirName));

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    File dex23 = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeClazz);
    toolchain.addProperty(
        Options.ANDROID_MIN_API_LEVEL.getName(),
        String.valueOf(23))
    .setErrorStream(errOut);
    try {
      toolchain.libToExe(lib24, dex23, /* zipFiles = */ false);
    } catch (JackAbortException e) {
      Assert.assertTrue(
          errOut.toString().contains("not supported in Android API level less than 24"));
    }
  }
}
