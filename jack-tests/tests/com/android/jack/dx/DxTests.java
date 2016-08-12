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

package com.android.jack.dx;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.FileChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackApiV02;
import com.android.jack.test.toolchain.JillBasedToolchain;

import junit.framework.Assert;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Code.Format.Instruction23x;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class DxTests extends RuntimeTest {

  private RuntimeTestInfo COMPILER = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.dx.compiler"),
    "com.android.jack.dx.compiler.dx.Tests");

  private RuntimeTestInfo OPTIMIZER = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.dx.optimizer"),
    "com.android.jack.dx.optimizer.dx.Tests");

  private RuntimeTestInfo OVERLAPPING = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.dx.overlapping"),
    "com.android.jack.dx.overlapping.dx.Tests").addFileChecker(new FileChecker() {

      @Override
      public void check(@Nonnull File file) throws Exception {
        DexFile dexFile = new DexFile(file);
        EncodedMethod em =
            TestTools.getEncodedMethod(dexFile,
                "Lcom/android/jack/dx/overlapping/jack/Data;", "test002",
                "(IJJ)J");

        checkThatRegistersDoesNotOverlap(em);
      }

    }).addFileChecker(new FileChecker() {

      @Override
      public void check(@Nonnull File file) throws Exception {
        DexFile dexFile = new DexFile(file);
        EncodedMethod em =
            TestTools.getEncodedMethod(dexFile,
                "Lcom/android/jack/dx/overlapping/jack/Data;", "test001",
                "(IJJ)J");

        checkThatRegistersDoesNotOverlap(em);
      }

    });

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void compiler() throws Exception {
    new RuntimeTestHelper(COMPILER).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void optimizer() throws Exception {
    new RuntimeTestHelper(OPTIMIZER).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void overlapping() throws Exception {
    new RuntimeTestHelper(OVERLAPPING)
    .compileAndRunTest();
  }

  @Test
  @Ignore("Dx register allocator use div_int instead of div_int_2addr in some cases.")
  public void testRegallocator() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    File dexOutDir = AbstractTestTools.createTempDir();
    File outFile = new File(dexOutDir, "classes.dex");
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        dexOutDir,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.dx.regallocator.jack"));

    DexFile dexFile = new DexFile(outFile);

    CodeItem ci =
        TestTools.getEncodedMethod(dexFile, "Lcom/android/jack/dx/regallocator/jack/Data;",
            "compute1", "(I)I").codeItem;
    Assert.assertTrue(hasOpcode(ci, Opcode.DIV_INT_2ADDR));
    ci =
        TestTools.getEncodedMethod(dexFile, "Lcom/android/jack/dx/regallocator/jack/Data;",
            "compute2", "(I)I").codeItem;
    Assert.assertTrue(hasOpcode(ci, Opcode.DIV_INT_2ADDR));
  }

  @Test
  public void testDexVersionIsIncremented() throws Exception {

    String dex37Magic = new String(new byte[] {0x64, 0x65, 0x78, 0x0A, 0x30, 0x33, 0x37, 0x00});
    String dex35Magic = new String(new byte[] {0x64, 0x65, 0x78, 0x0A, 0x30, 0x33, 0x35, 0x00});

    List<Class<? extends IToolchain>> excludedToolchains =
        new ArrayList<Class<? extends IToolchain>>();
    excludedToolchains.add(JillBasedToolchain.class);

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiV02.class, excludedToolchains);

    File dexOutDir = AbstractTestTools.createTempDir();
    File outFile = new File(dexOutDir, "classes.dex");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.addProperty(Options.ANDROID_MIN_API_LEVEL.getName(), "23");
    toolchain.srcToExe(dexOutDir, /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.dx.version"));

    byte[] magic = new byte[8];

    FileInputStream fis = new FileInputStream(outFile);
    fis.read(magic);
    fis.close();

    Assert.assertEquals(dex35Magic, new String(magic));

    toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiV02.class, excludedToolchains);

    dexOutDir = AbstractTestTools.createTempDir();
    outFile = new File(dexOutDir, "classes.dex");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.addProperty(Options.ANDROID_MIN_API_LEVEL.getName(), "24");
    toolchain.srcToExe(dexOutDir, /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.dx.version"));

    magic = new byte[8];

    fis = new FileInputStream(outFile);
    fis.read(magic);
    fis.close();

    Assert.assertEquals(dex37Magic, new String(magic));

  }

  @Test
  public void testCarnacBinaryOr() throws Exception {
    File srcDir = AbstractTestTools.getTestRootDir("com.android.jack.dx.jacklibs");
    File carnacLib = new File(srcDir, "carnac-binary-or.jack");

    Assert.assertTrue(carnacLib.isFile());

    JackApiToolchainBase toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addStaticLibs(carnacLib);
    File dexOutDir = AbstractTestTools.createTempDir();
    File outFile = new File(dexOutDir, "classes.dex");
    toolchain.srcToExe(dexOutDir, /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.dx.jacklibs"));
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

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(COMPILER);
    rtTestInfos.add(OPTIMIZER);
    rtTestInfos.add(OVERLAPPING);
  }
}
