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

package com.android.jack;

import com.android.jack.backend.jayce.ImportConflictException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class ImportTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testCompileNonConflictingSourceAndImport() throws Exception {
    File jackOut = TestTools.createTempDir("importtest", "dir");
    Options options =
        TestTools.buildCommandLineArgs(TestTools.getJackTestsWithJackFolder("fibonacci"));
    options.jayceOutDir = jackOut;
    TestTools.runCompilation(options);

    Options importOptions =
        TestTools.buildCommandLineArgs(TestTools.getJackTestsWithJackFolder("threeaddress"));
    importOptions.jayceImport.add(jackOut);
      TestTools.runCompilation(importOptions);
  }

  @Test
  public void testCompileConflictingSourceAndImport() throws Exception {
    File jackOut = TestTools.createTempDir("importtest", "dir");
    Options options =
        TestTools.buildCommandLineArgs(TestTools.getJackTestsWithJackFolder("fibonacci"));
    options.jayceOutDir = jackOut;
    TestTools.runCompilation(options);

    Options conflictOptions =
        TestTools.buildCommandLineArgs(TestTools.getJackTestsWithJackFolder("fibonacci"));
    conflictOptions.jayceImport.add(jackOut);
    try {
      TestTools.runCompilation(conflictOptions);
      Assert.fail();
    } catch (ImportConflictException e) {
      // expected
    }
  }
}
