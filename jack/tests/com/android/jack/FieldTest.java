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
 * JUnit test for compilation of fields.
 */
public class FieldTest {

  private static final File[] BOOTCLASSPATH = TestTools.getDefaultBootclasspath();

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testCompileInstance2() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("field/instance001")));
  }

  @Test
  public void testCompileInstance3() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("field/instance002")));
  }

  @Test
  public void testCompileInstance4() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("field/instance003")));
  }

  @Test
  public void testCompileInstance5() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("field/instance004")));
  }

  @Test
  public void testCompileInstance6() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("field/instance005")));
  }

  @Test
  public void testCompileStatic() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("field/static003")));
  }

  /**
   * Compiles StaticField.java into a {@code DexFile} and compares it to a dex file created
   * using a reference compiler and {@code dx}.
   */
  @Test
  public void testStatic() throws Exception {
    TestTools.checkStructure(BOOTCLASSPATH, null,
        TestTools.getJackTestsWithJackFolder("field/static003"), false /*withDebugInfo*/);
  }

  /**
   * Compiles InstanceField.java into a {@code DexFile} and compares it to a dex file created
   * using a reference compiler and {@code dx}.
   */
  @Test
  public void testInstance() throws Exception {
    TestTools.checkStructure(BOOTCLASSPATH, null,
        TestTools.getJackTestsWithJackFolder("field/instance005"), false /*withDebugInfo*/);
  }
}
