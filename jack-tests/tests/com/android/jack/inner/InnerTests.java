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

package com.android.jack.inner;

import com.android.jack.TestTools;
import com.android.jack.test.category.RedundantTests;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.comparator.ComparatorDex;
import com.android.jack.test.helper.CheckDexStructureTestHelper;
import com.android.jack.test.helper.FileChecker;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import junit.framework.Assert;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Item;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction35c;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

public class InnerTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test001"),
    "com.android.jack.inner.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test002"),
    "com.android.jack.inner.test002.dx.Tests");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test003"),
    "com.android.jack.inner.test003.dx.Tests");

  private RuntimeTestInfo TEST004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test004"),
    "com.android.jack.inner.test004.dx.Tests");

  private RuntimeTestInfo TEST005 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test005"),
    "com.android.jack.inner.test005.dx.Tests");

  private RuntimeTestInfo TEST006 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test006"),
    "com.android.jack.inner.test006.dx.Tests");

  private RuntimeTestInfo TEST007 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test007"),
    "com.android.jack.inner.test007.dx.Tests");

  private RuntimeTestInfo TEST008 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test008"),
    "com.android.jack.inner.test008.dx.Tests");

  private RuntimeTestInfo TEST009 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test009"),
    "com.android.jack.inner.test009.dx.Tests");

  private RuntimeTestInfo TEST010 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test010"),
    "com.android.jack.inner.test010.dx.Tests");

  private RuntimeTestInfo TEST011 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test011"),
    "com.android.jack.inner.test011.dx.Tests");

  private RuntimeTestInfo TEST012 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test012"),
    "com.android.jack.inner.test012.dx.Tests");

  private RuntimeTestInfo TEST013 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test013"),
    "com.android.jack.inner.test013.dx.Tests");

  private RuntimeTestInfo TEST014 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test014"),
    "com.android.jack.inner.test014.dx.Tests");

  private RuntimeTestInfo TEST015 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test015"),
    "com.android.jack.inner.test015.dx.Tests");

  private RuntimeTestInfo TEST016 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test016"),
    "com.android.jack.inner.test016.dx.Tests");

  private RuntimeTestInfo TEST017 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test017"),
    "com.android.jack.inner.test017.dx.Tests");

  private RuntimeTestInfo TEST018 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test018"),
    "com.android.jack.inner.test018.dx.Tests");

  private RuntimeTestInfo TEST019 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test019"),
    "com.android.jack.inner.test019.dx.Tests");

  private RuntimeTestInfo TEST020 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test020"),
    "com.android.jack.inner.test020.dx.Tests");

  private RuntimeTestInfo TEST021 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test021"),
    "com.android.jack.inner.test021.dx.Tests");

  private RuntimeTestInfo TEST022 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test022"),
    "com.android.jack.inner.test022.dx.Tests");

  private RuntimeTestInfo TEST023 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test023"),
    "com.android.jack.inner.test023.dx.Tests");

  private RuntimeTestInfo TEST024 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.inner.test024"),
    "com.android.jack.inner.test024.dx.Tests");

  private RuntimeTestInfo TEST026 =
      new RuntimeTestInfo(AbstractTestTools.getTestRootDir("com.android.jack.inner.test026"),
          "com.android.jack.inner.test026.dx.Tests").addFileChecker(new FileChecker() {

            @Override
            public void check(@Nonnull File file) throws Exception {
              // Check that the receiver type of method call into an accessor is correctly typed.
              DexFile dexFile = new DexFile(file);
              CodeItem ci =
                  TestTools.getEncodedMethod(dexFile, "Lcom/android/jack/inner/test026/jack/D;",
                      "-wrap1", "(Lcom/android/jack/inner/test026/jack/D;)I").codeItem;
              Assert.assertNotNull(ci);
              Instruction firstInst = ci.getInstructions()[0];
              Assert.assertTrue(firstInst instanceof Instruction35c);
              Item<?> referencedItem = ((Instruction35c) firstInst).getReferencedItem();
              Assert.assertTrue(referencedItem instanceof MethodIdItem);
              Assert.assertEquals("Lcom/android/jack/inner/test026/jack/pkg/C;",
                  ((MethodIdItem) referencedItem).getContainingClass().getTypeDescriptor());
            }
          });

  private RuntimeTestInfo TEST027 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.inner.test027"),
      "com.android.jack.inner.test027.dx.Tests");

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

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test009() throws Exception {
    new RuntimeTestHelper(TEST009).compileAndRunTest();
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
  @Category(RuntimeRegressionTest.class)
  public void test012() throws Exception {
    new RuntimeTestHelper(TEST012).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test013() throws Exception {
    new RuntimeTestHelper(TEST013).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test014() throws Exception {
    new RuntimeTestHelper(TEST014).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test015() throws Exception {
    new RuntimeTestHelper(TEST015).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test016() throws Exception {
    new RuntimeTestHelper(TEST016).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test017() throws Exception {
    new RuntimeTestHelper(TEST017).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test018() throws Exception {
    new RuntimeTestHelper(TEST018).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test019() throws Exception {
    new RuntimeTestHelper(TEST019).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test020() throws Exception {
    new RuntimeTestHelper(TEST020).compileAndRunTest(true);
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test021() throws Exception {
    new RuntimeTestHelper(TEST021).compileAndRunTest(true);
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test022() throws Exception {
    new RuntimeTestHelper(TEST022).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test023() throws Exception {
    new RuntimeTestHelper(TEST023).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test024() throws Exception {
    new RuntimeTestHelper(TEST024).compileAndRunTest();
  }

  @Test
  public void test025() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.inner.test025.jack"));
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test026() throws Exception {
    new RuntimeTestHelper(TEST026).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test027() throws Exception {
    new RuntimeTestHelper(TEST027).compileAndRunTest();
  }

  @Test
  public void testCheckStructure20() throws Exception {
    //TODO: find out why debug info check fails
    checkStructure("test020");
  }

  @Test
  @Category(RedundantTests.class)
  public void testCompile21() throws Exception {
    checkStructure("test021");
  }

  private void checkStructure(@Nonnull String test) throws Exception {
    SourceToDexComparisonTestHelper helper = new CheckDexStructureTestHelper(
        AbstractTestTools.getTestRootDir("com.android.jack.inner." + test + ".jack"));
    helper.runTest(new ComparatorDex(helper.getReferenceDex(), helper.getCandidateDex()));
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
    rtTestInfos.add(TEST009);
    rtTestInfos.add(TEST010);
    rtTestInfos.add(TEST011);
    rtTestInfos.add(TEST012);
    rtTestInfos.add(TEST013);
    rtTestInfos.add(TEST014);
    rtTestInfos.add(TEST015);
    rtTestInfos.add(TEST016);
    rtTestInfos.add(TEST017);
    rtTestInfos.add(TEST018);
    rtTestInfos.add(TEST019);
    rtTestInfos.add(TEST020);
    rtTestInfos.add(TEST021);
    rtTestInfos.add(TEST022);
    rtTestInfos.add(TEST023);
    rtTestInfos.add(TEST024);
    rtTestInfos.add(TEST026);
    rtTestInfos.add(TEST027);
  }
}
