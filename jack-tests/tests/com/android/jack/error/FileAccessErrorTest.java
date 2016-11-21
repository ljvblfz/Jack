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

package com.android.jack.error;

import com.android.jack.JackAbortException;
import com.android.jack.JackUserException;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.library.FileType;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.test.helper.ErrorTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.FileLocation;
import com.android.sched.vfs.InputVFile;

import junit.framework.Assert;

import org.junit.Assume;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Iterator;

/**
 * JUnit test checking Jack behavior on file access error.
 */
public class FileAccessErrorTest {

  /**
   * Checks that compilation fails correctly when folder to generate jack files is not readable.
   */
  @Test
  public void testFileAccessError001() throws Exception {
    ErrorTestHelper te = new ErrorTestHelper();

    AbstractTestTools.createFile(te.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    File jackOutputFile = AbstractTestTools.createTempDir();
    try {
      Assume.assumeTrue(jackOutputFile.setReadable(false));
      JackApiToolchainBase jackApiToolchain =
          AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

      try {
        jackApiToolchain.addToClasspath(jackApiToolchain.getDefaultBootClasspath())
        .srcToLib(jackOutputFile, /* zipFiles = */ false, te.getSourceFolder());
        Assert.fail();
      } catch (PropertyIdException e) {
        // Failure is ok since jack output folder is not readable
      }
    } finally {
      if (!jackOutputFile.setReadable(true)) {
        Assert.fail("Fails to change file permissions of " + jackOutputFile.getAbsolutePath());
      }
    }
  }

  /**
   * Checks that compilation fails correctly when a jack library folder is not readable.
   */
  @Test
  public void testFileAccessError002() throws Exception {
    ErrorTestHelper helper = new ErrorTestHelper();

    File srcFile = AbstractTestTools.createFile(helper.getSourceFolder(), "jack.incremental",
        "A.java", "package jack.incremental; \n" + "public class A {} \n");

    JackApiToolchainBase jackApiToolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    jackApiToolchain.addToClasspath(jackApiToolchain.getDefaultBootClasspath())
    .srcToLib(helper.getJackFolder(), /* zipFiles = */ false, helper.getSourceFolder());

    AbstractTestTools.deleteFile(srcFile);

    srcFile = AbstractTestTools.createFile(helper.getSourceFolder(), "jack.incremental", "B.java",
        "package jack.incremental; \n" + "public class B extends A {} \n");

    // Modify read permission of folder containing jack files
    Assume.assumeTrue(helper.getJackFolder().setReadable(false));

    jackApiToolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackApiToolchain.setErrorStream(errOut);
    jackApiToolchain.addStaticLibs(helper.getJackFolder());
    try {
      jackApiToolchain.addToClasspath(jackApiToolchain.getDefaultBootClasspath())
      .srcToExe(
          helper.getOutputDexFolder(), /* zipFile = */ false, helper.getSourceFolder());
      Assert.fail();
    } catch (JackAbortException e) {
      // Failure is ok since Jack file could not be imported since folder is not readable
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof WrongPermissionException);
    } finally {
      String errString = errOut.toString();
      Assert.assertTrue(errString.contains("ERROR: Library reading phase: directory"));
      Assert.assertTrue(errString.contains("is not readable"));
      if (!helper.getJackFolder().setReadable(true)) {
        Assert.fail("Fails to change file permissions of " + helper.getJackFolder().getAbsolutePath());
      }
    }
  }

  /**
   * Checks that compilation fails correctly when source file is not readable.
   */
  @Test
  public void testFileAccessError003() throws Exception {
    ErrorTestHelper te = new ErrorTestHelper();

    File a = AbstractTestTools.createFile(te.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");
    Assume.assumeTrue(a.setReadable(false));

    JackApiToolchainBase jackApiToolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackApiToolchain.setErrorStream(errOut);

    try {
      jackApiToolchain.addToClasspath(jackApiToolchain.getDefaultBootClasspath())
      .srcToExe(te.getOutputDexFolder(), /* zipFile = */ false, te.getSourceFolder());
      Assert.fail();
    } catch (JackUserException e) {
      // Failure is ok since source file is not readable
      Assert.assertTrue(e.getMessage().contains("is not readable"));
    } finally {
      if (!a.setReadable(true)) {
        Assert.fail("Fails to change file permissions of " + a.getAbsolutePath());
      }
    }
  }

  /**
   * Checks that compilation fails correctly when a jayce file is not readable.
   */
  @Test
  public void testFileAccessError004() throws Exception {
    ErrorTestHelper te = new ErrorTestHelper();

    AbstractTestTools.createFile(te.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    JackApiToolchainBase jackApiToolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    jackApiToolchain.addToClasspath(jackApiToolchain.getDefaultBootClasspath())
    .srcToLib(te.getJackFolder(), false, te.getSourceFolder());

    AbstractTestTools.deleteJavaFile(te.getSourceFolder(), "jack.incremental", "A.java");

    AbstractTestTools.createFile(te.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    jackApiToolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try (InputJackLibrary jackLib = AbstractTestTools.getInputJackLibrary(te.getJackFolder())) {
      Iterator<InputVFile> jackFileIter = jackLib.iterator(FileType.JAYCE);
      while (jackFileIter.hasNext()) {
        InputVFile vFile = jackFileIter.next();
        File file = new File(((FileLocation) vFile.getLocation()).getPath());
        Assume.assumeTrue(file.setReadable(false));
      }

      jackApiToolchain.setErrorStream(errOut);
      // disable the NNode cache because otherwise we may not trigger the read error
      jackApiToolchain.addProperty("jack.jayce.cache", "false");
      jackApiToolchain.addToClasspath(jackApiToolchain.getDefaultBootClasspath())
      .addToClasspath(te.getJackFolder())
      .srcToExe(AbstractTestTools.createTempDir(), false, te.getSourceFolder());
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof LibraryIOException);
      String errOutput = errOut.toString();
      Assert.assertTrue(errOutput.contains(
          "Library reading phase: I/O error when accessing ")); // user reporting
      Assert.assertTrue(errOutput.contains("is not readable")); // user reporting too
    } finally {
      for (File jackFile : AbstractTestTools.getFiles(te.getJackFolder(), JayceFileImporter.JAYCE_FILE_EXTENSION)) {
        if (!jackFile.setReadable(true)) {
          Assert.fail("Fails to change file permissions of " + jackFile.getAbsolutePath());
        }
      }
    }
  }

  /**
   * Checks that compilation fails correctly when source file does not exist.
   */
  @Test
  public void testFileAccessError005() throws Exception {
    ErrorTestHelper te = new ErrorTestHelper();

    JackApiToolchainBase jackApiToolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    try {

      jackApiToolchain.addToClasspath(jackApiToolchain.getDefaultBootClasspath())
      .srcToExe(
          te.getOutputDexFolder(), /* zipFile = */ false,
          new File(te.getSourceFolder(), "A.java"));

      Assert.fail();
    } catch (PropertyIdException e) {
      // Failure is ok since source file is not readable
      Assert.assertTrue(e.getMessage().contains("A.java"));
      Assert.assertTrue(e.getMessage().contains("does not exist"));
    }
  }

}
