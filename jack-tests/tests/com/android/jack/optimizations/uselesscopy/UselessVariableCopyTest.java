/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.optimizations.uselesscopy;

import com.android.jack.TestTools;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;

import junit.framework.Assert;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

public class UselessVariableCopyTest {

  /**
   * Verify that generated dex does not contains useless 'mov' instructions.
   */
  @Test
  @KnownIssue
  public void test001() throws Exception {
    File out = AbstractTestTools.createTempDir();
    File outDex = new File(out, "classes.dex");
    JackBasedToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.disableDxOptimizations();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(out, /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.optimizations.uselesscopy.test001.jack"));

    DexFile dexFile = new DexFile(outDex);
    CodeItem ci =
        TestTools.getEncodedMethod(dexFile,
            "Lcom/android/jack/optimizations/uselesscopy/test001/jack/Test001;", "test",
            "(Ljava/lang/Object;)Z").codeItem;

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
