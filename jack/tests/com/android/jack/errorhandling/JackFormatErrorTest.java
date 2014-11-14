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

package com.android.jack.errorhandling;

import com.android.jack.JackAbortException;
import com.android.jack.Main;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.jayce.JayceProperties;
import com.android.jack.library.FileType;
import com.android.jack.library.JackLibrary;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.library.v0001.Version;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    TestingEnvironment ite = new TestingEnvironment();

    ite.addFile(new File(ite.getJackFolder(), FileType.JAYCE.getPrefix()), "jack.incremental",
        "A.jayce", "jayce(" + JackLibraryFactory.DEFAULT_MAJOR_VERSION + "." + Version.MINOR
            + ")Corrupted");
    ite.addFile(ite.getJackFolder(), "", "jack.properties",
        JackLibrary.KEY_LIB_EMITTER + "=unknown\n"
        + JackLibrary.KEY_LIB_EMITTER_VERSION + "=0\n"
        + JackLibrary.KEY_LIB_MAJOR_VERSION + "=" + Version.MAJOR + "\n"
        + JackLibrary.KEY_LIB_MINOR_VERSION + "=" + Version.MINOR + "\n"
        + JayceProperties.KEY_JAYCE + "=true\n"
        + JayceProperties.KEY_JAYCE_MAJOR_VERSION + "=2\n"
        + JayceProperties.KEY_JAYCE_MINOR_VERSION + "=14\n");

    ite.addFile(ite.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(
        TestTools.getDefaultBootclasspathString() + File.pathSeparator + ite.getJackFolder());

    try {
      ite.startErrRedirection();
      ite.compile(options);
      Assert.fail();
    } catch (JackAbortException e) {
      // Failure is ok since jack file is corrupted.
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof LibraryFormatException);
    } finally {
      Assert.assertTrue(ite.endErrRedirection().contains("is invalid"));
      Assert.assertTrue(ite.endErrRedirection().contains(
          "Unexpected node NForStatement, NDeclaredType was expected"));
    }
  }

  /**
   * Checks that compilation fails correctly when jack file header is corrupted.
   */
  @Test
  public void testJackFormatError002() throws Exception {
    TestingEnvironment ite = new TestingEnvironment();

    ite.addFile(new File(ite.getJackFolder(), FileType.JAYCE.getPrefix()), "jack.incremental",
        "A.jayce", "jayce()");
    ite.addFile(ite.getJackFolder(), "", "jack.properties",
        JackLibrary.KEY_LIB_EMITTER + "=unknown\n"
        + JackLibrary.KEY_LIB_EMITTER_VERSION + "=0\n"
        + JackLibrary.KEY_LIB_MAJOR_VERSION + "=" + Version.MAJOR + "\n"
        + JackLibrary.KEY_LIB_MINOR_VERSION + "=" + Version.MINOR + "\n"
        + JayceProperties.KEY_JAYCE + "=true\n"
        + JayceProperties.KEY_JAYCE_MAJOR_VERSION + "=2\n"
        + JayceProperties.KEY_JAYCE_MINOR_VERSION + "=14\n");

    ite.addFile(ite.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(
        TestTools.getDefaultBootclasspathString() + File.pathSeparator + ite.getJackFolder());

    try {
      ite.startErrRedirection();
      ite.compile(options);
      Assert.fail();
    } catch (JackAbortException e) {
      // Failure is ok since jack file header is corrupted.
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof LibraryFormatException);
    } finally {
      Assert.assertTrue(ite.endErrRedirection().contains("is invalid"));
      Assert.assertTrue(ite.endErrRedirection().contains("Invalid Jayce header"));
    }
  }

  /**
   * Checks that compilation fails correctly when jack file is not longer supported.
   */
  @Test
  public void testJackFormatError003() throws Exception {
    TestingEnvironment ite = new TestingEnvironment();

    ite.addFile(new File(ite.getJackFolder(), FileType.JAYCE.getPrefix()), "jack.incremental",
        "A.jayce", "jayce()");
    ite.addFile(ite.getJackFolder(), "", "jack.properties",
        JackLibrary.KEY_LIB_EMITTER + "=unknown\n"
        + JackLibrary.KEY_LIB_EMITTER_VERSION + "=0\n"
        + JackLibrary.KEY_LIB_MAJOR_VERSION + "=" + Version.MAJOR + "\n"
        + JackLibrary.KEY_LIB_MINOR_VERSION + "=" + Version.MINOR + "\n"
        + JayceProperties.KEY_JAYCE + "=true\n"
        + JayceProperties.KEY_JAYCE_MAJOR_VERSION + "=0\n"
        + JayceProperties.KEY_JAYCE_MINOR_VERSION + "=0\n");

    ite.addFile(ite.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(
        TestTools.getDefaultBootclasspathString() + File.pathSeparator + ite.getJackFolder());

    try {
      ite.startErrRedirection();
      ite.compile(options);
      Assert.fail();
    } catch (JackAbortException e) {
      // Failure is ok since jack file header is corrupted.
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof LibraryFormatException);
    } finally {
      Assert.assertTrue(ite.endErrRedirection().contains("is invalid"));
      Assert.assertTrue(ite.endErrRedirection().contains("Jayce version 0 not supported"));
    }
  }
}
