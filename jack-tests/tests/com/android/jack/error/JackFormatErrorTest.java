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
import com.android.jack.Main;
import com.android.jack.jayce.JayceProperties;
import com.android.jack.library.FileType;
import com.android.jack.library.JackLibrary;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.library.v0001.Version;
import com.android.jack.test.helper.ErrorTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchain;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * JUnit test checking Jack behavior on exceptions.
 */
public class JackFormatErrorTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Checks that compilation fails correctly when jayce file is corrupted.
   */
  @Test
  public void testJackFormatError001() throws Exception {
    ErrorTestHelper helper = new ErrorTestHelper();

    AbstractTestTools.createFile(new File(helper.getJackFolder(), FileType.JAYCE.getPrefix()), "jack.incremental",
                "A.jayce", "jayce(" + JackLibraryFactory.DEFAULT_MAJOR_VERSION + "." + Version.MINOR
                    + ")Corrupted");
    AbstractTestTools.createFile(helper.getJackFolder(), "", "jack.properties",
                JackLibrary.KEY_LIB_EMITTER + "=unknown\n"
                + JackLibrary.KEY_LIB_EMITTER_VERSION + "=0\n"
                + JackLibrary.KEY_LIB_MAJOR_VERSION + "=" + Version.MAJOR + "\n"
                + JackLibrary.KEY_LIB_MINOR_VERSION + "=" + Version.MINOR + "\n"
                + FileType.JAYCE.buildPropertyName(null /*suffix*/) + "=true\n"
                + JayceProperties.KEY_JAYCE_MAJOR_VERSION + "=2\n"
                + JayceProperties.KEY_JAYCE_MINOR_VERSION + "=14\n");

    AbstractTestTools.createFile(helper.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    toolchain.setVerbose(true);

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    toolchain.setErrorStream(errOut);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(helper.getJackFolder())
      .srcToExe(helper.getOutputDexFolder(), /* zipFile= */ false, helper.getSourceFolder());
      Assert.fail();
    } catch (JackAbortException e) {
      // Failure is ok since jack file is corrupted.
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof LibraryFormatException);
    } finally {
      Assert.assertTrue(errOut.toString().contains("is an invalid library"));
      Assert.assertTrue(errOut.toString().contains(
          "Unexpected node NForStatement, NDeclaredType was expected"));
    }
  }

  /**
   * Checks that compilation fails correctly when jack file header is corrupted.
   */
  @Test
  public void testJackFormatError002() throws Exception {
    ErrorTestHelper helper = new ErrorTestHelper();

    AbstractTestTools.createFile(new File(helper.getJackFolder(), FileType.JAYCE.getPrefix()), "jack.incremental",
                "A.jayce", "jayce()");
    AbstractTestTools.createFile(helper.getJackFolder(), "", "jack.properties",
                JackLibrary.KEY_LIB_EMITTER + "=unknown\n"
                + JackLibrary.KEY_LIB_EMITTER_VERSION + "=0\n"
                + JackLibrary.KEY_LIB_MAJOR_VERSION + "=" + Version.MAJOR + "\n"
                + JackLibrary.KEY_LIB_MINOR_VERSION + "=" + Version.MINOR + "\n"
                + FileType.JAYCE.buildPropertyName(null /*suffix*/) + "=true\n"
                + JayceProperties.KEY_JAYCE_MAJOR_VERSION + "=2\n"
                + JayceProperties.KEY_JAYCE_MINOR_VERSION + "=14\n");


    AbstractTestTools.createFile(helper.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    toolchain.setVerbose(true);

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    toolchain.setErrorStream(errOut);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(helper.getJackFolder())
      .srcToExe(helper.getOutputDexFolder(), /* zipFile= */ false, helper.getSourceFolder());
      Assert.fail();
    } catch (JackAbortException e) {
      // Failure is ok since jack file header is corrupted.
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof LibraryFormatException);
    } finally {
      Assert.assertTrue(errOut.toString().contains("is an invalid library"));
      Assert.assertTrue(errOut.toString().contains("Invalid Jayce header"));
    }
  }

  /**
   * Checks that compilation fails correctly when jack file is not longer supported.
   */
  @Test
  public void testJackFormatError003() throws Exception {
    ErrorTestHelper helper = new ErrorTestHelper();

    AbstractTestTools.createFile(new File(helper.getJackFolder(), FileType.JAYCE.getPrefix()),
        "jack.incremental",
        "A.jayce", "jayce()");
    AbstractTestTools.createFile(helper.getJackFolder(), "", "jack.properties",
        JackLibrary.KEY_LIB_EMITTER + "=unknown\n"
        + JackLibrary.KEY_LIB_EMITTER_VERSION + "=0\n"
        + JackLibrary.KEY_LIB_MAJOR_VERSION + "=" + Version.MAJOR + "\n"
        + JackLibrary.KEY_LIB_MINOR_VERSION + "=" + Version.MINOR + "\n"
        + FileType.JAYCE.buildPropertyName(null /*suffix*/) + "=true\n"
        + JayceProperties.KEY_JAYCE_MAJOR_VERSION + "=0\n"
        + JayceProperties.KEY_JAYCE_MINOR_VERSION + "=0\n");

    AbstractTestTools.createFile(helper.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    toolchain.setVerbose(true);

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    toolchain.setErrorStream(errOut);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(helper.getJackFolder())
      .srcToExe(helper.getOutputDexFolder(), /* zipFile= */ false, helper.getSourceFolder());
      Assert.fail();
    } catch (JackAbortException e) {
      // Failure is ok since jack file header is corrupted.
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof LibraryFormatException);
    } finally {
      Assert.assertTrue(errOut.toString().contains("is an invalid library"));
      Assert.assertTrue(errOut.toString().contains("Jayce version 0 not supported"));
    }
  }
}
