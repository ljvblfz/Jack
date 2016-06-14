/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.classpath;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.library.FileType;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.AndroidToolchain;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillApiToolchainBase;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.sched.vfs.VPath;

import junit.framework.Assert;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClasspathTests {

  @Test
  public void test001() throws Exception {
    File libOut = AbstractTestTools.createTempDir();

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(libOut, false,
        new File(AbstractTestTools.getTestRootDir("com.android.jack.classpath.test001"), "lib"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    File testOut = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(libOut)
    .srcToLib(testOut, false,
        new File(AbstractTestTools.getTestRootDir("com.android.jack.classpath.test001"), "jack"));
  }

  @Test
  public void test002() throws Exception {
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>(1);
    exclude.add(JillApiToolchainBase.class);

    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class, exclude);

    File[] defaultBootCp = toolchain.getDefaultBootClasspath();

    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test002");
    File outFolder = AbstractTestTools.createTempDir();

    File lib1Out = AbstractTestTools.createDir(outFolder, "lib1");
    toolchain.addToClasspath(defaultBootCp)
    .srcToLib(lib1Out,
        /* zipFiles = */ false, new File(testFolder, "lib1"));

    File lib1BisOut = AbstractTestTools.createDir(outFolder, "lib1override");
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class, exclude);
    toolchain.addToClasspath(defaultBootCp)
    .srcToLib(lib1BisOut,
        /* zipFiles = */ false, new File(testFolder, "lib1override"));

    File lib2Out = AbstractTestTools.createDir(outFolder, "lib2");
    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class, exclude);
    toolchain.addToClasspath(defaultBootCp)
    .addToClasspath(lib1Out)
    .srcToLib(lib2Out,
    /* zipFiles = */ false, new File(testFolder, "lib2"));

    toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class, exclude);
    toolchain.addStaticLibs(lib2Out);
    toolchain.addToClasspath(defaultBootCp)
    .addToClasspath(lib1BisOut)
    .srcToExe(outFolder,
        /* zipFile = */ false, new File(testFolder, "jack"));

  }

  @Test
  public void test003() throws Exception {
    File testDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test003");

    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    excludeList.add(JillBasedToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    File libOut = AbstractTestTools.createTempDir();
    File libSrc = new File(testDir, "lib");
    File[] defaultBootClasspath = toolchain.getDefaultBootClasspath();
    toolchain.addToClasspath(defaultBootClasspath)
    .srcToLib(libOut, /* zipFiles = */ false, libSrc);

    {
      // reference compilation
      toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
      File testOut = AbstractTestTools.createTempDir();
      File testSrc = new File(testDir, "jack");
      toolchain.addToClasspath(defaultBootClasspath)
      .addToClasspath(libOut)
      .srcToLib(testOut, /* zipFiles = */ false, testSrc);
    }

    {
      // delete unused inner in classpath and check we can still compile with it
      try (InputJackLibrary lib = AbstractTestTools.getInputJackLibrary(libOut)) {
        lib.getFile(FileType.JAYCE,
            new VPath("com/android/jack/classpath/test003/lib/HasInnersClasses$InnerToDelete", '/'))
            .delete();
      }
      toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
      File testOut = AbstractTestTools.createTempDir();
      File testSrc = new File(testDir, "jack");
      toolchain.addToClasspath(defaultBootClasspath)
      .addToClasspath(libOut)
      .srcToLib(testOut, /* zipFiles = */ false, testSrc);
    }
  }

  @Test
  public void libOfLib() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    File libOfLibOut = AbstractTestTools.createTempFile("libOfLibOut", toolchain.getLibraryExtension());
    File sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.liboflib.lib2");
    toolchain.addToClasspath(defaultClasspath)
    .srcToLib(libOfLibOut, /* zipFiles = */ true, sourceDir);

    toolchain = AbstractTestTools.getCandidateToolchain();
    File libOut = AbstractTestTools.createTempFile("libOut", toolchain.getLibraryExtension());
    sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.liboflib.lib");
    toolchain.addToClasspath(defaultClasspath)
    .addToClasspath(libOfLibOut)
    .srcToLib(libOut, /* zipFiles = */ true, sourceDir);

    toolchain = AbstractTestTools.getCandidateToolchain();
    File mainOut = AbstractTestTools.createTempFile("mainOut", toolchain.getLibraryExtension());
    sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.liboflib.main");
    toolchain.addToClasspath(defaultClasspath)
    .addToClasspath(libOut)
    .srcToLib(mainOut, /* zipFiles = */ true, sourceDir);
  }

  @Test
  public void testMissingClasspathEntry() throws Exception {
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>(1);
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    File srcDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test004.jack");
    File testOut = AbstractTestTools.createTempFile("ClasspathTest", "missing");
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    toolchain.setErrorStream(errOut);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
        .addToClasspath(new File(srcDir, "missing.jack")).srcToLib(testOut, /* zipFiles = */ true,
            srcDir);
    String errString = errOut.toString();
    Assert.assertTrue(errString.contains("Bad classpath entry ignored"));
    Assert.assertTrue(errString.contains("missing.jack\' does not exist"));
  }

  @Test
  public void testMissingClasspathEntryStrict() throws Exception {
    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File srcDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test004.jack");
    File testOut = AbstractTestTools.createTempFile("ClasspathTest", "missing");
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    toolchain.setErrorStream(errOut);
    toolchain.addProperty(Jack.STRICT_CLASSPATH.getName(), "true");
    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
          .addToClasspath(new File(srcDir, "missing.jack")).srcToLib(testOut, /* zipFiles = */ true,
              srcDir);
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
    } finally {
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("Library reading phase"));
      Assert.assertTrue(errString.contains("missing.jack\' does not exist"));
    }
  }

  @Test
  public void testInvalidClasspathEntry() throws Exception {
    File srcDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test004.jack");
    compileWithInvalidClasspathEntry(srcDir, new File(srcDir, "Classpath004.java"));
  }

  @Test
  @KnownIssue
  public void testInvalidJackLibraryInClasspath() throws Exception {
    File srcDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test004.jack");
    compileWithInvalidClasspathEntry(srcDir, new File(srcDir, "invalid.jack"));
  }

  private void compileWithInvalidClasspathEntry(File srcDir, File invalidJack) throws IOException,
      Exception {
    Assert.assertTrue(invalidJack.isFile());

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    File testOut = AbstractTestTools.createTempFile("ClasspathTest", "invalid");
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(invalidJack)
    .srcToLib(testOut, /* zipFiles = */ true, srcDir);

    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Jack.STRICT_CLASSPATH.getName(), "true");

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(invalidJack)
      .srcToLib(testOut, /* zipFiles = */ true, srcDir);
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
    }
  }

  @Test
  public void testWithZipInClasspath() throws Exception {
    File srcDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test004.jack");
    File zip = new File(srcDir, "notjack.zip");
    Assert.assertTrue(zip.isFile());

    // check that using a zip that is neither a Jack lib or a Jar generates a warning
    {
      JackApiToolchainBase toolchain =
          AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
      File testOut = AbstractTestTools.createTempFile("ClasspathTest", "jack");
      ByteArrayOutputStream errOut = new ByteArrayOutputStream();
      toolchain.setErrorStream(errOut);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).addToClasspath(zip)
          .srcToLib(testOut, /* zipFiles = */ true, srcDir);
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("WARNING: Bad classpath entry ignored"));
      Assert.assertTrue(errString.contains("is not a jack library"));
    }

    // check that using a zip in strict mode generates an error
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try {
      JackApiToolchainBase toolchain =
          AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
      File testOut = AbstractTestTools.createTempFile("ClasspathTest", "jack");
      toolchain.setErrorStream(errOut);
      toolchain.addProperty(Jack.STRICT_CLASSPATH.getName(), "true");
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).addToClasspath(zip)
          .srcToLib(testOut, /* zipFiles = */ true, srcDir);
    } catch (JackAbortException e) {
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("ERROR: Library reading phase"));
      Assert.assertTrue(errString.contains("is not a jack library"));
    }
  }

  @Test
  public void testWithInvalidJarInClasspath() throws Exception {
    File srcDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test004.jack");
    File jar = new File(srcDir, "invalid.jar");
    Assert.assertTrue(jar.isFile());

    // check that using an invalid jar does nothing
    {
      JackApiToolchainBase toolchain =
          AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
      File testOut = AbstractTestTools.createTempFile("ClasspathTest", "jack");
      ByteArrayOutputStream errOut = new ByteArrayOutputStream();
      toolchain.setErrorStream(errOut);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).addToClasspath(jar)
          .srcToLib(testOut, /* zipFiles = */ true, srcDir);
      Assert.assertTrue(errOut.toString().isEmpty());
    }

    // check that using an invalid jar in strict mode still does nothing
    {
      ByteArrayOutputStream errOut = new ByteArrayOutputStream();
      JackApiToolchainBase toolchain =
          AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
      File testOut = AbstractTestTools.createTempFile("ClasspathTest", "jack");
      toolchain.setErrorStream(errOut);
      toolchain.addProperty(Jack.STRICT_CLASSPATH.getName(), "true");
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).addToClasspath(jar)
          .srcToLib(testOut, /* zipFiles = */ true, srcDir);
      Assert.assertTrue(errOut.toString().isEmpty());
    }
  }

  @Test
  public void testWithEmptyJarInClasspath() throws Exception {
    File srcDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test004.jack");
    File jar = new File(srcDir, "empty.jar");
    Assert.assertTrue(jar.isFile());

    // check that using an empty jar generates a warning
    {
      JackApiToolchainBase toolchain =
          AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
      File testOut = AbstractTestTools.createTempFile("ClasspathTest", "jack");
      ByteArrayOutputStream errOut = new ByteArrayOutputStream();
      toolchain.setErrorStream(errOut);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).addToClasspath(jar)
          .srcToLib(testOut, /* zipFiles = */ true, srcDir);
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("WARNING: Bad classpath entry ignored"));
      Assert.assertTrue(errString.contains("empty.jar"));
      Assert.assertTrue(errString.contains("zip file is empty"));
    }

    // check that using an empty jar in strict mode generates an error
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try {
      JackApiToolchainBase toolchain =
          AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
      File testOut = AbstractTestTools.createTempFile("ClasspathTest", "jack");
      toolchain.setErrorStream(errOut);
      toolchain.addProperty(Jack.STRICT_CLASSPATH.getName(), "true");
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).addToClasspath(jar)
          .srcToLib(testOut, /* zipFiles = */ true, srcDir);
    } catch (JackAbortException e) {
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("ERROR: Library reading phase"));
      Assert.assertTrue(errString.contains("empty.jar"));
      Assert.assertTrue(errString.contains("zip file is empty"));
    }
  }
}
