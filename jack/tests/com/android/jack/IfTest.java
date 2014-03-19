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

package com.android.jack;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for compilation of if statements.
 */
public class IfTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testCompileSimple() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("ifstatement/simpleTest")));
  }

  @Test
  public void testCompileAdvanced() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("ifstatement/advancedTest")));
  }

  @Test
  public void testCompileCfg() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("ifstatement/cfgTest")));
  }

  @Test
  public void testFastPath() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("ifstatement/fastpath")));
  }
}
