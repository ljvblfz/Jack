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

package com.android.jack.java7;

import com.android.jack.Main;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * JUnit test for compilation of Java 7 features
 */
public class SwitchesTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void java7Switches001() throws Exception {
    compileJava7Test("test001");
  }

  @Test
  public void java7Switches002() throws Exception {
    compileJava7Test("test002");
  }

  @Test
  public void java7Switches003() throws Exception {
    compileJava7Test("test003");
  }

  @Test
  public void java7Switches004() throws Exception {

    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain();

    File jackZipFile = AbstractTestTools.createTempFile("tmp", toolchain.getLibraryExtension());

    toolchain.setSourceLevel(SourceLevel.JAVA_7)
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        jackZipFile,
        /* zipFiles = */ true,
        AbstractTestTools.getTestRootDir("com.android.jack.java7.switches.test001.jack"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.setSourceLevel(SourceLevel.JAVA_7).libToExe(
        jackZipFile,
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false);
  }

  private void compileJava7Test(@Nonnull String name) throws Exception {
    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain();
    toolchain.setSourceLevel(SourceLevel.JAVA_7)
    .addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        AbstractTestTools.createTempDir(), /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.java7.switches." + name + ".jack"));
  }

}
