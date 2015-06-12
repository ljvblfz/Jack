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

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.CheckDexStructureTestHelper;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.annotation.Nonnull;

public class DebugTests extends RuntimeTest {

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
  @Category(RuntimeRegressionTest.class)
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001)
        .setWithDebugInfos(true).compileAndRunTest(/* checkStructure  = */ true);
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test002() throws Exception {
    new RuntimeTestHelper(TEST002)
        .setWithDebugInfos(true).compileAndRunTest(/* checkStructure  = */ true);
  }

  @Test
  public void test003() throws Exception {
    checkStructure("003");
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test004() throws Exception {
    new RuntimeTestHelper(TEST004)
        .setWithDebugInfos(true).compileAndRunTest(/* checkStructure  = */ true);
  }

  @Test
  public void test005() throws Exception {
    checkStructure("005");
  }

  @Test
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
  public void test013() throws Exception {
    checkStructure("013");
  }

  @Test
  public void test014() throws Exception {
    checkStructure("014");
  }

  @Test
  public void test019() throws Exception {
    checkStructure("019");
  }

  private void checkStructure(@Nonnull String testNumber) throws Exception {
    CheckDexStructureTestHelper helper = new CheckDexStructureTestHelper(
        AbstractTestTools.getTestRootDir("com.android.jack.debug.test" + testNumber + ".jack"));
    helper.setWithDebugInfo(true).compare();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
    rtTestInfos.add(TEST002);
    rtTestInfos.add(TEST004);
  }
}
