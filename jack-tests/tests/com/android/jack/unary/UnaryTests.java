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

package com.android.jack.unary;

import com.android.jack.TestTools;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.FileChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JillBasedToolchain;

import junit.framework.Assert;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

public class UnaryTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.unary.test001"),
    "com.android.jack.unary.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.unary.test002"),
    "com.android.jack.unary.test002.dx.Tests");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.unary.test003"),
    "com.android.jack.unary.test003.dx.Tests");

  private RuntimeTestInfo TEST004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.unary.test004"),
    "com.android.jack.unary.test004.dx.Tests");

  private RuntimeTestInfo TEST005 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.unary.test005"),
      "com.android.jack.unary.test005.dx.Tests").addFileChecker(new FileChecker() {
        @Override
        public void check(@Nonnull File file) throws Exception {
          DexFile dexFile = new DexFile(file);
          EncodedMethod em =
              TestTools.getEncodedMethod(dexFile,
                  "Lcom/android/jack/unary/test005/jack/UnaryNot;", "flipBooleans",
                  "(I)Z");
          Assert.assertTrue(hasXor(em.codeItem));
        }
      }).addFileChecker(new FileChecker() {
        @Override
        public void check(@Nonnull File file) throws Exception {
          DexFile dexFile = new DexFile(file);
          EncodedMethod em =
              TestTools.getEncodedMethod(dexFile,
                  "Lcom/android/jack/unary/test005/jack/UnaryNot;", "flipBooleansTwice",
                  "(I)Z");
          Assert.assertTrue(!hasXor(em.codeItem));
        }
      }).addFileChecker(new FileChecker() {
        @Override
        public void check(@Nonnull File file) throws Exception {
          DexFile dexFile = new DexFile(file);
          EncodedMethod em =
              TestTools.getEncodedMethod(dexFile,
                  "Lcom/android/jack/unary/test005/jack/UnaryNot;", "flipBooleansWithDep",
                  "(I)Z");
          Assert.assertTrue(hasXor(em.codeItem));
        }
      }).addFileChecker(new FileChecker() {
        @Override
        public void check(@Nonnull File file) throws Exception {
          DexFile dexFile = new DexFile(file);
          EncodedMethod em =
              TestTools.getEncodedMethod(dexFile,
                  "Lcom/android/jack/unary/test005/jack/UnaryNot;", "flipBooleansTwiceWithDep",
                  "(I)Z");
          Assert.assertTrue(!hasXor(em.codeItem));
        }
      });

  private RuntimeTestInfo TEST006 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.unary.test006"),
      "com.android.jack.unary.test006.dx.Tests");

  private RuntimeTestInfo TEST007 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.unary.test007"),
      "com.android.jack.unary.test007.dx.Tests");

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
  public void test004() throws Exception {
    new RuntimeTestHelper(TEST004).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  @KnownIssue(candidate=JillBasedToolchain.class)
  public void test005() throws Exception {
    new RuntimeTestHelper(TEST005)
        .addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test006() throws Exception {
    new RuntimeTestHelper(TEST006).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test007() throws Exception {
    new RuntimeTestHelper(TEST007).compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
    rtTestInfos.add(TEST002);
    rtTestInfos.add(TEST003);
    rtTestInfos.add(TEST004);
    rtTestInfos.add(TEST005);
    rtTestInfos.add(TEST006);
    rtTestInfos.add(TEST007);
  }

  /**
   * Check if there is at least one XOR_INT instruction.
   *
   * Depending on how constants are lifted, reused..etc. We are not sure how the backend will
   * generate the XOR instruction. This method just check if there is at least one XOR instruction
   * of any kind in the code item.
   */
  private boolean hasXor(@Nonnull CodeItem codeItem) {
    for (Instruction inst : codeItem.getInstructions()) {
      switch (inst.opcode) {
        case XOR_INT:
        case XOR_INT_LIT8:
        case XOR_INT_LIT16:
        case XOR_INT_2ADDR:
          return true;
        default:
          continue;
      }
    }
    return false;
  }
}