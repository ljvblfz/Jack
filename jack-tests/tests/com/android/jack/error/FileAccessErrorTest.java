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

import com.android.jack.JackUserException;
import com.android.jack.Main;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.load.JackLoadingException;
import com.android.jack.test.helper.ErrorTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchain;
import com.android.sched.util.config.PropertyIdException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * JUnit test checking Jack behavior on file access error.
 */
public class FileAccessErrorTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Checks that compilation fails correctly when folder to generate jack files is not readable.
   */
  @Test
  public void testFileAccessError001() throws Exception {
    ErrorTestHelper te = new ErrorTestHelper();

    AbstractTestTools.createJavaFile(te.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    File jackOutputFile = AbstractTestTools.createTempDir();
    if (!jackOutputFile.setReadable(false)) {
      Assert.fail("Fails to change file permissions of " + jackOutputFile.getAbsolutePath());
    }
    JackApiToolchain jackApiToolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    try {
      jackApiToolchain.srcToLib(
          AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath()),
          jackOutputFile, false, te.getSourceFolder());
      Assert.fail();
    } catch (PropertyIdException e) {
      // Failure is ok since jack output folder is not readable
    } finally {
      if (!jackOutputFile.setReadable(true)) {
        Assert.fail("Fails to change file permissions of " + jackOutputFile.getAbsolutePath());
      }
    }
  }

  /**
   * Checks that compilation fails correctly when source file is not readable.
   */
  @Test
  public void testFileAccessError003() throws Exception {
    ErrorTestHelper te = new ErrorTestHelper();

    File a = AbstractTestTools.createJavaFile(te.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");
    if (!a.setReadable(false)) {
      Assert.fail("Fails to change file permissions of " + a.getAbsolutePath());
    }

    JackApiToolchain jackApiToolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackApiToolchain.setErrorStream(errOut);

    try {
      jackApiToolchain.srcToExe(
          AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath()),
          te.getOutputDexFolder(), te.getSourceFolder());
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Failure is ok since source file is not readable
    } finally {
      if (!a.setReadable(true)) {
        Assert.fail("Fails to change file permissions of " + a.getAbsolutePath());
      }
      Assert.assertTrue(errOut.toString().contains("Permission denied"));
    }
  }

  /**
   * Checks that compilation fails correctly when jack file is not readable.
   */
  @Test
  public void testFileAccessError004() throws Exception {
    ErrorTestHelper te = new ErrorTestHelper();

    AbstractTestTools.createJavaFile(te.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    JackApiToolchain jackApiToolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    jackApiToolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath()),
        te.getJackFolder(), false, te.getSourceFolder());

    AbstractTestTools.deleteJavaFile(te.getSourceFolder(), "jack.incremental", "A.java");

    AbstractTestTools.createJavaFile(te.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try {
      for (File jackFile : AbstractTestTools.getFiles(te.getJackFolder(), ".jack")) {
        if (!jackFile.setReadable(false)) {
          Assert.fail("Fails to change file permissions of " + jackFile.getAbsolutePath());
        }
      }

      jackApiToolchain.setErrorStream(errOut);
      jackApiToolchain.srcToLib(
          AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath())
          + File.pathSeparator + te.getJackFolder().getAbsolutePath(),
          AbstractTestTools.createTempDir(), false, te.getSourceFolder());
      Assert.fail();
    } catch (JackLoadingException e) {
      // Failure is ok since jack file is not readable
    } finally {
      Assert.assertEquals("", errOut.toString());
      for (File jackFile : AbstractTestTools.getFiles(te.getJackFolder(), ".jack")) {
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

    JackApiToolchain jackApiToolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    try {

      jackApiToolchain.srcToExe(
          AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath()),
          te.getOutputDexFolder(), new File(te.getSourceFolder(), "A.java"));

      Assert.fail();
    } catch (JackUserException e) {
      // Failure is ok since source file is not readable
      Assert.assertTrue(e.getMessage().contains("A.java is missing"));
    }
  }
}
