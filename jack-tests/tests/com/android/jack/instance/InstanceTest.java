/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.instance;

import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class InstanceTest {

  @Before
  public void setUp() throws Exception {
    InstanceTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testReturnThis() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToExe(AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        new File( AbstractTestTools.getTestRootDir("com.android.jack.instance.test001.jack"),
            "Instance.java"));
  }
}
