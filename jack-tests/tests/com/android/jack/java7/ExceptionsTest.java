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
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * JUnit test for compilation of Java 7 features
 */
public class ExceptionsTest {



  @Test
  public void java7Exception001() throws Exception {
    compileJava7Test("test001");
  }

  @Test
  public void java7Exception002() throws Exception {
    compileJava7Test("test002");
  }

  @Test
  public void java7Exception003() throws Exception {
    compileJava7Test("test003");
  }

  @Test
  public void java7Exception004() throws Exception {
    compileJava7Test("test004");
  }

  @Test
  public void java7Exception005() throws Exception {
    compileJava7Test("test005");
  }

  private void compileJava7Test(@Nonnull String name) throws Exception {
    JackBasedToolchain jackBasedToolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    jackBasedToolchain.setSourceLevel(SourceLevel.JAVA_7)
    .addToClasspath(jackBasedToolchain.getDefaultBootClasspath())
    .srcToExe(
        AbstractTestTools.createTempDir(), /* zipFile = */ false, new File(
            AbstractTestTools.getTestRootDir("com.android.jack.java7.exceptions." + name), "jack"));
  }
}
