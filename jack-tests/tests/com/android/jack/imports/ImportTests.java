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

package com.android.jack.imports;

import com.android.jack.JackAbortException;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.backend.jayce.TypeImportConflictException;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillApiToolchainBase;
import com.android.jack.test.toolchain.JillBasedToolchain;

import junit.framework.Assert;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class ImportTests {



  @Test
  public void testCompileNonConflictingSourceAndImport() throws Exception {
    File jackOut = AbstractTestTools.createTempDir();
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillApiToolchainBase.class);
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain(IToolchain.class, exclude);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackOut,
        /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.fibonacci.test001.jack"));

    toolchain = AbstractTestTools.getCandidateToolchain(IToolchain.class, exclude);
    toolchain.addStaticLibs(jackOut);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.threeaddress.test001.jack"));
  }

  @Test
  public void testCompileConflictingSourceAndImport() throws Exception {
    File jackOut = AbstractTestTools.createTempDir();
    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackOut,
        /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.fibonacci.test001.jack"));

    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addStaticLibs(jackOut);

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    toolchain.setErrorStream(errOut);
    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToExe(
          AbstractTestTools.createTempDir(),
          /* zipFile = */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.fibonacci.test001.jack"));
      Assert.fail();
    } catch (JackAbortException e) {
      // expected
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof TypeImportConflictException);
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("Type com.android.jack.fibonacci.test001.jack.Fibo"));
      Assert.assertTrue(errString.contains("has already been imported"));
    }
  }

  @Test
  @KnownIssue(candidate = JillBasedToolchain.class)
  public void testConflictingImport() throws Exception {
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    commonConflictingImport(errOut, false /* = verbose */ );
    errOut.close();
    Assert.assertEquals("", errOut.toString());
  }

  @Test
  public void testConflictingImportVerbose() throws Exception {
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    commonConflictingImport(errOut, true /* = verbose */ );
    errOut.close();
    String errString = errOut.toString();
    Assert.assertTrue(errString
        .contains("Ignoring import: Type com.android.jack.inner.test015.lib.A from"));
    Assert.assertTrue(errString.contains("has already been imported from"));
  }

  private void commonConflictingImport(@Nonnull ByteArrayOutputStream errorStream, boolean verbose)
      throws Exception {
    String testName = "com.android.jack.inner.test015";
    File lib = AbstractTestTools.createTempDir();
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillApiToolchainBase.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .setVerbose(verbose)
    .setErrorStream(errorStream)
    .srcToLib(
        lib,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir(testName + ".lib"));


    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    // import twice the same lib
    toolchain.addStaticLibs(lib, lib);
    toolchain.addProperty(JayceFileImporter.COLLISION_POLICY.getName(), "keep-first");
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .setVerbose(verbose)
    .setErrorStream(errorStream)
    .srcToExe(
        AbstractTestTools.createTempFile("inner15", ".zip"),
        /* zipFile = */ true,
        AbstractTestTools.getTestRootDir(testName + ".jack"));
  }

  @Test
  public void testConflictingImportWithFailPolicy1() throws Exception {
    String testName = "com.android.jack.inner.test015";
    File lib = AbstractTestTools.createTempDir();
    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        lib,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir(testName + ".lib"));

    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    // import twice the same lib
    toolchain.addStaticLibs(lib, lib);
    toolchain.addProperty(JayceFileImporter.COLLISION_POLICY.getName(), "fail");

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    toolchain.setErrorStream(errOut);
    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToExe(
          AbstractTestTools.createTempFile("inner15", ".zip"),
          /* zipFile = */ true,
          AbstractTestTools.getTestRootDir(testName + ".jack"));
      Assert.fail();
    } catch (JackAbortException e) {
      // Exception is ok
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof TypeImportConflictException);
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("Type com.android.jack.inner.test015.lib.A"));
      Assert.assertTrue(errString.contains("has already been imported"));
    }
  }

  @Test
  public void testConflictingImportWithFailPolicy2() throws Exception {
    String testName = "com.android.jack.inner.test015";
    File lib1 = AbstractTestTools.createTempDir();
    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        lib1,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir(testName + ".lib"));

    File lib2 = AbstractTestTools.createTempDir();
    toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        lib2,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir(testName + ".lib"));

    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    toolchain.addStaticLibs(lib1, lib2);
    toolchain.addProperty(JayceFileImporter.COLLISION_POLICY.getName(), "fail");

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    toolchain.setErrorStream(errOut);
    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToExe(
          AbstractTestTools.createTempFile("inner15", ".zip"),
          /* zipFile = */ true,
          AbstractTestTools.getTestRootDir(testName + ".jack"));
      Assert.fail();
    } catch (JackAbortException e) {
      // Exception is ok
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof TypeImportConflictException);
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("Type com.android.jack.inner.test015.lib.A"));
      Assert.assertTrue(errString.contains("has already been imported"));
    }
  }
}
