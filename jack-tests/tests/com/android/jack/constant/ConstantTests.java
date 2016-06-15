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

package com.android.jack.constant;

import com.android.jack.TestTools;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.FileChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;

import junit.framework.Assert;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class ConstantTests extends RuntimeTest {

  private RuntimeTestInfo CLAZZ = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.constant.clazz"),
    "com.android.jack.constant.clazz.dx.Tests");

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.constant.test001"),
    "com.android.jack.constant.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.constant.test002"),
    "com.android.jack.constant.test002.dx.Tests");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.constant.test003"),
    "com.android.jack.constant.test003.dx.Tests");

  private RuntimeTestInfo TEST004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.constant.test004"),
    "com.android.jack.constant.test004.dx.Tests");

  private RuntimeTestInfo TEST005 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.constant.test005"),
    "com.android.jack.constant.test005.dx.Tests").addFileChecker(new FileChecker() {

      @Override
      public void check(@Nonnull File file) throws Exception {
        DexFile dexFile = new DexFile(file);
        CodeItem ci =
            TestTools.getEncodedMethod(dexFile, "Lcom/android/jack/constant/test005/jack/Constant005;",
                "test", "()I").codeItem;

        Assert.assertEquals(7, countOpcode(ci, Opcode.CONST_4));
      }
    }
    );

  private RuntimeTestInfo TEST006 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.constant.test006"),
    "com.android.jack.constant.test006.dx.Tests");

  private RuntimeTestInfo TEST007 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.constant.test007"),
    "com.android.jack.constant.test007.dx.Tests");

  @Test
  @Category(RuntimeRegressionTest.class)
  public void clazz() throws Exception {
    new RuntimeTestHelper(CLAZZ).compileAndRunTest();
  }

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
  public void test005() throws Exception {
    new RuntimeTestHelper(TEST005)
    .addIgnoredCandidateToolchain(JillBasedToolchain.class)
    .compileAndRunTest();
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
  @Category(RuntimeRegressionTest.class)
  public void test006() throws Exception {
    new RuntimeTestHelper(TEST006).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test007() throws Exception {
    new RuntimeTestHelper(TEST007).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test008() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.constant.test008"));
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(CLAZZ);
    rtTestInfos.add(TEST001);
    rtTestInfos.add(TEST002);
    rtTestInfos.add(TEST003);
    rtTestInfos.add(TEST004);
    rtTestInfos.add(TEST006);
    rtTestInfos.add(TEST007);
  }
}
