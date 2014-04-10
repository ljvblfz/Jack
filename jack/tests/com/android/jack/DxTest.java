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

import junit.framework.Assert;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Code.Format.Instruction23x;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * Junit tests related to dx.
 */
public class DxTest {

  @Nonnull
  private static final File[] BOOTCLASSPATH = TestTools.getDefaultBootclasspath();

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void test() throws Exception {
    TestTools.runCompilation(TestTools
        .buildCommandLineArgs(TestTools.getJackTestsWithJackFolder("dx/compiler")));
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void testDxOptimizer() throws Exception {
    TestTools.runCompilation(TestTools
        .buildCommandLineArgs(TestTools.getJackTestsWithJackFolder("dx/optimizer")));
  }

  /**
   * Verify that generated dex that not contains register overlapping.
   */
  @Test
  public void testRegisterOverlapping002() throws Exception {
    File out = TestTools.createTempFile("registerOverlapping", ".dex");
    TestTools.compileSourceToDex(new Options(),
        TestTools.getJackTestsWithJackFolder("dx/overlapping"),
        TestTools.getClasspathAsString(BOOTCLASSPATH), out, false);

    DexFile dexFile = new DexFile(out);
    EncodedMethod em =
        TestTools.getEncodedMethod(dexFile,
            "Lcom/android/jack/dx/overlapping/jack/Data;", "test002",
            "(IJJ)J");

    checkThatRegistersDoesNotOverlap(em);
  }

  /**
   * Verify that generated dex that not contains register overlapping.
   */
  @Test
  public void testRegisterOverlapping001() throws Exception {
    File out = TestTools.createTempFile("registerOverlapping", ".dex");
    TestTools.compileSourceToDex(new Options(),
        TestTools.getJackTestsWithJackFolder("dx/overlapping"),
        TestTools.getClasspathAsString(BOOTCLASSPATH), out, false);

    DexFile dexFile = new DexFile(out);
    EncodedMethod em =
        TestTools.getEncodedMethod(dexFile,
            "Lcom/android/jack/dx/overlapping/jack/Data;", "test001",
            "(IJJ)J");

    checkThatRegistersDoesNotOverlap(em);
  }

  /**
   * Verify that generated dex contains div_int_2addr.
   */
  @Test
  @Ignore("Dx register allocator use div_int instead of div_int_2addr in some cases.")
  public void testRegallocator() throws Exception {
    File out = TestTools.createTempFile("core", ".dex");
    TestTools.compileSourceToDex(new Options(),
        TestTools.getJackTestsWithJackFolder("dx/regallocator"),
        TestTools.getClasspathAsString(BOOTCLASSPATH), out, false);

    DexFile dexFile = new DexFile(out);

    CodeItem ci =
        TestTools.getEncodedMethod(dexFile, "Lcom/android/jack/dx/regallocator/jack/Data;",
            "compute1", "(I)I").codeItem;
    Assert.assertTrue(hasOpcode(ci, Opcode.DIV_INT_2ADDR));
    ci =
        TestTools.getEncodedMethod(dexFile, "Lcom/android/jack/dx/regallocator/jack/Data;",
            "compute2", "(I)I").codeItem;
    Assert.assertTrue(hasOpcode(ci, Opcode.DIV_INT_2ADDR));
  }

  private boolean hasOpcode(@Nonnull CodeItem codeItem, @Nonnull Opcode opcode) {
    for (Instruction inst : codeItem.getInstructions()) {
      if (inst.opcode == opcode) {
        return true;
      }
    }
    return false;
  }

  private void checkThatRegistersDoesNotOverlap(@Nonnull EncodedMethod em) {
    MethodAnalyzer ma = new MethodAnalyzer(em, false, null);
    boolean packedSwitchDataPseudo = false;
    for (AnalyzedInstruction ai : ma.getInstructions()) {
      if (ai.getInstruction() instanceof Instruction23x) {
        Instruction23x inst = (Instruction23x) ai.getInstruction();
        // Register overlaps in the following cases
        // v0, v1 = ..., v1, v2
        // v0, v1 = v1, v2, ...
        // v1, v2 = ..., v0, v1
        // v1, v2 = v0, v1, ...
        if (inst.getRegisterA() + 1 == inst.getRegisterC()
            || inst.getRegisterA() + 1 == inst.getRegisterB()
            || inst.getRegisterA() == inst.getRegisterC() + 1
            || inst.getRegisterA() == inst.getRegisterB() + 1) {
          Assert.fail("Register overlapping");
        }
      }
    }
  }
}
