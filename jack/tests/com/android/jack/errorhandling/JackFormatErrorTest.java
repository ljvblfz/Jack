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

import com.android.jack.Main;
import com.android.jack.Options;
import com.android.jack.Options.VerbosityLevel;
import com.android.jack.TestTools;
import com.android.jack.jayce.JayceFormatException;
import com.android.jack.jayce.JayceVersionException;
import com.android.jack.jayce.JayceWriter;
import com.android.jack.jayce.v0002.Version;
import com.android.jack.load.JackLoadingException;

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
   * Checks that compilation fails correctly when jack file is corrupted.
   */
  @Test
  public void testJackFormatError001() throws Exception {
    TestingEnvironment ite = new TestingEnvironment();

    ite.addFile(ite.getJackFolder(), "jack.incremental", "A.jayce",
        "jayce(" + JayceWriter.DEFAULT_MAJOR_VERSION + "." + Version.CURRENT_MINOR + ")Corrupted");

    ite.addFile(ite.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    Options options = new Options();
    options.setVerbosityLevel(VerbosityLevel.DEBUG);
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(
        TestTools.getDefaultBootclasspathString() + File.pathSeparator + ite.getJackFolder());

    try {
      ite.startErrRedirection();
      ite.compile(options);
      Assert.fail();
    } catch (JackLoadingException e) {
      // Failure is ok since jack file is corrupted.
      Assert.assertTrue(e.getCause() instanceof JayceFormatException);
    } finally {
<<<<<<< HEAD
      Assert.assertEquals("", ite.endErrRedirection());
=======
      Assert.assertTrue(ite.endErrRedirection().contains("is an invalid library"));
      Assert.assertTrue(ite.endErrRedirection().contains(
          "Unexpected node NForStatement, NDeclaredType was expected"));
>>>>>>> 1eb87cf... Disable logs in Error, Warning and Info verbose levels
    }
  }

  /**
   * Checks that compilation fails correctly when jack file header is corrupted.
   */
  @Test
  public void testJackFormatError002() throws Exception {
    TestingEnvironment ite = new TestingEnvironment();

    ite.addFile(ite.getJackFolder(), "jack.incremental", "A.jayce",
        "jayce()");

    ite.addFile(ite.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    Options options = new Options();
    options.setVerbosityLevel(VerbosityLevel.DEBUG);
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(
        TestTools.getDefaultBootclasspathString() + File.pathSeparator + ite.getJackFolder());

    try {
      ite.startErrRedirection();
      ite.compile(options);
      Assert.fail();
    } catch (JackLoadingException e) {
      // Failure is ok since jack file header is corrupted.
      Assert.assertTrue(e.getCause() instanceof JayceFormatException);
    } finally {
<<<<<<< HEAD
      Assert.assertEquals("", ite.endErrRedirection());
=======
      Assert.assertTrue(ite.endErrRedirection().contains("is an invalid library"));
      Assert.assertTrue(ite.endErrRedirection().contains("Invalid Jayce header"));
>>>>>>> 1eb87cf... Disable logs in Error, Warning and Info verbose levels
    }
  }

  /**
   * Checks that compilation fails correctly when jack file is not longer supported.
   */
  @Test
  public void testJackFormatError003() throws Exception {
    TestingEnvironment ite = new TestingEnvironment();

    ite.addFile(ite.getJackFolder(), "jack.incremental", "A.jayce",
        "jayce(0.0)");

    ite.addFile(ite.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    Options options = new Options();
    options.setVerbosityLevel(VerbosityLevel.DEBUG);
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(
        TestTools.getDefaultBootclasspathString() + File.pathSeparator + ite.getJackFolder());

    try {
      ite.startErrRedirection();
      ite.compile(options);
      Assert.fail();
    } catch (JackLoadingException e) {
      // Failure is ok since jack file header is corrupted.
      Assert.assertTrue(e.getCause() instanceof JayceVersionException);
    } finally {
<<<<<<< HEAD
      Assert.assertEquals("", ite.endErrRedirection());
=======
      Assert.assertTrue(ite.endErrRedirection().contains("is an invalid library"));
      Assert.assertTrue(ite.endErrRedirection().contains("Jayce version 0 not supported"));
>>>>>>> 1eb87cf... Disable logs in Error, Warning and Info verbose levels
    }
  }
}
