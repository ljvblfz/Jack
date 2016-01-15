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
import com.android.jack.library.JackLibrary;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.library.v0001.Version;
import com.android.jack.test.helper.ErrorTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchainBase;

import junit.framework.Assert;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.annotation.Nonnull;

/**
 * Test checking the behavior of Jack with an invalid Jayce format.
 */
public class JayceFormatErrorTest {

  @Nonnull
  private static final String JAYCE_SECTION_PATH = "jayce";

  /**
   * Checks that compilation fails correctly when a jayce file is corrupted.
   */
  @Test
  public void testJayceFormatError() throws Exception {
    ErrorTestHelper helper = new ErrorTestHelper();

    AbstractTestTools.createFile(new File(helper.getJackFolder(), JAYCE_SECTION_PATH), "jack.incremental",
                "A.jayce", "jayce(" + JackLibraryFactory.DEFAULT_MAJOR_VERSION + "." + Version.MINOR
                    + ")Corrupted");
    AbstractTestTools.createFile(helper.getJackFolder(), "", "jack.properties",
                JackLibrary.KEY_LIB_EMITTER + "=unknown\n"
                + JackLibrary.KEY_LIB_EMITTER_VERSION + "=0\n"
                + JackLibrary.KEY_LIB_MAJOR_VERSION + "=" + Version.MAJOR + "\n"
                + JackLibrary.KEY_LIB_MINOR_VERSION + "=" + Version.MINOR + "\n"
                + "jayce=true\n"
                + "jayce.version.major=2\n"
                + "jayce.version.minor=14\n");

    AbstractTestTools.createFile(helper.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    toolchain.setErrorStream(errOut);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(helper.getJackFolder())
      .srcToExe(helper.getOutputDexFolder(), /* zipFile= */ false, helper.getSourceFolder());
      Assert.fail();
    } catch (JackAbortException e) {
      // Failure is OK since a jayce file is corrupted.
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof LibraryFormatException);
    } finally {
      Assert.assertTrue(errOut.toString().contains("is an invalid library")); // user reporting
      Assert.assertTrue(errOut.toString().contains(
          "Unexpected node NForStatement, NDeclaredType was expected")); // system log
    }
  }

  /**
   * Checks that compilation fails correctly when a Jayce file header is corrupted.
   */
  @Test
  public void testJayceHeaderError() throws Exception {
    ErrorTestHelper helper = new ErrorTestHelper();

    AbstractTestTools.createFile(new File(helper.getJackFolder(), JAYCE_SECTION_PATH), "jack.incremental",
                "A.jayce", "jayce()");
    AbstractTestTools.createFile(helper.getJackFolder(), "", "jack.properties",
                JackLibrary.KEY_LIB_EMITTER + "=unknown\n"
                + JackLibrary.KEY_LIB_EMITTER_VERSION + "=0\n"
                + JackLibrary.KEY_LIB_MAJOR_VERSION + "=" + Version.MAJOR + "\n"
                + JackLibrary.KEY_LIB_MINOR_VERSION + "=" + Version.MINOR + "\n"
                + "jayce=true\n"
                + "jayce.version.major=2\n"
                + "jayce.version.minor=14\n");


    AbstractTestTools.createFile(helper.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    toolchain.setErrorStream(errOut);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(helper.getJackFolder())
      .srcToExe(helper.getOutputDexFolder(), /* zipFile= */ false, helper.getSourceFolder());
      Assert.fail();
    } catch (JackAbortException e) {
      // Failure is OK since a jayce file header is corrupted.
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof LibraryFormatException);
    } finally {
      Assert.assertTrue(errOut.toString().contains("is an invalid library")); // user reporting
      Assert.assertTrue(errOut.toString().contains("Invalid Jayce header")); // system log
    }
  }

  /**
   * Checks that compilation fails correctly with a Jack library containing an invalid Jayce format
   * version on classpath.
   */
  @Test
  public void testJayceVersionError() throws Exception {
    ErrorTestHelper helper = new ErrorTestHelper();

    AbstractTestTools.createFile(new File(helper.getJackFolder(), JAYCE_SECTION_PATH),
        "jack.incremental",
        "A.jayce", "jayce()");
    AbstractTestTools.createFile(helper.getJackFolder(), "", "jack.properties",
        JackLibrary.KEY_LIB_EMITTER + "=unknown\n"
        + JackLibrary.KEY_LIB_EMITTER_VERSION + "=0\n"
        + JackLibrary.KEY_LIB_MAJOR_VERSION + "=" + Version.MAJOR + "\n"
        + JackLibrary.KEY_LIB_MINOR_VERSION + "=" + Version.MINOR + "\n"
        + "jayce=true\n"
        + "jayce.version.major=0\n"
        + "jayce.version.minor=0\n");

    AbstractTestTools.createFile(helper.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    toolchain.setErrorStream(errOut);

    try {
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .addToClasspath(helper.getJackFolder())
      .srcToExe(helper.getOutputDexFolder(), /* zipFile= */ false, helper.getSourceFolder());
      Assert.fail();
    } catch (JackAbortException e) {
      // Failure is OK since Jayce format version 0 is not supported.
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof LibraryFormatException);
    } finally {
      Assert.assertTrue(errOut.toString().contains("is an invalid library")); // user reporting
      Assert.assertTrue(errOut.toString().contains("Jayce version 0 not supported")); // system log
    }
  }
}
