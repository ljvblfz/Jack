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
import com.android.jack.test.toolchain.JillBasedToolchain;

import com.google.common.collect.Maps;

import junit.framework.Assert;

import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.ProtoIdItem;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Code.Format.PackedSwitchDataPseudoInstruction;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

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

  private RuntimeTestInfo TEST012 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test012"),
      "com.android.jack.switchstatement.test012.dx.Tests");

  private RuntimeTestInfo TEST013 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test013"),
      "com.android.jack.switchstatement.test013.dx.Tests");

  private RuntimeTestInfo TEST014 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test014"),
      "com.android.jack.switchstatement.test014.dx.Tests");

  private RuntimeTestInfo TEST015 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test015"),
      "com.android.jack.switchstatement.test015.dx.Tests");

  private RuntimeTestInfo TEST016 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test016"),
      "com.android.jack.switchstatement.test016.dx.Tests");

  private RuntimeTestInfo TEST017 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test017"),
      "com.android.jack.switchstatement.test017.dx.Tests");

  private RuntimeTestInfo TEST018 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test018"),
      "com.android.jack.switchstatement.test018.dx.Tests");

  private RuntimeTestInfo TEST019 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test019"),
      "com.android.jack.switchstatement.test019.dx.Tests");

  private RuntimeTestInfo TEST020 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test020"),
      "com.android.jack.switchstatement.test020.dx.Tests");

  private RuntimeTestInfo TEST021 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test021"),
      "com.android.jack.switchstatement.test021.dx.Tests");

  private RuntimeTestInfo TEST022 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test022"),
      "com.android.jack.switchstatement.test022.dx.Tests");

  private RuntimeTestInfo TEST023 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test023"),
      "com.android.jack.switchstatement.test023.dx.Tests");

  private RuntimeTestInfo TEST024 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test024"),
      "com.android.jack.switchstatement.test024.dx.Tests");

  private RuntimeTestInfo TEST025 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test025"),
      "com.android.jack.switchstatement.test025.dx.Tests");

  private RuntimeTestInfo TEST026 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.switchstatement.test026"),
      "com.android.jack.switchstatement.test026.dx.Tests");

  @Nonnull
  private Map<String, String> properties = Maps.newHashMap();

  @BeforeClass
  public static void setUpClass() {
    SwitchstatementTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Before
  public void setUp() {
    // refresh the properties every time a test is executed
    properties.clear();
    properties.put("jack.optimization.enum.switch", "true");
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
        Assert.assertEquals(7,
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

  @Test
  public void testCompile012() throws Exception {
    String packageName = "com.android.jack.switchstatement.test012.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1", /*not contains Enum1*/ false));
    input.put("Enum2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2", /*not contains Enum2*/ false));

    input.put("Switch1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1", /*not contains Enum1*/ false));
    input.put("Switch2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2", /*not contains Enum2*/ false));

    input.put("SyntheticSwitchmapClass-*", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1", /*contains Enum1*/ true).
        specifyInfo(classPrefix + "Enum2", /*contains Enum2*/ true));
    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun012() throws Exception {
    runTestCase(TEST012);
  }

  @Test
  public void testCompile013() throws Exception {
    String packageName = "com.android.jack.switchstatement.test013.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1", /*not contains Enum1*/ false));
    input.put("Enum2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2", /*not contains Enum2*/ false));

    input.put("Switch1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1", /*not contains Enum1*/ false));
    input.put("Switch2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2", /*not contains Enum2*/ false));

    input.put("SyntheticSwitchmapClass-*", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1", /*contains Enum1*/ true).
        specifyInfo(classPrefix + "Enum2", /*contains Enum2*/ true));
    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun013() throws Exception {
    runTestCase(TEST013);
  }

  @Test
  public void testCompile014() throws Exception {
    String packageName = "com.android.jack.switchstatement.test014.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("Enum1$Enum1_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum2$Enum2_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("Switch1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Switch2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("SyntheticSwitchmapClass-*", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*contains Enum1*/ true).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*contains Enum2*/ true));

    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun014() throws Exception {
    runTestCase(TEST014);
  }

  @Test
  public void testCompile015() throws Exception {
    String packageName = "com.android.jack.switchstatement.test015.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("Enum1$Enum1_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum2$Enum2_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("Switch1", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Switch2", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("SyntheticSwitchmapClass-*", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*contains Enum1*/ true).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*contains Enum2*/ true));

    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun015() throws Exception {
    runTestCase(TEST015);
  }
  @Test
  public void testCompile016() throws Exception {
    String packageName = "com.android.jack.switchstatement.test016.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum1$Enum1_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));

    input.put("Enum1$Switch1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum1$Switch2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));

    input.put("SyntheticSwitchmapClass-*", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*contains Enum1*/ true));

    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun016() throws Exception {
    runTestCase(TEST016);
  }

  @Test
  public void testCompile017() throws Exception {
    String packageName = "com.android.jack.switchstatement.test017.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum1$Enum1_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));

    input.put("Enum1$Switch1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*contains Enum1*/ true));

    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun017() throws Exception {
    runTestCase(TEST017);
  }

  @Test
  public void testCompile018() throws Exception {
    String packageName = "com.android.jack.switchstatement.test018.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1", /*not contains Enum1*/ false));
    input.put("Enum2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2", /*not contains Enum2*/ false));

    input.put("Switch1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1", /*not contains Enum1*/ false));
    input.put("Switch2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2", /*not contains Enum2*/ false));

    input.put("SyntheticSwitchmapClass-*", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1", /*contains Enum1*/ true).
        specifyInfo(classPrefix + "Enum2", /*contains Enum2*/ true));
    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun018() throws Exception {
    runTestCase(TEST018);
  }


  @Test
  public void testCompile019() throws Exception {
    String packageName = "com.android.jack.switchstatement.test019.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1", /*not contains Enum1*/ false));
    input.put("Enum2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2", /*not contains Enum2*/ false));

    input.put("Switch1", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1", /*not contains Enum1*/ false));
    input.put("Switch2", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum2", /*not contains Enum2*/ false));

    input.put("SyntheticSwitchmapClass-*", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1", /*contains Enum1*/ true).
        specifyInfo(classPrefix + "Enum2", /*contains Enum2*/ true));
    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun019() throws Exception {
    runTestCase(TEST019);
  }

  @Test
  public void testCompile020() throws Exception {
    String packageName = "com.android.jack.switchstatement.test020.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Switch1", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo("java_lang_Thread$State", /*not contains State*/ false));
    input.put("Switch2", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo("java_lang_Thread$State", /*not contains State*/ false));
    input.put("SyntheticSwitchmapClass-*", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo("java_lang_Thread$State", /*contains State*/ true));
    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun020() throws Exception {
    runTestCase(TEST020);
  }

  @Test
  public void testCompile021() throws Exception {
    String packageName = "com.android.jack.switchstatement.test021.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Switch1", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo("java_lang_Thread$State", /*contains State*/ true));
    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun021() throws Exception {
    runTestCase(TEST021);
  }

  @Test
  public void testCompile022() throws Exception {
    String packageName = "com.android.jack.switchstatement.test022.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Switch1", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo("java_lang_Thread$State", /*contains State*/ true));
    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun022() throws Exception {
    runTestCase(TEST022);
  }


  @Test
  public void testCompile023() throws Exception {
    String packageName = "com.android.jack.switchstatement.test023.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Switch1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Switch2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("Enum1$Enum1_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum2$Enum2_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("SyntheticSwitchmapClass-*", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*contains Enum1*/ true).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*contains Enum2*/ true));
    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun023() throws Exception {
    runTestCase(TEST023);
  }

  @Test
  public void testCompile024() throws Exception {
    String packageName = "com.android.jack.switchstatement.test024.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Switch1", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Switch2", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("Enum1$Enum1_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum2$Enum2_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*not contains Enum2*/ false));

    input.put("SyntheticSwitchmapClass-*", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*contains Enum1*/ true).
        specifyInfo(classPrefix + "Enum2$Enum2_", /*contains Enum2*/ true));
    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun024() throws Exception {
    runTestCase(TEST024);
  }

  @Test
  public void testCompile025() throws Exception {
    String packageName = "com.android.jack.switchstatement.test025.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Enum1$Switch1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum1$Switch2", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    input.put("Enum1$Enum1_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));

    input.put("SyntheticSwitchmapClass-*", new VerifierInfo(/*contains <init>*/ true).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*contains Enum1*/ true));
    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun025() throws Exception {
    runTestCase(TEST025);
  }

  @Test
  public void testCompile026() throws Exception {
    String packageName = "com.android.jack.switchstatement.test026.jack";
    String classPrefix = packageName.replace('.', '_') + "_";
    Map<String, VerifierInfo> input = Maps.newHashMap();

    input.put("Enum1$Switch1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*contains Enum1*/ true));

    input.put("Enum1", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));

    input.put("Enum1$Enum1_", new VerifierInfo(/*not contains <init>*/ false).
        specifyInfo(classPrefix + "Enum1$Enum1_", /*not contains Enum1*/ false));
    checkCompiledCode(packageName, input);
  }

  @Test
  public void testRun026() throws Exception {
    runTestCase(TEST026);
  }

  /**
   * Compile the code with our analysis enabled, then check if it is successful.
   * @param packageName The package name of test case
   * @param classMap The map from class signature to its method signature
   */
  private void checkCompiledCode(
      @Nonnull String packageName, @Nonnull Map<String, VerifierInfo> classMap) throws Exception {
    File outFolder = AbstractTestTools.createTempDir();
    File out = new File(outFolder, DexFileWriter.DEX_FILENAME);
    List<Class<? extends IToolchain>> exclude = new ArrayList<Class<? extends IToolchain>>(1);
    exclude.add(JillBasedToolchain.class);
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class, exclude);
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      toolchain.addProperty(entry.getKey(), entry.getValue());
    }
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToExe(outFolder,
        /* zipFile = */ false, AbstractTestTools.getTestRootDir(packageName));
    DexFile dexFile = new DexFile(out);

    List<ClassDefItem> classDefs = dexFile.ClassDefsSection.getItems();
    Assert.assertEquals(classMap.size(), classDefs.size());
    for (String classSig : classMap.keySet()) {
      boolean matchClass = false;
      for (ClassDefItem classDef : classDefs) {
        String typeDescriptor = classDef.getClassType().getTypeDescriptor();
        typeDescriptor = typeDescriptor.substring(typeDescriptor.lastIndexOf('/') + 1, typeDescriptor.length() - 1);
        if (classSig.endsWith("*") && typeDescriptor.startsWith(classSig.substring(0, classSig.length() - 1))
            // match the prefix only
            || classSig.equals(typeDescriptor)) {
          VerifierInfo verifierInfo = classMap.get(classSig);
          for (Map.Entry<String, Boolean> spec : verifierInfo.getInfo().entrySet()) {
            String enumType = spec.getKey();
            Assert.assertEquals(spec.getValue().booleanValue(), containsSwitchmapInitializer(classDef, enumType));
            Assert.assertEquals(spec.getValue().booleanValue(), containsSwitchmapField(classDef, enumType));
          }
          if (verifierInfo.containsInitMethod()) {
            Assert.assertTrue(checkInitMethods(classDef));
          }
          matchClass = true;
          break;
        }
      }
      Assert.assertTrue(matchClass);
    }
  }

  /**
   * Compile and execute the instrumented code.
   * @param testInfo The runtime test information including package and class file
   */
  private void runTestCase(RuntimeTestInfo testInfo) throws Exception {
    RuntimeTestHelper rtHelper = new RuntimeTestHelper(testInfo);
    for (Map.Entry<String, String> propEntry : properties.entrySet()) {
      rtHelper.addProperty(propEntry.getKey(), propEntry.getValue());
    }
    rtHelper.compileAndRunTest();
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

  /**
   * This method is used to check if switch map initializer exists in given
   * class.
   * @param classItem The input class
   * @param enumString The enum signature in the format of A_B_Enum
   * @return Return true if it contains switch map initializer
   */
  private boolean containsSwitchmapInitializer(ClassDefItem classItem, String enumString) {
    for (EncodedMethod method : classItem.getClassData().getDirectMethods()) {
      String methodName = method.method.getMethodName().getStringValue();
      ProtoIdItem protoItem = method.method.getPrototype();
      if (methodName.equals("-get" + enumString + "SwitchesValues")
          && "()[I".equals(protoItem.getPrototypeString())) {
        // not only match class signature, but also statements
        return true;
      }
    }
    return false;
  }

  /**
   * This method is used to check if switch map field exists in given
   * class.
   * @param classItem The input class
   * @param enumString The enum signature in the format of A_B_Enum
   * @return Return true if it contains switch map field
   */
  private boolean containsSwitchmapField(ClassDefItem classItem, String enumString) {
    for (EncodedField field : classItem.getClassData().getStaticFields()) {
      String fieldName = field.field.getFieldName().getStringValue();
      if (fieldName.equals("-" + enumString + "SwitchesValues")
          && "[I".equals(field.field.getFieldType().getTypeDescriptor())) {
        return true;
      }
    }
    return false;
  }

  /**
   * This method is used to check
   * <li> if instance constructor exists in given class </li>
   * <li> if class constructor doesn't exist in given class </li>
   * @param classItem The input class
   * @return Return true if the instance exists but class constructor doesn't
   */
  private boolean checkInitMethods(ClassDefItem classItem) {
    for (EncodedMethod method : classItem.getClassData().getDirectMethods()) {
      String methodName = method.method.getShortMethodString();
      if (methodName.equals("<init>()V")) {
        return true;
      } else if (methodName.equals("<clinit>()V")) {
        return false;
      }
    }
    return false;
  }

  private class VerifierInfo {
    /**
     * Constructor.
     * @param
     */
    VerifierInfo(boolean containsInitMethod) {
      this.containsInitMethod = containsInitMethod;
    }

    /**
     * Remember the specification.
     * @param enumType The given enum type
     * @param contains If the given enum type is contained inside of class
     *
     * @return this instance of object
     */
    @Nonnull
    VerifierInfo specifyInfo(@Nonnull String enumType, @Nonnull boolean contains) {
      enumTypeMap.put(enumType, Boolean.valueOf(contains));
      return this;
    }

    /**
     * Get the verification specification information.
     *
     * @return The verification specification information
     */
    @Nonnull
    Map<String, Boolean> getInfo() {
      return enumTypeMap;
    }

    /**
     * Get the specification on if the init method is contained inside of class
     * @return true if init method is contained
     */
    @Nonnull
    boolean containsInitMethod() {
      return containsInitMethod;
    }

    // specify if the given enumType which is used as key is contained in the class
    private Map<String, Boolean> enumTypeMap = Maps.newHashMap();

    // if the associated class contains the object initialization method
    private final boolean containsInitMethod;
  }
}
