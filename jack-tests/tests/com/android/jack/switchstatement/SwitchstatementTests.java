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

package com.android.jack.switchstatement;

import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;

import junit.framework.Assert;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Code.Format.PackedSwitchDataPseudoInstruction;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class SwitchstatementTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test001"),
    "com.android.jack.switchstatement.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test002"),
    "com.android.jack.switchstatement.test002.dx.Tests");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test003"),
    "com.android.jack.switchstatement.test003.dx.Tests");

  private RuntimeTestInfo TEST004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test004"),
    "com.android.jack.switchstatement.test004.dx.Tests");

  private RuntimeTestInfo TEST005 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test005"),
    "com.android.jack.switchstatement.test005.dx.Tests");

  private RuntimeTestInfo TEST006 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test006"),
    "com.android.jack.switchstatement.test006.dx.Tests");

  private RuntimeTestInfo TEST007 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test007"),
    "com.android.jack.switchstatement.test007.dx.Tests");

  private RuntimeTestInfo TEST008 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test008"),
    "com.android.jack.switchstatement.test008.dx.Tests");

  private RuntimeTestInfo TEST010 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test010"),
    "com.android.jack.switchstatement.test010.dx.Tests");

  private RuntimeTestInfo TEST011 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test011"),
      "com.android.jack.switchstatement.test011.dx.Tests");


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
  public void testCompile002AsJackThenDex() throws Exception {
    File outJackTmp = AbstractTestTools.createTempDir();
    {
      // build as jack
      JackBasedToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
      toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
      .srcToLib(outJackTmp,
          /* zipFiles = */false,
          AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test002.jack"));
    }

    {
      // build dex from jack
      JackBasedToolchain toolchain =
          AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
      toolchain.libToExe(outJackTmp, AbstractTestTools.createTempDir(), /* zipFile = */false);
    }
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
  public void test005() throws Exception {
    new RuntimeTestHelper(TEST005).compileAndRunTest();
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
    new RuntimeTestHelper(TEST008).compileAndRunTest();
  }

  /**
   * Test allowing to check that 'packed-switch-payload' into generated dex is as small as possible.
   */
  @Test
  public void testCompile9() throws Exception {
    File outFolder = AbstractTestTools.createTempDir();
    File out = new File(outFolder, DexFileWriter.DEX_FILENAME);

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        outFolder,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test009.jack"));

    DexFile dexFile = new DexFile(out);
    EncodedMethod em =
        TestTools.getEncodedMethod(dexFile,
            "Lcom/android/jack/switchstatement/test009/jack/Switch;", "switch1",
            "(Lcom/android/jack/switchstatement/test009/jack/Switch$Num;)Z");

    MethodAnalyzer ma = new MethodAnalyzer(em, false, null);
    boolean packedSwitchDataPseudo = false;
    for (AnalyzedInstruction ai : ma.getInstructions()) {
      if (ai.getInstruction() instanceof PackedSwitchDataPseudoInstruction) {
        packedSwitchDataPseudo = true;
        Assert.assertEquals(5,
            ((PackedSwitchDataPseudoInstruction) ai.getInstruction()).getTargetCount());
      }
    }

    Assert.assertTrue(packedSwitchDataPseudo);
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test010() throws Exception {
    new RuntimeTestHelper(TEST010).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test011() throws Exception {
    new RuntimeTestHelper(TEST011).compileAndRunTest();
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
    rtTestInfos.add(TEST008);
    rtTestInfos.add(TEST010);
  }
}
