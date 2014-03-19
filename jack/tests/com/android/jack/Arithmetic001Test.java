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

import java.io.File;

/**
 * JUnit test for compilation of arithmetic tests.
 */
public class Arithmetic001Test {
  private static final File PATH = TestTools.getJackTestsWithJackFolder("arithmetic/test001");

  private static final File[] JAVA_SOURCES = {
    new File(PATH, "Add.java"),
    new File(PATH, "And.java"),
    new File(PATH, "Div.java"),
    new File(PATH, "Mod.java"),
    new File(PATH, "Mul.java"),
    new File(PATH, "Or.java"),
    new File(PATH, "Shl.java"),
    new File(PATH, "Shr.java"),
    new File(PATH, "Sub.java"),
    new File(PATH, "Ushr.java"),
    new File(PATH, "Xor.java")
    };

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void testCompile() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(JAVA_SOURCES));
  }
}
