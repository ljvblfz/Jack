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

package com.android.jack.frontend;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JProgram;
import com.android.jack.util.filter.RejectAllMethods;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class InitTest {

  @Before
  public void setUp() throws Exception {
    ConstantReuseTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void searchInit() throws Exception {
    String binaryName = "com/android/jack/fibonacci/jack/Fibo";
    JMethod init = TestTools.getJMethod(
        TestTools.getJackTestFromBinaryName(binaryName),
        "L" + binaryName + ";",
        JProgram.INIT_NAME+"()V", new RejectAllMethods());

    Assert.assertNotNull(init);
  }

  @Test
  public void testCompileEmptyConstructor() throws Exception {
    Options compilerArgs = TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("init/test001"));
    TestTools.runCompilation(compilerArgs);
  }

  @Test
  public void testVariableInit() throws Exception {
    Options compilerArgs = TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("init/test002"));
    TestTools.runCompilation(compilerArgs);
  }
}