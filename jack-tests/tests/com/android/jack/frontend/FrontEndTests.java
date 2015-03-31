/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.frontend;

import com.android.jack.category.ExtraTests;
import com.android.jack.test.category.KnownBugs;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class FrontEndTests {

  @Test
  @Category(KnownBugs.class)
  public void testMissingClass001() throws Exception {
    File outJackTmpMissing = AbstractTestTools.createTempDir();
    File outJackTmpSuper = AbstractTestTools.createTempDir();
    File outJackTmpTest = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain();

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        outJackTmpMissing,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test001.jack.missing"));

    toolchain =  AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(outJackTmpMissing)
    .srcToLib(outJackTmpSuper,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test001.jack.sub2"));

    toolchain =  AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(outJackTmpSuper)
    .srcToLib(outJackTmpTest,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test001.jack.test"));

  }

  /**
   * Test that we do not crash and that we report the error.
   */
  @Test
  @Category(KnownBugs.class)
  public void testConflictingPackage001() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test002.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains(
          "com.android.jack.frontend.test002.jack.PackageName"));
      Assert.assertTrue(errString.contains("collides"));
   }
  }

  /**
   * Test that we do not crash.
   */
  @Test
  @Category(ExtraTests.class)
  public void testConflictingPackage002() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =  AbstractTestTools.getCandidateToolchain();

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        outDir,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test003.jack"));
  }

  /**
   * Test that we do not crash and that we report the error.
   */
  @Test
  @Category(KnownBugs.class)
  public void testDuplicated001() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test016.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("Duplicated"));
   }
  }


  /**
   * Test that Jack is neither failing nor dropping warnings while ecj frontend is subject to skip
   * the local classes.
   */
  @Test
  @Category(ExtraTests.class)
  public void testUninstanciableLocalClass001() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =  AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        outDir,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test004.jack"));
    Assert.assertEquals(0, out.size());
    String errString = err.toString();
    int warnIndex = errString.indexOf("WARNING:");
    Assert.assertTrue(warnIndex != -1);
    warnIndex = errString.indexOf("WARNING:", warnIndex + 1);
    Assert.assertTrue(warnIndex != -1);
    Assert.assertTrue(errString.indexOf("WARNING:", warnIndex + 1) == -1);

  }

  /**
   * Test that Jack is neither failing nor dropping the error in this case.
   */
  @Test
  @Category(KnownBugs.class)
  public void testInnerError001() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test013.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("ExtendingInnerOnly"));
      Assert.assertTrue(errString.contains("Inner"));
   }
  }

  /**
   * Test that Jack is neither failing nor dropping the error in this case.
   */
  @Test
  @Category(ExtraTests.class)
  public void testInnerError002() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test014.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("ExtendingInnerInStaticContext"));
      Assert.assertTrue(errString.contains("Inner"));
   }
  }

  /**
   * Test that Jack is neither failing nor dropping the error in this case.
   */
  @Test
  @Category(KnownBugs.class)
  public void testInnerError003() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test015.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("WithOuterContextButStatic"));
      Assert.assertTrue(errString.contains("Inner"));
   }
  }

  /**
   * Test that Jack is neither failing nor dropping the error in this case.
   */
  @Test
  @Category(KnownBugs.class)
  public void testInnerError004() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test008.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("NoOuterContext"));
      Assert.assertTrue(errString.contains("Inner"));
   }
  }

  /**
   * Test that Jack is neither failing nor dropping the error in this case.
   */
  @Test
  @Category(KnownBugs.class)
  public void testUnusedLocalVar001() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    toolchain.setOutputStream(out);
    toolchain.setErrorStream(err);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test010.jack"));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertEquals(0, out.size());
      String errString = err.toString();
      Assert.assertTrue(errString.contains("ERROR:"));
      Assert.assertTrue(errString.contains("UnusedLocalVar"));
      Assert.assertTrue(errString.contains("Inner"));
   }
  }

  /**
   * Test that Jack is not failing.
   */
  @Test
  @Category(ExtraTests.class)
  public void testUnusedLocalVar002() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();

      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test011.jack"));
  }

  /**
   * Test that Jack is not failing.
   */
  @Test
  @Category(ExtraTests.class)
  public void testUnusedLocalVar003() throws Exception {
    File outDir = AbstractTestTools.createTempDir();

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();

      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(
          outDir,
          /* zipFiles= */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test012.jack"));
  }

}
