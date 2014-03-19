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

package com.android.jack;

import junit.framework.Assert;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Code.Format.PackedSwitchDataPseudoInstruction;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * JUnit test for compilation of static field access.
 */
public class SwitchesTest {

  @Nonnull
  private static final File[] BOOTCLASSPATH = TestTools.getDefaultBootclasspath();

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testCompile1() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("switchstatement/test001")));
  }

  @Test
  public void testCompile2() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("switchstatement/test002")));
  }


  @Test
  public void testCompile2AsJackThenDex() throws Exception {
    File outJackTmp = TestTools.createTempDir("switchstatement2", ".jack");
    try {

      {
        // build as jack
        Options options = TestTools.buildCommandLineArgs(
            TestTools.getJackTestsWithJackFolder("switchstatement/test002"));
        options.setJayceOutputDir(outJackTmp);
        TestTools.runCompilation(options);
      }

      {
        // build dex from jack

        File emptySource = new File("Empty.java");
        if (!emptySource.exists()) {
          if (!emptySource.createNewFile()) {
            throw new AssertionError("Failed to create " + emptySource.getAbsolutePath());
          }
        }

        Options options = TestTools.buildCommandLineArgs(null, null, emptySource);
        List<File> l = new ArrayList<File>();
        l.add(outJackTmp);
        options.setJayceImports(l);
        TestTools.runCompilation(options);
      }
    } catch (Exception e) {
      System.err.println("Not deleting temp files of failed test in " +
          outJackTmp.getAbsolutePath());
      throw e;
    }
  }

  @Test
  public void testCompile3() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("switchstatement/test003")));
  }

  @Test
  public void testCompile4() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("switchstatement/test004")));
  }

  @Test
  public void testCompile7() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("switchstatement/test007")));
  }

  @Test
  public void testCompile8() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(
        TestTools.getJackTestsWithJackFolder("switchstatement/test008")));
  }

  /**
   * Test allowing to check that 'packed-switch-payload' into generated dex is as small as possible.
   */
  @Test
  public void testCompile9() throws Exception {
    File out = TestTools.createTempFile("packedSwitchPayloadTest", ".dex");

    TestTools.compileSourceToDex(new Options(),
        TestTools.getJackTestsWithJackFolder("switchstatement/test009"),
        TestTools.getClasspathAsString(BOOTCLASSPATH), out, false);

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
}
