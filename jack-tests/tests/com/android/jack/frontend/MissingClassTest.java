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

package com.android.jack.frontend;

import com.android.jack.test.category.KnownBugs;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class MissingClassTest {

  @Test
  @Category(KnownBugs.class)
  public void test001() throws Exception {
    File outJackTmpMissing = AbstractTestTools.createTempDir();
    File outJackTmpSuper = AbstractTestTools.createTempDir();
    File outJackTmpTest = AbstractTestTools.createTempDir();

    IToolchain toolchain =  AbstractTestTools.getCandidateToolchain();

    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        outJackTmpMissing,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test001.jack.missing"));

    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath())
            + File.pathSeparatorChar + outJackTmpMissing.getPath(),
        outJackTmpSuper,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test001.jack.sub2"));

    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath())
            + File.pathSeparatorChar + outJackTmpSuper.getPath(),
        outJackTmpTest,
        /* zipFiles= */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.frontend.test001.jack.test"));

  }

}
