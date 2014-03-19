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

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class NoPackageTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void test001() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("nopackage")));
  }

  @Test
  public void test001_throughJayce() throws Exception {
    File tmpDir = TestTools.createTempDir("NoPackageTest", "dir");

    String testName = "nopackage";
    String classpath = TestTools.getDefaultBootclasspathString();
    File sourceDir = TestTools.getJackTestsWithJackFolder(testName);
    TestTools.compileSourceToJack(
        new Options(), sourceDir, classpath, tmpDir, false);
    File tmpDex = TestTools.createTempFile("NoPackageTest", ".dex");
    TestTools.compileJackToDex(new Options(), tmpDir, tmpDex, false);
  }

}
