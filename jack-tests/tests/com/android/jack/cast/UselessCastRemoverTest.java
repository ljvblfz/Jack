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

package com.android.jack.cast;


import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import junit.framework.Assert;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

public class UselessCastRemoverTest {

  @Nonnull
  private static final String CAST = "com/android/jack/transformations/cast/jack/Data";

  @Nonnull
  private static final String CAST_USELESS002 = "com/android/jack/cast/useless002/jack/UselessCast";
  @Nonnull
  private static final String CAST_USELESS003 = "com/android/jack/cast/useless003/jack/UselessCast";


  @Test
  public void test001() throws Exception {
    compileAndCheckUselessCast("com.android.jack.cast.test001",
        "Lcom/android/jack/cast/test001/jack/Test001;", "get",
        "(Lcom/android/jack/cast/test001/jack/Test001;)I");
  }

  @Test
  public void test002() throws Exception {
    compileAndCheckUselessCast("com.android.jack.cast.test002",
        "Lcom/android/jack/cast/test002/jack/Test002;", "get",
        "(Lcom/android/jack/cast/test002/jack/Test002;)I");
  }

  @Test
  public void test003() throws Exception {
    compileAndCheckUselessCast("com.android.jack.cast.test003",
        "Lcom/android/jack/cast/test003/jack/Test003;", "get",
        "(Lcom/android/jack/cast/test003/jack/A;)I");
  }

  private void compileAndCheckUselessCast(@Nonnull String testPackage, @Nonnull String typeSig,
      @Nonnull String methodName, @Nonnull String methSig) throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir(testPackage);
    File outFolder = AbstractTestTools.createTempDir();
    File out = new File(outFolder, DexFileWriter.DEX_FILENAME);

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(outFolder,
        /* zipFile = */false, testFolder);

    DexFile dexFile = new DexFile(out);
    CodeItem ci = TestTools.getEncodedMethod(dexFile, typeSig, methodName, methSig).codeItem;

    Assert.assertFalse(hasOpcode(ci, Opcode.CHECK_CAST));
    Assert.assertFalse(hasOpcode(ci, Opcode.CHECK_CAST_JUMBO));
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