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

import com.android.jack.backend.dex.DexFileWriter;

import junit.framework.Assert;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class ConstantTest {

  @Nonnull
  private static final File[] BOOTCLASSPATH = TestTools.getDefaultBootclasspath();

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testClazz() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("constant/clazz")));
  }

  @Test
  public void test001() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("constant/test001")));
  }

  @Test
  public void test002() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("constant/test002")));
  }

  @Test
  public void test003() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("constant/test003")));
  }

  @Test
  public void test004() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("constant/test004")));
  }

  @Test
  public void test005() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("constant/test005")));

    File outFolder = TestTools.createTempDir("uselessConstantInstructions", "dex");
    File out = new File(outFolder, DexFileWriter.DEX_FILENAME);
    TestTools.compileSourceToDex(new Options(),
        TestTools.getJackTestsWithJackFolder("constant/test005"),
        TestTools.getClasspathAsString(BOOTCLASSPATH), outFolder, false);

    DexFile dexFile = new DexFile(out);
    CodeItem ci =
        TestTools.getEncodedMethod(dexFile, "Lcom/android/jack/constant/test005/jack/Constant005;",
            "test", "()I").codeItem;

    Assert.assertEquals(7, countOpcode(ci, Opcode.CONST_4));
  }

  @Nonnegative
  private int countOpcode(@Nonnull CodeItem codeItem, @Nonnull Opcode opcode) {
    int countOpcode = 0;
    for (Instruction inst : codeItem.getInstructions()) {
      if (inst.opcode == opcode) {
        countOpcode++;
      }
    }
    return countOpcode;
  }

  @Test
  public void test006() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("constant/test006")));
  }

  @Test
  public void test007() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("constant/test007")));
  }

  @Test
  public void test008() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("constant/test008")));
  }
}
