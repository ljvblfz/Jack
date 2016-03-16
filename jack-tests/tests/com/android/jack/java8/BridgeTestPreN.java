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
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import junit.framework.Assert;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BridgeTestPreN {

  private RuntimeTestInfo BRIDGE001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.bridges.test001"),
      "com.android.jack.java8.bridges.test001.jack.Tests");

  private RuntimeTestInfo BRIDGE003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.bridges.test003"),
      "com.android.jack.java8.bridges.test003.jack.Tests");

  @Test
  public void testBridge001() throws Exception {
    new RuntimeTestHelper(BRIDGE001).setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .compileAndRunTest();
  }

  /**
   * Check that Pre-N library can be build when source code has default bridge into interface and
   * that later this library can be use to generate a Pre-N dex file when pre-dex is used.
   */
  @Test
  public void testBridge001_BIS() throws Exception {
    List<Class<? extends IToolchain>> excludedToolchains =
        new ArrayList<Class<? extends IToolchain>>();
    excludedToolchains.add(JillBasedToolchain.class);
    excludedToolchains.add(JackApiV01.class);

    // Build a Jack library from source files
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludedToolchains);
    File srclib = AbstractTestTools.createTempFile("srclib", toolchain.getLibraryExtension());
    File sourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.java8.bridges.test001.jack");
    ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
    toolchain.setErrorStream(errorStream);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).setSourceLevel(SourceLevel.JAVA_8)
        .srcToLib(srclib, /* zipFiles = */ true, sourceDir);


    // Build dex file from the Jack library
    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File srcDexFolder = AbstractTestTools.createTempDir();
    errorStream = new ByteArrayOutputStream();
    toolchain.setErrorStream(errorStream);
    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
          .setSourceLevel(SourceLevel.JAVA_8)
          .libToExe(srclib, srcDexFolder, /* zipFiles = */ false);
    } catch (JackAbortException e) {
      Assert.fail();
    }
  }

  @Test
  public void testBridge003() throws Exception {
    new RuntimeTestHelper(BRIDGE003).setSourceLevel(SourceLevel.JAVA_8)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        // Known issue with JillBasedToolchain because when Jill is used, it does not provide
        // information needed to generate bridges, instead it uses default bridge methods into
        // interfaces.
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .compileAndRunTest();
  }
}
