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
import com.android.jack.library.LibraryReadingException;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchain;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ClasspathTests {

  @BeforeClass
  public static void setUpClass() {
    ClasspathTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void test001() throws Exception {
    File libOut = AbstractTestTools.createTempDir();

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToLib(AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        libOut, false,
        new File(AbstractTestTools.getTestRootDir("com.android.jack.classpath.test001"), "lib"));

    File testOut = AbstractTestTools.createTempDir();
    toolchain.srcToLib(AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath())
        + File.pathSeparatorChar + libOut.getAbsolutePath(), testOut, false,
        new File(AbstractTestTools.getTestRootDir("com.android.jack.classpath.test001"), "jack"));
  }

  @Test
  public void test002() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();

    String defaultBootCp =
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath());

    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test002");
    File outFolder = AbstractTestTools.createTempDir();

    File lib1Out = AbstractTestTools.createDir(outFolder, "lib1");
    toolchain.srcToLib(defaultBootCp,
        lib1Out,
        /* zipFiles = */ false, new File(testFolder, "lib1"));

    File lib1BisOut = AbstractTestTools.createDir(outFolder, "lib1override");
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToLib(defaultBootCp,
        lib1BisOut,
        /* zipFiles = */ false, new File(testFolder, "lib1override"));

    File lib2Out = AbstractTestTools.createDir(outFolder, "lib2");
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToLib(defaultBootCp
        + File.pathSeparatorChar + lib1Out.getAbsolutePath(), lib2Out,
    /* zipFiles = */ false, new File(testFolder, "lib2"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addStaticLibs(lib2Out);
    toolchain.srcToExe(defaultBootCp
        + File.pathSeparatorChar + lib1BisOut.getAbsolutePath(), outFolder,
        /* zipFile = */ false, new File(testFolder, "jack"));

  }

  @Test
  public void test003() throws Exception {
    File testDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test003");

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    File libOut = AbstractTestTools.createTempDir();
    File libSrc = new File(testDir, "lib");
    String defaultBootClasspath =
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath());
    toolchain.srcToLib(defaultBootClasspath, libOut, /* zipFiles = */ false, libSrc);

    {
      // reference compilation
      toolchain = AbstractTestTools.getCandidateToolchain();
      File testOut = AbstractTestTools.createTempDir();
      File testSrc = new File(testDir, "jack");
      toolchain.srcToLib(defaultBootClasspath + File.pathSeparatorChar + libOut.getAbsolutePath(),
          testOut, /* zipFiles = */ false, testSrc);
    }

    {
      // delete unused inner in classpath and check we can still compile with it
      boolean deleted =
          new File(libOut, FileType.JAYCE.getPrefix()
              + "/com/android/jack/classpath/test003/lib/HasInnersClasses$InnerToDelete.jayce")
      .delete();
      Assert.assertTrue(deleted);
      toolchain = AbstractTestTools.getCandidateToolchain();
      File testOut = AbstractTestTools.createTempDir();
      File testSrc = new File(testDir, "jack");
      toolchain.srcToLib(defaultBootClasspath + File.pathSeparatorChar + libOut.getAbsolutePath(),
          testOut, /* zipFiles = */ false, testSrc);
    }
  }

  @Test
  public void libOfLib() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    String defaultClasspath =
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath());
    File libOfLibOut = AbstractTestTools.createTempFile("libOfLibOut", toolchain.getLibraryExtension());
    File sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.liboflib.lib2");
    toolchain.srcToLib(defaultClasspath, libOfLibOut, /* zipFiles = */ true, sourceDir);

    toolchain = AbstractTestTools.getCandidateToolchain();
    File libOut = AbstractTestTools.createTempFile("libOut", toolchain.getLibraryExtension());
    sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.liboflib.lib");
    toolchain.srcToLib(defaultClasspath + File.pathSeparatorChar + libOfLibOut.getAbsolutePath(),
        libOut, /* zipFiles = */ true, sourceDir);

    toolchain = AbstractTestTools.getCandidateToolchain();
    File mainOut = AbstractTestTools.createTempFile("mainOut", toolchain.getLibraryExtension());
    sourceDir = AbstractTestTools.getTestRootDir("com.android.jack.liboflib.main");
    toolchain.srcToLib(defaultClasspath + File.pathSeparatorChar + libOut.getAbsolutePath(),
        mainOut, /* zipFiles = */ true, sourceDir);
  }

  @Test
  public void testMissingClasspathEntry() throws Exception {
    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    String defaultClasspath =
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath());
    File srcDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test004.jack");
    String classpathWithMissingEntry = defaultClasspath + File.pathSeparator +
        new File(srcDir, "missing.jack").getAbsolutePath();
    File testOut = AbstractTestTools.createTempFile("ClasspathTest", "missing");
    toolchain.srcToLib(classpathWithMissingEntry, testOut, /* zipFiles = */ true, srcDir);

    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    toolchain.addProperty(Jack.STRICT_CLASSPATH.getName(), "true");
    try {
      toolchain.srcToLib(classpathWithMissingEntry, testOut, /* zipFiles = */ true, srcDir);
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
    }
  }

  @Test
  public void testInvalidClasspathEntry() throws Exception {
    File srcDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test004.jack");
    compileWithInvalidClasspathEntry(srcDir, new File(srcDir, "Classpath004.java"));
    compileWithInvalidClasspathEntry(srcDir, new File(srcDir, "invalid.jack"));
    compileWithInvalidClasspathEntry(srcDir, new File(srcDir, "notjack.zip"));
  }

  private void compileWithInvalidClasspathEntry(File srcDir, File invalidJack) throws IOException,
      Exception {
    Assert.assertTrue(invalidJack.isFile());

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    String classpathWithInvalidEntry =
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath())
        + File.pathSeparator + invalidJack.getAbsolutePath();

    File testOut = AbstractTestTools.createTempFile("ClasspathTest", "invalid");
    toolchain.srcToLib(classpathWithInvalidEntry, testOut, /* zipFiles = */ true, srcDir);

    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    toolchain.addProperty(Jack.STRICT_CLASSPATH.getName(), "true");

    try {
      toolchain.srcToLib(classpathWithInvalidEntry, testOut, /* zipFiles = */ true, srcDir);
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
    }
  }


}
