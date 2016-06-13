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

package com.android.jack.frontend;

import com.android.jack.Options;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import junit.framework.Assert;

import org.junit.Test;

public class ECJTest {

  /**
   * Check that ECJ can correctly resolve a generic usage.
   */
  @Test
  @KnownIssue
  public void testCompileTest002() throws Exception {
    JackApiToolchainBase toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Options.INPUT_FILTER.getName(), "ordered-filter");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .setSourceLevel(SourceLevel.JAVA_7);

    try {
      toolchain.srcToExe(AbstractTestTools.createTempDir(), /* zipFile = */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.frontend.test018.jack"));
    } catch (FrontendCompilationException e) {
      Assert.fail();
    }
  }
}
