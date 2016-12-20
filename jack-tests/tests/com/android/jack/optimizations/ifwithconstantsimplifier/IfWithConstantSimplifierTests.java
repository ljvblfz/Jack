/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.optimizations.ifwithconstantsimplifier;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import javax.annotation.Nonnull;

public class IfWithConstantSimplifierTests extends RuntimeTest {
  @Nonnull
  private final RuntimeTestInfo TEST001 = new IfWithConstantSimplifierTestRuntimeInfo("test001");

  @Nonnull
  private final RuntimeTestInfo TEST002 = new IfWithConstantSimplifierTestRuntimeInfo("test002");

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).compileAndRunTest();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test002() throws Exception {
    new RuntimeTestHelper(TEST002).compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
    rtTestInfos.add(TEST002);
  }

  /**
   * A subclass of {@link RuntimeTestInfo} dedicated to tests about IfWithConstantSimplifier.
   */
  private static class IfWithConstantSimplifierTestRuntimeInfo extends RuntimeTestInfo {
    @Nonnull
    private static final String PARENT_TEST_PACKAGE =
        "com.android.jack.optimizations.ifwithconstantsimplifier";

    public IfWithConstantSimplifierTestRuntimeInfo(@Nonnull String packageName) {
      super(getRootDir(packageName), getTestClassName(packageName));
      setSrcDirName("");
    }

    @Nonnull
    private static File getRootDir(@Nonnull String packageName) {
      return AbstractTestTools.getTestRootDir(PARENT_TEST_PACKAGE + "." + packageName);
    }

    @Nonnull
    private static String getTestClassName(@Nonnull String packageName) {
      return PARENT_TEST_PACKAGE + "." + packageName + ".Tests";

    }
  }
}
