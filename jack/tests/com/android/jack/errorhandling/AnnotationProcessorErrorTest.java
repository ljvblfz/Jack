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

import com.google.common.io.Files;

import com.android.jack.IllegalOptionsException;
import com.android.jack.JackUserException;
import com.android.jack.Main;
import com.android.jack.NothingToDoException;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.errorhandling.annotationprocessor.SimpleAnnotationProcessor;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.sched.util.config.ConfigurationException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit test checking Jack behavior when using annotation processor.
 */
public class AnnotationProcessorErrorTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Checks that compilation fails correctly when annotation processor is called without specifying
   * output folder.
   */
  @Test
  public void testAnnotationProcessorError001() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(),"jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add("-processor");
    ecjArgs.add("com.android.sched.build.SchedAnnotationProcessor");
    ecjArgs.add(te.getSourceFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(TestTools.getDefaultBootclasspathString());

    try {
      te.compile(options);
      Assert.fail();
    } catch (JackUserException e) {
      // Failure is ok since output for annotation processor is not specify.
      Assert.assertTrue(e.getMessage().contains("Unknown location"));
    }
  }

  /**
   * Checks that compilation succeed when running annotation processor.
   */
  @Test
  public void testAnnotationProcessorError002() throws Exception {
    runAnnotProcBuildingResource(new TestingEnvironment());
  }

  /**
   * Checks that last compilation failed since the resource created by annotation processor already
   * exist.
   */
  @Test
  public void testAnnotationProcessorError003() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    runAnnotProcBuildingResource(te);

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add("-d");
    ecjArgs.add(te.getTestingFolder().getAbsolutePath());
    ecjArgs.add("-processor");
    ecjArgs.add("com.android.jack.errorhandling.annotationprocessor.SimpleAnnotationProcessor");
    ecjArgs.add(te.getSourceFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(TestTools.getDefaultBootclasspathString() + File.pathSeparator
        + te.getJackFolder());

    try {
      te.startErrRedirection();
      te.compile(options);
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Failure is ok since created file already exists
    } finally {
      Assert.assertTrue(te.endErrRedirection().contains("Resource already created"));
    }
  }

  private void runAnnotProcBuildingResource(TestingEnvironment te) throws IOException,
      ConfigurationException, IllegalOptionsException, NothingToDoException, FileNotFoundException {
    File targetAnnotationFileFolder = new File(te.getSourceFolder(),
        "com/android/jack/errorhandling/annotationprocessor/");
    if (!targetAnnotationFileFolder.mkdirs()) {
      Assert.fail("Fail to create folder " + targetAnnotationFileFolder.getAbsolutePath());
    }

    Files.copy(new File(TestTools.getAndroidTop()
        + "/toolchain/jack/jack/tests/com/android/jack/errorhandling/annotationprocessor/"
        + "SimpleAnnotationTest.java"), new File(targetAnnotationFileFolder, "SimpleAnnotationTest.java"));

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(te.getSourceFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(TestTools.getDefaultBootclasspathString());
    options.setJayceOutputDir(te.getJackFolder());

    te.compile(options);

    te.addFile(te.getSourceFolder(), "jack.incremental", "A.java", "package jack.incremental;\n"
        + "import com.android.jack.errorhandling.annotationprocessor.SimpleAnnotationTest;\n"
        + "@SimpleAnnotationTest\n"
        + "public class A {}\n");

    options = new Options();
    ecjArgs = new ArrayList<String>();
    ecjArgs.add("-d");
    ecjArgs.add(te.getTestingFolder().getAbsolutePath());
    ecjArgs.add("-processor");
    ecjArgs.add("com.android.jack.errorhandling.annotationprocessor.SimpleAnnotationProcessor");
    ecjArgs.add(te.getSourceFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(TestTools.getDefaultBootclasspathString() + File.pathSeparator
        + te.getJackFolder());

    te.compile(options);

    File discoverFile = new File(te.getTestingFolder(), SimpleAnnotationProcessor.FILENAME);
    Assert.assertTrue(discoverFile.exists());
    LineNumberReader lnr = new LineNumberReader(new FileReader(discoverFile));
    Assert.assertEquals("com.android.jack.errorhandling.annotationprocessor.SimpleAnnotationTest", lnr.readLine());
    Assert.assertEquals("jack.incremental.A", lnr.readLine());
    Assert.assertNull(lnr.readLine());
    lnr.close();
  }
}
