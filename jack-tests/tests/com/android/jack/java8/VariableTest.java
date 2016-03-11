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

package com.android.jack.java8;

import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import junit.framework.Assert;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VariableTest {

  /**
   * Test that we report a compilation error when a lambda attempt to access an effectively not
   * final local variable.
   */
  @Test
  public void test001() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.java8.variable.test001.jack");
    File out = AbstractTestTools.createTempDir();

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    List<Class<? extends IToolchain>> excludeClazz = new ArrayList<Class<? extends IToolchain>>(2);
    excludeClazz.add(JackApiV01.class);
    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class, excludeClazz);

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .setErrorStream(errOut)
    .setSourceLevel(SourceLevel.JAVA_8);
    try {
      toolchain.srcToExe(out, /* zipFile = */ false, testFolder);
      Assert.fail();
    } catch (FrontendCompilationException e) {
      Assert.assertTrue(errOut.toString().contains(
          "Local variable value defined in an enclosing scope must be final or effectively final"));
    }

  }

  /**
   * Test that we compile correctly when a lambda access a effectively final local variable even
   * when not declared final.
   */
  @Test
  public void test002() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.java8.variable.test002.jack");
    File out = AbstractTestTools.createTempDir();

    List<Class<? extends IToolchain>> excludeClazz = new ArrayList<Class<? extends IToolchain>>(2);
    excludeClazz.add(JackApiV01.class);
    excludeClazz.add(JillBasedToolchain.class);
    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeClazz);

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .setSourceLevel(SourceLevel.JAVA_8)
    .srcToExe(out, /* zipFile = */ false, testFolder);

  }

}
