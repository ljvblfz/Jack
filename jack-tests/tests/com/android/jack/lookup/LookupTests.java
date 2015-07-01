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

package com.android.jack.lookup;

import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JillApiToolchainBase;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LookupTests {

  @Test
  public void test001() throws Exception {
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>();
    exclude.add(JillApiToolchainBase.class);
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain(IToolchain.class, exclude);
    File lib = AbstractTestTools.createTempDir();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        lib,
        /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.lookup.test001.lib"));

    File libOverride = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(IToolchain.class, exclude);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(
        libOverride,
        /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.lookup.test001.liboverride"));

    File jacks = AbstractTestTools.createTempDir();
    toolchain = AbstractTestTools.getCandidateToolchain(IToolchain.class, exclude);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .addToClasspath(lib)
    .srcToLib(jacks, /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.lookup.test001.jack"));

    toolchain = AbstractTestTools.getCandidateToolchain(IToolchain.class, exclude);
    toolchain.libToLib(
        new File [] {jacks, libOverride},
        AbstractTestTools.createTempFile("Lookup001", toolchain.getLibraryExtension()),
        /* zipFiles = */ true);
  }

}
