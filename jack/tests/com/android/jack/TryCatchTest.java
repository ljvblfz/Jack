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

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * JUnit test for compilation of try/catch.
 */
public class TryCatchTest {

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
  public void testCompile() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("trycatch/test001")));
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void testCompile2() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
            TestTools.getJackTestsWithJackFolder("trycatch/test002")));
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void testCompile3() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("trycatch/test003")));
  }

  @Test
  public void testCompile5() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("trycatch/test005")));
  }

  /**
   * Verify that generated dex does not contains useless 'mov' instructions.
   */
  @Test
  @Ignore("Generated dex contains useless 'mov' instructions")
  public void uselessMovInstructions() throws Exception {
    File out = TestTools.createTempFile("uselessMovInstructions", ".dex");
    TestTools.compileSourceToDex(new Options(),
        TestTools.getJackTestsWithJackFolder("trycatch/test004"),
        TestTools.getClasspathAsString(BOOTCLASSPATH), out, false);

    DexFile dexFile = new DexFile(out);
    CodeItem ci =
        TestTools.getEncodedMethod(dexFile, "Lcom/android/jack/trycatch/test004/jack/TryCatch;",
            "setIconAndText", "(IIILjava/lang/String;II)V").codeItem;

    Assert.assertFalse(hasOpcode(ci, Opcode.MOVE_OBJECT));
  }

  private boolean hasOpcode(@Nonnull CodeItem codeItem, @Nonnull Opcode opcode) {
    for (Instruction inst : codeItem.getInstructions()) {
      if (inst.opcode == opcode) {
        return true;
      }
    }
    return false;
  }
}
