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

package com.android.jack.enums;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class EnumTests {

  private File baseDir;

  @Before
  public void setUp() {
    baseDir = AbstractTestTools.getTestRootDir("com.android.jack.enums.test003");
  }

  @Test
  public void compileAndRunTest() throws Exception {
    new RuntimeTestHelper(new RuntimeTestInfo(baseDir, "com.android.jack.enums.test003.dx.Tests"))
        .compileAndRunTest();
  }

}
