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

package com.android.jack.ifstatement;

import com.android.jack.TestTools;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.FileChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JillBasedToolchain;

import junit.framework.Assert;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Code.Instruction;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

public class IfstatementTests extends RuntimeTest {

  private RuntimeTestInfo ADVANCEDTEST = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.ifstatement.advancedTest"),
    "com.android.jack.ifstatement.advancedTest.dx.Tests");

  private RuntimeTestInfo CFGTEST = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.ifstatement.cfgTest"),
    "com.android.jack.ifstatement.cfgTest.dx.Tests");

  private RuntimeTestInfo FASTPATH = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.ifstatement.fastpath"),
    "com.android.jack.ifstatement.fastpath.dx.Tests");

  private RuntimeTestInfo SIMPLETEST = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.ifstatement.simpleTest"),
    "com.android.jack.ifstatement.simpleTest.dx.Tests");

  private RuntimeTestInfo SHORTCONDITION = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.ifstatement.shortCondition"),
      "com.android.jack.ifstatement.shortCondition.dx.Tests").addFileChecker(new FileChecker() {
        @Override
        public void check(@Nonnull File file) throws Exception {
          DexFile dexFile = new DexFile(file);
          EncodedMethod em =
              TestTools.getEncodedMethod(dexFile,
                  "Lcom/android/jack/ifstatement/shortCondition/jack/If;", "shortCircuit1",
                  "(Z)I");
          Assert.assertTrue(!hasConditionalBranch(em.codeItem));
        }
      });

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void advancedTest() throws Exception {
    new RuntimeTestHelper(ADVANCEDTEST).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void cfgTest() throws Exception {
    new RuntimeTestHelper(CFGTEST).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void fastpath() throws Exception {
    new RuntimeTestHelper(FASTPATH).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void simpleTest() throws Exception {
    new RuntimeTestHelper(SIMPLETEST).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  @KnownIssue(candidate=JillBasedToolchain.class)
  public void shortCondition() throws Exception {
    new RuntimeTestHelper(SHORTCONDITION)
        .addProperty("jack.dex.optimizebranches", "true")
        .compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(ADVANCEDTEST);
    rtTestInfos.add(CFGTEST);
    rtTestInfos.add(FASTPATH);
    rtTestInfos.add(SIMPLETEST);
  }

  private boolean hasConditionalBranch(@Nonnull CodeItem codeItem) {
    for (Instruction inst : codeItem.getInstructions()) {
      switch (inst.opcode) {
        case IF_EQZ:
        case IF_NEZ:
        case IF_NE:
        case IF_EQ:
          return true;
        default:
          continue;
      }
    }
    return false;
  }
}
