/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.reflect;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.unary.UnaryTests;

import org.junit.BeforeClass;
import org.junit.Test;

public class ReflectTests extends RuntimeTest {

  private static RuntimeTestInfo TEST001_WITH_SHRINK_SCHEDULABLE = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.reflect.test001"),
      "com.android.jack.reflect.test001.dx.Tests");

  private static RuntimeTestInfo TEST001_WITHOUT_SHRINK_SCHEDULABLE = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.reflect.test001"),
      "com.android.jack.reflect.test001.dx.Tests");

  @BeforeClass
  public static void setUpClass() {
    UnaryTests.class.getClassLoader().setDefaultAssertionStatus(true);
    TEST001_WITH_SHRINK_SCHEDULABLE.addProguardFlagsFileName("proguard.flags");
  }

  @Test
  public void simpleName001() throws Exception {
    new RuntimeTestHelper(TEST001_WITHOUT_SHRINK_SCHEDULABLE).compileAndRunTest();
  }


  @Test
  public void simpleName002() throws Exception {
    new RuntimeTestHelper(TEST001_WITH_SHRINK_SCHEDULABLE).compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001_WITH_SHRINK_SCHEDULABLE);
    rtTestInfos.add(TEST001_WITHOUT_SHRINK_SCHEDULABLE);
  }
}
