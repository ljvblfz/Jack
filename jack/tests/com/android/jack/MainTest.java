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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

/**
 * Junit test for Main.
 */
public class MainTest {

  private static final String CLASS_BINARY_NAME = "com/android/jack/fibonacci/test001/jack/Fibo";
  private static final String CLASS_SIGNATURE = "L" + CLASS_BINARY_NAME + ";";

  @After
  public void tearDown() {
    ThreadConfig.unsetConfig();
  }

  /**
   * Verifies that Fibo can be loaded in J-AST.
   */
  @Test
  public void testLoadFiboInJAst() throws Exception {
    JSession session = TestTools.buildJAst(
        TestTools.buildCommandLineArgs(TestTools.getJackTestFromBinaryName(CLASS_BINARY_NAME)));
    JDefinedClassOrInterface fibo =
        (JDefinedClassOrInterface) session.getLookup().getType(CLASS_SIGNATURE);
    Assert.assertNotNull(fibo);
    // TODO(yroussel): make further checks
  }
}
