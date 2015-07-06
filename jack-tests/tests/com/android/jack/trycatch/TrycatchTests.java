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

package com.android.jack.trycatch;

import com.android.jack.TestTools;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import junit.framework.Assert;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

public class TrycatchTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.trycatch.test001"),
    "com.android.jack.trycatch.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.trycatch.test002"),
    "com.android.jack.trycatch.test002.dx.Tests");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.trycatch.test003"),
    "com.android.jack.trycatch.test003.dx.Tests");

  private RuntimeTestInfo TEST005 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.trycatch.test005"),
    "com.android.jack.trycatch.test005.dx.Tests");


  @Test
  @Category(RuntimeRegressionTest.class)
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test002() throws Exception {
    new RuntimeTestHelper(TEST002).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test003() throws Exception {
    new RuntimeTestHelper(TEST003).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test005() throws Exception {
    new RuntimeTestHelper(TEST005).compileAndRunTest();
  }

  /**
   * Verify that generated dex does not contains useless 'mov' instructions.
   */
  @Test
  @Ignore("Generated dex contains useless 'mov' instructions")
  public void uselessMovInstructions() throws Exception {
    File out = new File(AbstractTestTools.createTempDir(), "classes.dex");
    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        out.getParentFile(),
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.trycatch.test004"));

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

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
    rtTestInfos.add(TEST002);
    rtTestInfos.add(TEST003);
    rtTestInfos.add(TEST005);
  }
}
