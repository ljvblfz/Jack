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
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClasspathTests {

  @BeforeClass
  public static void setUpClass() {
    ClasspathTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void test001() throws Exception {
    File libOut = AbstractTestTools.createTempDir();

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(libOut, false,
        new File(AbstractTestTools.getTestRootDir("com.android.jack.classpath.test001"), "lib"));

    File testOut = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(libOut)
    .srcToLib(testOut, false,
        new File(AbstractTestTools.getTestRootDir("com.android.jack.classpath.test001"), "jack"));
  }

  @Test
  public void test002() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();

    File[] defaultBootCp = toolchain.getDefaultBootClasspath();

    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test002");
    File outFolder = AbstractTestTools.createTempDir();

    File lib1Out = AbstractTestTools.createDir(outFolder, "lib1");
    toolchain.addToClasspath(defaultBootCp)
    .srcToLib(lib1Out,
        /* zipFiles = */ false, new File(testFolder, "lib1"));

    File lib1BisOut = AbstractTestTools.createDir(outFolder, "lib1override");
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(defaultBootCp)
    .srcToLib(lib1BisOut,
        /* zipFiles = */ false, new File(testFolder, "lib1override"));

    File lib2Out = AbstractTestTools.createDir(outFolder, "lib2");
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(defaultBootCp)
    .addToClasspath(lib1Out)
    .srcToLib(lib2Out,
    /* zipFiles = */ false, new File(testFolder, "lib2"));

    toolchain = AbstractTestTools.getCandidateToolchain();
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
      toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
      File testOut = AbstractTestTools.createTempDir();
      File testSrc = new File(testDir, "jack");
      toolchain.addToClasspath(defaultBootClasspath)
      .addToClasspath(libOut)
      .srcToLib(testOut, /* zipFiles = */ false, testSrc);
    }

    {
      // delete unused inner in classpath and check we can still compile with it
      boolean deleted = new File(libOut, FileType.JAYCE.getPrefix()
          + "/com/android/jack/classpath/test003/lib/HasInnersClasses$InnerToDelete"
          + toolchain.getLibraryElementsExtension()).delete();
      Assert.assertTrue(deleted);
      toolchain = AbstractTestTools.getCandidateToolchain();
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
    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    File srcDir = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test004.jack");
    File testOut = AbstractTestTools.createTempFile("ClasspathTest", "missing");
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(new File(srcDir, "missing.jack"))
    .srcToLib(testOut, /* zipFiles = */ true, srcDir);

    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Jack.STRICT_CLASSPATH.getName(), "true");
    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(new File(srcDir, "missing.jack"))
      .srcToLib(testOut, /* zipFiles = */ true, srcDir);
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


}
