/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.experimental.incremental;

import com.google.common.io.ByteStreams;

import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.library.FileType;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.LibraryIOException;
import com.android.jack.test.helper.IncrementalTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.TwoStepsToolchain;
import com.android.sched.vfs.InputVFile;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * JUnit tests checking errors and incremental support.
 */
public class DependenciesTest017 {

  /**
   * Check that an incremental build can recover after an exception.
   */
  @Test
  public void testError1() throws Exception {

    IncrementalTestHelper helper =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    File source1 = helper.addJavaFile("jack.source", "Source1.java", "package jack.source; \n"
        + "public class Source1 { \n"
        + "public m(){} }"); // missing return type

    helper.addJavaFile("jack.source", "Source2.java", "package jack.source; \n"
        + "public class Source2 extends Source1 { \n"
        + "@Override public void m(){} }");

    File outputDex1 = AbstractTestTools.createTempDir();

    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(3);
    excludeList.add(JillBasedToolchain.class);
    excludeList.add(IncrementalToolchain.class);
    excludeList.add(TwoStepsToolchain.class);
    // API-only because we catch an exception
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeList);
    File outputJack = AbstractTestTools.createTempFile("output", toolchain.getLibraryExtension());

    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.setErrorStream(ByteStreams.nullOutputStream()); // ignore stderr
    try {
      toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex1,
          /* zipFiles = */ false, helper.getSourceFolder());
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // expected
    }

    // check the content of the incremental dir
    Assert.assertEquals(0, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));

    source1 = helper.addJavaFile("jack.source", "Source1.java", "package jack.source; \n"
        + "public class Source1 { \n"
        + "public void m(){} }");

    File outputDex2 = AbstractTestTools.createTempDir();

    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex2,
        /* zipFiles = */ false, helper.getSourceFolder());

    // check the content of the incremental dir
    Assert.assertEquals(2, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));

    // check the content of the output jack lib
    Assert.assertEquals(2, getCount(outputJack, FileType.JAYCE));
  }

  /**
   * Check that an incremental build can recover after an exception, after a first successful run.
   */
  @Test
  public void testError2() throws Exception {

    IncrementalTestHelper helper =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    File source1 = helper.addJavaFile("jack.source", "Source1.java", "package jack.source; \n"
        + "public class Source1 { \n"
        + "public void m(){} }");

    helper.addJavaFile("jack.source", "Source2.java", "package jack.source; \n"
        + "public class Source2 extends Source1 { \n"
        + "@Override public void m(){} }");

    helper.addJavaFile("jack.source", "UnrelatedSource.java", "package jack.source; \n"
        + "public class UnrelatedSource { \n"
        + "public void u(){} }");

    File outputDex1 = AbstractTestTools.createTempDir();

    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(3);
    excludeList.add(JillBasedToolchain.class);
    excludeList.add(IncrementalToolchain.class);
    excludeList.add(TwoStepsToolchain.class);
    // API-only because we catch an exception
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeList);
    File outputJack = AbstractTestTools.createTempFile("output", toolchain.getLibraryExtension());

    File[] defaultClasspath = toolchain.getDefaultBootClasspath();
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex1, /* zipFiles = */ false,
        helper.getSourceFolder());

    // check the content of the incremental dir
    Assert.assertEquals(3, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));

    source1 = helper.addJavaFile("jack.source", "Source1.java", "package jack.source; \n"
        + "public class Source1 { \n"
        + "public m(){} }"); // missing return type

    File outputDex2 = AbstractTestTools.createTempDir();

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeList);
    outputJack = AbstractTestTools.createTempFile("output", toolchain.getLibraryExtension());

    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.setErrorStream(ByteStreams.nullOutputStream()); // ignore stderr
    try {
      toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex2,
          /* zipFiles = */ false, helper.getSourceFolder());
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // expected
    }

    // check the content of the incremental dir
    int numJayce = 1; // Source1 was modified and Source2 depends on it so both have been deleted
    Assert.assertEquals(numJayce, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));

    source1 = helper.addJavaFile("jack.source", "Source1.java", "package jack.source; \n"
        + "public class Source1 { \n"
        + "public void m(){}; public void n(){}; }");

    helper.addJavaFile("jack.source", "Source3.java", "package jack.source; \n"
        + "public class Source3 extends Source1 { \n"
        + "@Override public void m(){}; @Override public void n(){}; }");

    helper.addJavaFile("jack.source", "Source4.java", "package jack.source; \n"
        + "public class Source4 extends Source2 { \n"
        + "@Override public void m(){};}");

    File outputDex3 = AbstractTestTools.createTempDir();

    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    toolchain.setIncrementalFolder(helper.getCompilerStateFolder());
    toolchain.setOutputJack(outputJack, /* zipFiles = */ true);
    toolchain.addToClasspath(defaultClasspath).srcToExe(outputDex3,
        /* zipFiles = */ false, helper.getSourceFolder());

    // check the content of the incremental dir
    Assert.assertEquals(5, getCount(helper.getCompilerStateFolder(), FileType.JAYCE));

    // check the content of the output jack lib
    Assert.assertEquals(5, getCount(outputJack, FileType.JAYCE));
  }

  @Nonnegative
  private int getCount(@Nonnull File lib, @Nonnull FileType fileType) throws LibraryIOException {
    int size = 0;
    try (InputJackLibrary compilerStateLib = AbstractTestTools.getInputJackLibrary(lib)) {
      Iterator<InputVFile> jayceIter = compilerStateLib.iterator(fileType);
      while (jayceIter.hasNext()) {
        size++;
        jayceIter.next();
      }
    }
    return size;
  }
}

