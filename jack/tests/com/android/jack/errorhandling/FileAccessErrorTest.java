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

import com.android.jack.JackIOException;
import com.android.jack.Main;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.category.KnownBugs;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.sched.util.config.PropertyIdException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    TestingEnvironment ite = new TestingEnvironment();

    ite.addFile(ite.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(TestTools.getDefaultBootclasspathString());
    File jackOutputFile = TestTools.createTempDir("ErrorHandlingTest_", "001");
    options.setJayceOutputDir(jackOutputFile);
    if (!jackOutputFile.setReadable(false)) {
      Assert.fail("Fails to change file permissions of " + jackOutputFile.getAbsolutePath());
    }

    try {
      ite.compile(options);
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
   * Checks that compilation fails correctly when folder containing jack files is not readable.
   */
  @Test
  @Category(KnownBugs.class)
  public void testFileAccessError002() throws Exception {
    TestingEnvironment ite = new TestingEnvironment();

    ite.addFile(ite.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(TestTools.getDefaultBootclasspathString());
    File jackOutputFile = TestTools.createTempDir("ErrorHandlingTest_", "001");
    options.setJayceOutputDir(jackOutputFile);

    ite.compile(options);

    ite.deleteJavaFile(ite.getSourceFolder(), "jack.incremental", "A.java");
    ite.addFile(ite.getSourceFolder(), "jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    options = new Options();
    ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(TestTools.getDefaultBootclasspathString());
    options.addJayceImport(jackOutputFile);

    // Modify read permission of folder containing jack files
    if (!jackOutputFile.setReadable(false)) {
      Assert.fail("Fails to change file permissions of " + jackOutputFile.getAbsolutePath());
    }
    try {
      ite.startErrRedirection();
      ite.compile(options);
      Assert.fail();
    } finally {
      Assert.assertEquals("", ite.endErrRedirection());
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
    TestingEnvironment ite = new TestingEnvironment();

    File a = ite.addFile(ite.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");
    if (!a.setReadable(false)) {
      Assert.fail("Fails to change file permissions of " + a.getAbsolutePath());
    }

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(TestTools.getDefaultBootclasspathString());

    try {
      ite.compile(options);
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Failure is ok since source file is not readable
    } finally {
      if (!a.setReadable(true)) {
        Assert.fail("Fails to change file permissions of " + a.getAbsolutePath());
      }
    }
  }

  /**
   * Checks that compilation fails correctly when jack file is not readable.
   */
  @Test
  public void testFileAccessError004() throws Exception {
    TestingEnvironment ite = new TestingEnvironment();

    ite.addFile(ite.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(TestTools.getDefaultBootclasspathString());
    options.setJayceOutputDir(ite.getJackFolder());

    ite.compile(options);

    ite.deleteJavaFile(ite.getSourceFolder(), "jack.incremental", "A.java");

    ite.addFile(ite.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    options = new Options();
    ecjArgs = new ArrayList<String>();
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(
        TestTools.getDefaultBootclasspathString() + File.pathSeparator + ite.getJackFolder());

    try {
      for (File jackFile : ite.getJackFiles(ite.getJackFolder())) {
        if (!jackFile.setReadable(false)) {
          Assert.fail("Fails to change file permissions of " + jackFile.getAbsolutePath());
        }
      }
      ite.startErrRedirection();
      ite.compile(options);
    } catch (JackIOException e) {
      // Failure is ok since jack file is not readable
    } finally {
      Assert.assertEquals("", ite.endErrRedirection());
      for (File jackFile : ite.getJackFiles(ite.getJackFolder())) {
        if (!jackFile.setReadable(true)) {
          Assert.fail("Fails to change file permissions of " + jackFile.getAbsolutePath());
        }
      }
    }
  }
}
