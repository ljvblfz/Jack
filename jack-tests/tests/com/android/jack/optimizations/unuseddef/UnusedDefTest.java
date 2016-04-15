/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.optimizations.unuseddef;

import com.android.jack.TestTools;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;

import junit.framework.Assert;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class UnusedDefTest {

  /**
   * Verify that generated dex contains a 'const_4' instruction corresponding to 'o = null'
   * instruction.
   */
  @Test
  public void test001() throws Exception {
    File out = AbstractTestTools.createTempDir();
    File outDex = new File(out, "classes.dex");
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>(1);
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    toolchain.disableDxOptimizations();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(out, /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.optimizations.unuseddef.test001.jack"));

    DexFile dexFile = new DexFile(outDex);
    CodeItem ci =
        TestTools.getEncodedMethod(dexFile,
            "Lcom/android/jack/optimizations/unuseddef/test001/jack/Test001;", "test",
            "()V").codeItem;

    Assert.assertTrue(hasOpcode(ci, Opcode.CONST_4));
  }

  private boolean hasOpcode(@Nonnull CodeItem codeItem, @Nonnull Opcode opcode) {
    for (Instruction inst : codeItem.getInstructions()) {
      if (inst.opcode == opcode) {
        return true;
      }
    }
    return false;
  }


  /**
   * Non-regression test. This used to fail intermittently in UnusedDefinitionRemover.
   */
  @Test
  public void test002() throws Exception {

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);

    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.optimizations.unuseddef.test002");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }
}
