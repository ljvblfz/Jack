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
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.Test;
import org.junit.experimental.categories.Category;

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

  @Test
  // this test must be run with assertions enabled (for now, use dalvik)
  @Category(RuntimeRegressionTest.class)
  @KnownIssue
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
  public void test004() throws Exception {
    new RuntimeTestHelper(TEST004)
        .addProperty(Options.ASSERTION_POLICY.getName(), AssertionPolicy.ENABLE.toString())
        .compileAndRunTest();
  }

  @Test
  public void test005() throws Exception {
    new RuntimeTestHelper(TEST005)
        .addProperty(Options.ASSERTION_POLICY.getName(), AssertionPolicy.DISABLE.toString())
        .compileAndRunTest();
  }


  @Override
  protected void fillRtTestInfos() {
//    rtTestInfos.add(TEST001);
    rtTestInfos.add(TEST002);
    rtTestInfos.add(TEST003);
  }
}
