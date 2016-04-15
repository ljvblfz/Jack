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

package com.android.jack.analysis.dfa.reachingdefs;

import com.android.jack.Options;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.util.filter.SignatureMethodFilter;

import org.junit.Test;

import java.io.File;

public class ReachingDefsTests {

  @Test
  public void testDfa001() throws Exception {

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    toolchain.addProperty(Options.METHOD_FILTER.getName(), "method-with-signature");
    toolchain.addProperty(SignatureMethodFilter.METHOD_SIGNATURE_FILTER.getName(),
        "dfaWithSwitch(I)I");
    toolchain.addProperty(ReachingDefinitions.REACHING_DEFS_CHECKER.getName(), "test001Checker");

    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir = AbstractTestTools.getTestRootDir("com.android.jack.analysis.dfa.reachingdefs.test001");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }
}
