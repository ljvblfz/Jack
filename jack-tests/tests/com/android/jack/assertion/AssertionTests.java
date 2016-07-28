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

package com.android.jack.assertion;

import com.android.jack.Options;
import com.android.jack.Options.AssertionPolicy;
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

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;


public class AssertionTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.assertion.test001"),
    "com.android.jack.assertion.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.assertion.test002"),
    "com.android.jack.assertion.test002.dx.Tests");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.assertion.test003"),
    "com.android.jack.assertion.test003.dx.Tests");

  private RuntimeTestInfo TEST004 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.assertion.test004"),
      "com.android.jack.assertion.test004.dx.Tests");

  private RuntimeTestInfo TEST005 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.assertion.test005"),
      "com.android.jack.assertion.test005.dx.Tests");

  private RuntimeTestInfo TEST006 =
      new RuntimeTestInfo(AbstractTestTools.getTestRootDir("com.android.jack.assertion.test006"),
          "com.android.jack.assertion.test006.dx.Tests").addFileChecker(new FileChecker() {
            @Override
            public void check(@Nonnull File file) throws Exception {
              DexFile dexFile = new DexFile(file);
              EncodedMethod em = TestTools.getEncodedMethod(dexFile,
                  "Lcom/android/jack/assertion/test006/jack/Assertion006;", "test", "(III)V");
              // We should not need to generate any intermediate true or false values for the
              // assertion. This mean there should be zero const instructions.
              Assert.assertTrue(!hasConst(em.codeItem));
            }
          });

  @Test
  @Runtime
  // this test must be run with assertions enabled (for now, use dalvik)
  @Category(RuntimeRegressionTest.class)
  @KnownIssue
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test002() throws Exception {
    new RuntimeTestHelper(TEST002).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test003() throws Exception {
    new RuntimeTestHelper(TEST003).compileAndRunTest();
  }

  @Test
  @Runtime
  public void test004() throws Exception {
    new RuntimeTestHelper(TEST004)
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .addProperty(Options.ASSERTION_POLICY.getName(), AssertionPolicy.ALWAYS.toString())
        .compileAndRunTest();
  }

  @Test
  @Runtime
  public void test005() throws Exception {
    new RuntimeTestHelper(TEST005)
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .addProperty(Options.ASSERTION_POLICY.getName(), AssertionPolicy.NEVER.toString())
        .compileAndRunTest();
  }

  @Test
  @Runtime
  public void test006() throws Exception {
    new RuntimeTestHelper(TEST006)
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .addProperty(Options.ASSERTION_POLICY.getName(), AssertionPolicy.RUNTIME.toString())
        .compileAndRunTest();
  }


  @Override
  protected void fillRtTestInfos() {
//    rtTestInfos.add(TEST001);
    rtTestInfos.add(TEST002);
    rtTestInfos.add(TEST003);
  }

  /**
   * Check if there is at least one const_4 or const_16 instruction.
   */
  private boolean hasConst(@Nonnull CodeItem codeItem) {
    for (Instruction inst : codeItem.getInstructions()) {
      switch (inst.opcode) {
        case CONST_4:
        case CONST_16:
          return true;
        default:
          continue;
      }
    }
    return false;
  }
}
