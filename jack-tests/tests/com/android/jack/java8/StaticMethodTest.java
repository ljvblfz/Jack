/*
 * Copyright (C) 2015 The Android Open Source Project
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

import org.junit.Test;

import javax.annotation.Nonnull;

import com.android.jack.Options;
import com.android.jack.backend.dex.compatibility.AndroidCompatibilityChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;



/**
 * JUnit test for compilation of static method.
 */
public class StaticMethodTest {

  private RuntimeTestInfo STATICTMETHOD001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.staticmethod.test001"),
      "com.android.jack.java8.staticmethod.test001.jack.Tests");

  private RuntimeTestInfo STATICTMETHOD002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.staticmethod.test002"),
      "com.android.jack.java8.staticmethod.test002.jack.Tests");

  private RuntimeTestInfo STATICTMETHOD003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.java8.staticmethod.test003"),
      "com.android.jack.java8.staticmethod.test003.jack.Tests");

  @Test
  public void testStaticMethod001() throws Exception {
    run(STATICTMETHOD001);
  }

  @Test
  public void testStaticMethod002() throws Exception {
    run(STATICTMETHOD002);
  }

  @Test
  public void testStaticMethod003() throws Exception {
    run(STATICTMETHOD003);
  }

  private void run(@Nonnull RuntimeTestInfo rti) throws Exception {
    new RuntimeTestHelper(rti)
        .setSourceLevel(SourceLevel.JAVA_8)
        .addProperty(
            Options.ANDROID_MIN_API_LEVEL.getName(),
            String.valueOf(AndroidCompatibilityChecker.N_API_LEVEL))
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .addIgnoredCandidateToolchain(JackApiV01.class)
        .compileAndRunTest();
  }
}
