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

package com.android.jack.debug;

import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.optimizations.Optimizations;
import com.android.jack.test.helper.CheckDexStructureTestHelper;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JillBasedToolchain;

import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnull;

public class DebugTests {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.debug.test001"),
      "com.android.jack.debug.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.debug.test002"),
    "com.android.jack.debug.test002.dx.Tests");

  private RuntimeTestInfo TEST004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.debug.test004"),
    "com.android.jack.debug.test004.dx.Tests");

  @Test
  @Runtime
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).setWithDebugInfos(true)
        .compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  @Runtime
  public void test002() throws Exception {
    new RuntimeTestHelper(TEST002).addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .setWithDebugInfos(true)
        .addProperty(Optimizations.REMOVE_UNUSED_NON_SYNTHETIC_DEFINITION.getName(),
            String.valueOf(false))
        .addProperty(CodeItemBuilder.DEX_OPTIMIZE.getName(), "true")
        .compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  @Ignore
  public void test003() throws Exception {
    checkStructure("003");
  }

  @Test
  @Runtime
  public void test004() throws Exception {
    new RuntimeTestHelper(TEST004).addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .setWithDebugInfos(true)
        .addProperty(Optimizations.REMOVE_UNUSED_NON_SYNTHETIC_DEFINITION.getName(),
            String.valueOf(false))
        .compileAndRunTest(/* checkStructure  = */ true);
  }

  @Test
  @Ignore
  public void test005() throws Exception {
    checkStructure("005");
  }

  @Test
  @Ignore
  public void test006() throws Exception {
    checkStructure("006");
  }

  @Test
  public void test007() throws Exception {
    checkStructure("007");
  }

  @Test
  public void test008() throws Exception {
    checkStructure("008");
  }

  @Test
  public void test009() throws Exception {
    checkStructure("009");
  }

  @Test
  public void test010() throws Exception {
    checkStructure("010");
  }

  @Test
  public void test011() throws Exception {
    checkStructure("011");
  }

  @Test
  public void test012() throws Exception {
    checkStructure("012");
  }

  @Test
  @Ignore
  public void test013() throws Exception {
    checkStructure("013");
  }

  @Test
  @Ignore
  public void test014() throws Exception {
    checkStructure("014");
  }

  @Test
  @Ignore
  public void test019() throws Exception {
    checkStructure("019");
  }

  private void checkStructure(@Nonnull String testNumber) throws Exception {
    CheckDexStructureTestHelper helper = new CheckDexStructureTestHelper(
        AbstractTestTools.getTestRootDir("com.android.jack.debug.test" + testNumber + ".jack"));
    helper.setWithDebugInfo(true).compare();
  }

}
