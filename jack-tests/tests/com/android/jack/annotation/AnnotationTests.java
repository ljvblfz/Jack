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

package com.android.jack.annotation;

import com.android.jack.Main;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.CheckDexStructureTestHelper;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class AnnotationTests extends RuntimeTest {

  private static final File ANNOTATION001_PATH =
      AbstractTestTools.getTestRootDir("com.android.jack.annotation.test001.jack");

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.annotation.test001"),
      "com.android.jack.annotation.test001.dx.Tests");

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void checkStructure() throws Exception {
    CheckDexStructureTestHelper env =
        new CheckDexStructureTestHelper(new File(ANNOTATION001_PATH, "Annotation2.java"));
    env.setWithDebugInfo(true);
    env.compare();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void runtimeTest001() throws Exception {
    new RuntimeTestHelper(TEST001).compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
  }

}
