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

package com.android.jack.optimizations.usedef;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

import com.android.jack.TestTools;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.FileChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import junit.framework.Assert;

public class UseDefTests extends RuntimeTest {
  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.optimizations.usedef.test001"),
      "com.android.jack.optimizations.usedef.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.optimizations.usedef.test002"),
      "com.android.jack.optimizations.usedef.test002.Tests").setSrcDirName("");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.optimizations.usedef.test003"),
      "com.android.jack.optimizations.usedef.test003.Tests").setSrcDirName("")
          .addFileChecker(new FileChecker() {

            @Override
            public void check(@Nonnull File file) throws Exception {
              DexFile dexFile = new DexFile(file);
              CodeItem ci = TestTools.getEncodedMethod(dexFile,
                  "Lcom/android/jack/optimizations/usedef/test003/TestClass;",
                  "callAndReturnConstLong", "()J").codeItem;
              // Checks that we find the following instructions in the exact order.
              Opcode[] expectedOpcodes =
                  new Opcode[] {Opcode.INVOKE_STATIC, Opcode.CONST_WIDE_16, Opcode.RETURN_WIDE};
              Instruction[] instructions = ci.getInstructions();
              Assert.assertEquals(expectedOpcodes.length, instructions.length);
              for (int i = 0; i < expectedOpcodes.length; ++i) {
                Assert.assertEquals("Unexpected instruction at index " + i, expectedOpcodes[i],
                    instructions[i].opcode);
              }
            }
          });

    @Test
    @Runtime
    @Category(RuntimeRegressionTest.class)
    public void test001() throws Exception {
      runUseDefTest(TEST001);
    }

    @Test
    @Runtime
    @Category(RuntimeRegressionTest.class)
    public void test002() throws Exception {
      runUseDefTest(TEST002);
    }

    @Test
    @Runtime
    @Category(RuntimeRegressionTest.class)
    public void test003() throws Exception {
      runUseDefTest(TEST003);
    }

    private static void runUseDefTest(@Nonnull RuntimeTestInfo testInfo) throws Exception {
      new RuntimeTestHelper(testInfo)
        .addProperty("jack.optimization.use-def-simplifier", Boolean.toString(true))
        .addProperty("jack.optimization.use-def-cst-simplifier", Boolean.toString(true))
        .compileAndRunTest();
    }

    @Override
    protected void fillRtTestInfos() {
      rtTestInfos.add(TEST001);
      rtTestInfos.add(TEST002);
      rtTestInfos.add(TEST003);
    }
}
