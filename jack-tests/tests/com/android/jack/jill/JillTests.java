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

package com.android.jack.jill;

import com.android.jack.test.TestsProperties;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.util.List;

public class JillTests {

  @Test
  public void test001() throws Exception {
    File jasminFolder = AbstractTestTools.getTestRootDir("com.android.jack.jill.test001.jasmin");
    File jarInput = new File(jasminFolder, "jarInput.jar");

    JackBasedToolchain jackToolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File dex = AbstractTestTools.createTempFile("dex", jackToolchain.getExeExtension());
    File testSourceDir = AbstractTestTools.getTestRootDir("com.android.jack.jill.test001.jack");
    jackToolchain.addToClasspath(jackToolchain.getDefaultBootClasspath());
    jackToolchain.addStaticLibs(jarInput);
    jackToolchain.srcToExe(dex, /* zipFile = */ true, testSourceDir);


    List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners(null);
    String[] names = {"com.android.jack.jill.test001.jack.Test001"};
    for (RuntimeRunner runner : runnerList) {
      Assert.assertEquals(0,
          runner.runJUnit(new String[] {}, AbstractTestTools.JUNIT_RUNNER_NAME, names, new File[] {
              new File(TestsProperties.getJackRootDir(), "jack-tests/prebuilts/junit4-hostdex.jar"),
              dex}));
    }
  }
}
