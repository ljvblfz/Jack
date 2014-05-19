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

import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class ClasspathTest {

  @BeforeClass
  public static void setUpClass() {
    ClasspathTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void test001() throws Exception {
    File libOut = AbstractTestTools.createTempDir();

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToLib(AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        libOut, false,
        new File(AbstractTestTools.getTestRootDir("com.android.jack.classpath.test001"), "lib"));

    File testOut = AbstractTestTools.createTempDir();
    toolchain.srcToLib(AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath())
        + File.pathSeparatorChar + libOut.getAbsolutePath(), testOut, false,
        new File(AbstractTestTools.getTestRootDir("com.android.jack.classpath.test001"), "jack"));
  }

  @Test
  public void test002() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();

    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.classpath.test002");
    File outFolder = AbstractTestTools.createTempDir();

    File lib1Out = AbstractTestTools.createDir(outFolder, "lib1");
    toolchain.srcToLib(AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        lib1Out,
        /* zipFiles = */ false, new File(testFolder, "lib1"));

    File lib1BisOut = AbstractTestTools.createDir(outFolder, "lib1override");
    toolchain.srcToLib(AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        lib1BisOut,
        /* zipFiles = */false, new File(testFolder, "lib1override"));

    File lib2Out = AbstractTestTools.createDir(outFolder, "lib2");
    toolchain.srcToLib(AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath())
        + File.pathSeparatorChar + lib1Out.getAbsolutePath(), lib2Out,
    /* zipFiles = */false, new File(testFolder, "lib2"));

    toolchain.addStaticLibs(lib2Out);
    toolchain.srcToExe(AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath())
        + File.pathSeparatorChar + lib1BisOut.getAbsolutePath(), outFolder,
        new File(testFolder, "jack"));

  }
}
