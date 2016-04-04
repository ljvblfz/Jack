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

import com.android.jack.Options;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.IncrementalToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.LegacyJillToolchain;
import com.android.jack.test.toolchain.TwoStepsToolchain;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit test checking behavior of incremental mode with other options.
 */
public class IncrementalTests {

  /**
   * Test compilation with incremental + generation of dex and jack lib
   * at the same time + import of an unpredexed library.
   * @throws Exception
   */
  @Test
  public void testIncremental001() throws Exception {

    List<Class<? extends IToolchain>> excludeList = new ArrayList<Class<? extends IToolchain>>(1);
    excludeList.add(LegacyJillToolchain.class);
    excludeList.add(IncrementalToolchain.class);
    excludeList.add(TwoStepsToolchain.class);
    excludeList.add(JillBasedToolchain.class);

    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    File unpredexedLibrary =
        AbstractTestTools.createTempFile("unpredexedLibrary", toolchain.getLibraryExtension());
    toolchain.addProperty(Options.GENERATE_DEX_IN_LIBRARY.getName(), "false");
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.srcToLib(
        unpredexedLibrary,
        /* zipFiles = */ true,
        AbstractTestTools.getTestRootDir("com.android.jack.experimental.incremental.test001.lib"));

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, excludeList);
    File library =
        AbstractTestTools.createTempFile("library", toolchain.getLibraryExtension());
    File dexFile = AbstractTestTools.createTempDir();
    toolchain.addStaticLibs(unpredexedLibrary);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.setOutputJack(library, /* zipFiles = */ true);
    toolchain.setIncrementalFolder(AbstractTestTools.createTempDir());
    toolchain.srcToExe(
        dexFile,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.experimental.incremental.test001.jack"));
  }

}
